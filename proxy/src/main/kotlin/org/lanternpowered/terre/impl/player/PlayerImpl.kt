/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.player

import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.future.asDeferred
import org.lanternpowered.terre.MaxPlayers
import org.lanternpowered.terre.Player
import org.lanternpowered.terre.ProtocolVersion
import org.lanternpowered.terre.Server
import org.lanternpowered.terre.ServerConnectionRequestResult
import org.lanternpowered.terre.ServerInfo
import org.lanternpowered.terre.Team
import org.lanternpowered.terre.character.CharacterStorage
import org.lanternpowered.terre.dispatcher.async
import org.lanternpowered.terre.dispatcher.launchAsync
import org.lanternpowered.terre.event.character.InitCharacterStorageEvent
import org.lanternpowered.terre.event.connection.PlayerLoginEvent
import org.lanternpowered.terre.event.connection.PlayerPostLoginEvent
import org.lanternpowered.terre.event.player.PlayerDeathEvent
import org.lanternpowered.terre.event.server.PlayerJoinServerEvent
import org.lanternpowered.terre.event.server.PlayerLeaveServerEvent
import org.lanternpowered.terre.impl.ProxyImpl
import org.lanternpowered.terre.impl.ServerImpl
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.event.TerreEventBus
import org.lanternpowered.terre.impl.item.InventoryImpl
import org.lanternpowered.terre.impl.network.Connection
import org.lanternpowered.terre.impl.network.buffer.ItemId
import org.lanternpowered.terre.impl.network.buffer.NpcId
import org.lanternpowered.terre.impl.network.buffer.NpcType
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.client.ClientPlayConnectionHandler
import org.lanternpowered.terre.impl.network.packet.ChatMessagePacket
import org.lanternpowered.terre.impl.network.packet.CombatMessagePacket
import org.lanternpowered.terre.impl.network.packet.NpcUpdatePacket
import org.lanternpowered.terre.impl.network.packet.PlayerActivePacket
import org.lanternpowered.terre.impl.network.packet.PlayerChatMessagePacket
import org.lanternpowered.terre.impl.network.packet.PlayerCommandPacket
import org.lanternpowered.terre.impl.network.packet.PlayerHealthPacket
import org.lanternpowered.terre.impl.network.packet.PlayerInfoPacket
import org.lanternpowered.terre.impl.network.packet.PlayerInventorySlotPacket
import org.lanternpowered.terre.impl.network.packet.PlayerPvPPacket
import org.lanternpowered.terre.impl.network.packet.PlayerTeamPacket
import org.lanternpowered.terre.impl.network.packet.ProjectileDestroyPacket
import org.lanternpowered.terre.impl.network.packet.SimpleItemUpdatePacket
import org.lanternpowered.terre.impl.network.packet.StatusPacket
import org.lanternpowered.terre.impl.network.packet.TeleportPylonPacket
import org.lanternpowered.terre.impl.network.ProjectileDataMap
import org.lanternpowered.terre.impl.text.MessageReceiverImpl
import org.lanternpowered.terre.impl.util.channel.distinct
import org.lanternpowered.terre.item.ItemStack
import org.lanternpowered.terre.math.Vec2f
import org.lanternpowered.terre.math.Vec2i
import org.lanternpowered.terre.portal.Portal
import org.lanternpowered.terre.portal.PortalBuilder
import org.lanternpowered.terre.portal.PortalType
import org.lanternpowered.terre.text.MessageSender
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.text
import org.lanternpowered.terre.text.textOf
import org.lanternpowered.terre.util.AABB
import java.net.InetSocketAddress
import java.util.BitSet
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.math.max

internal class PlayerImpl(
  val clientConnection: Connection,
  override val name: String,
  override val clientUniqueId: UUID,
) : Player, MessageReceiverImpl {

  @Volatile override var latency = 0

  override val protocolVersion: ProtocolVersion
    get() = clientConnection.protocolVersion

  private fun generateUniqueId(): UUID {
    val nameUniqueId = UUID.nameUUIDFromBytes(name.toByteArray())
    var most = nameUniqueId.mostSignificantBits xor clientUniqueId.mostSignificantBits
    var least = nameUniqueId.leastSignificantBits xor clientUniqueId.leastSignificantBits
    // clear version
    most = most and (0xfL shl 12).inv()
    // set version
    most = most or (8L shl 12)
    // clear variant
    least = least and (0x3L shl 62).inv()
    // set variant
    least = least or (1 shl 62)
    return UUID(most, least)
  }

  override val uniqueId: UUID = generateUniqueId()

  override var serverConnection: ServerConnectionImpl? = null
    private set

  var serverSideCharacter: Boolean = false
    private set

  var clientSideCharacter: Boolean = true
    private set

  var characterStorage: CharacterStorage? = null
    private set

  var forwardNextOwnerUpdate = false

  var health = -1

  private var characterStoragePersistJob: Job? = null
  private var characterStoragePersistQueue: Channel<Int>? = null

  private val inventory = InventoryImpl()

  val playerId: PlayerId?
    get() = serverConnection?.playerId

  override val remoteAddress: InetSocketAddress
    get() = clientConnection.remoteAddress

  /**
   * The client unique id the server knows the player as.
   */
  var serverClientUniqueId = clientUniqueId

  /**
   * The underlying team value.
   */
  @Volatile var teamValue: Team = Team.None

  override var team: Team
    get() = teamValue
    set(value) {
      teamValue = value
      if (!handlingJoinServerEvent && (serverConnection != null || previousServer != null)) {
        val packet = PlayerTeamPacket(playerId!!, value)
        clientConnection.send(packet)
        serverConnection?.ensureConnected()?.send(packet)
      }
    }

  @Volatile var pvpEnabledValue = false

  override var pvpEnabled: Boolean
    get() = pvpEnabledValue
    set(value) {
      pvpEnabledValue = value
      if (!handlingJoinServerEvent && (serverConnection != null || previousServer != null)) {
        val packet = PlayerPvPPacket(playerId!!, value)
        clientConnection.send(packet)
        serverConnection?.ensureConnected()?.send(packet)
      }
    }

  @Volatile private var handlingJoinServerEvent = false

  /**
   * The last player info and send by the client.
   */
  lateinit var lastPlayerInfo: PlayerInfoPacket

  @Volatile override var position: Vec2f = Vec2f.Zero

  override val boundingBox: AABB
    get() = AABB.centerSize(Vec2f(20f, 42f)).offset(position)

  /**
   * The NPCs this player is aware of.
   */
  val trackedNpcs = BitSet()

  /**
   * The players this player is aware of.
   */
  val trackedPlayers = BitSet()

  /**
   * The items this player is aware of.
   */
  val trackedItems = BitSet()

  /**
   * The projectiles this player is aware of.
   */
  val trackedProjectiles = ProjectileDataMap()

  /**
   * The teleport pylons this player is aware of.
   */
  val trackedTeleportPylons = Int2LongOpenHashMap()

  /**
   * Whether the player was previously connected to another server.
   */
  var previousServer: ServerInfo? = null

  /**
   * Deferred that will be updated when the player is cleaned up.
   */
  private var cleanedUp = CompletableFuture<Unit>()

  /**
   * The status text that is currently being shown, and a counter of packets
   * that are expected before it can be shown.
   */
  var statusText: StatusPacket? = null
  var statusCounter = 0

  var permissionChecker: (String) -> Boolean = { true }

  fun setInventoryItem(index: Int, itemStack: ItemStack) {
    inventory[index] = itemStack
    val serverConnection = serverConnection
    if (serverConnection == null || !serverConnection.isWorldInitialized || serverSideCharacter)
      return // Too early to persist or server side character
    val characterStoragePersistQueue = characterStoragePersistQueue
    assert(characterStoragePersistQueue != null &&
      !characterStoragePersistQueue.trySend(index).isSuccess
    ) { "Failed to queue persist for item at $index" }
  }

  fun checkDuplicateIdentifier(): Boolean {
    // Duplicate client UUIDs aren't allowed by default, however duplicate names are.
    if (!ProxyImpl.allowMultiplePlayersPerClientUniqueId &&
      ProxyImpl.mutablePlayers.any { it.clientUniqueId == clientUniqueId }
    ) {
      clientConnection.close(textOf(
        "There's already a player connected with the same client unique id."))
      return true
    }
    return false
  }

  /**
   * Initializes the player and adds it to the proxy.
   */
  fun finishLogin(originalResult: PlayerLoginEvent.Result) {
    if (checkDuplicateIdentifier())
      return

    clientConnection.setConnectionHandler(ClientPlayConnectionHandler(this))

    var result = originalResult
    if (result is PlayerLoginEvent.Result.Allowed) {
      val maxPlayers = ProxyImpl.maxPlayers
      if (maxPlayers is MaxPlayers.Limited && ProxyImpl.players.size >= maxPlayers.amount) {
        result = PlayerLoginEvent.Result.Denied(textOf("The server is full."))
      }
    }

    TerreEventBus.postAsyncWithFuture(PlayerLoginEvent(this, result))
      .thenAcceptAsync({ event ->
        if (clientConnection.isClosed)
          return@thenAcceptAsync
        checkDuplicateIdentifier()
        if (ProxyImpl.mutablePlayers.addIfAbsent(this) != null) {
          clientConnection.close(textOf(
            "There's already a player connected with the same unique id."))
          return@thenAcceptAsync
        }
        val eventResult = event.result
        if (eventResult is PlayerLoginEvent.Result.Denied) {
          clientConnection.close(eventResult.reason)
        } else {
          TerreEventBus.postAsyncWithFuture(PlayerPostLoginEvent(this))
            .thenAccept { afterLogin() }
        }
      }, clientConnection.eventLoop)
  }

  private fun afterLogin() {
    TerreEventBus.postAsyncWithFuture(InitCharacterStorageEvent(this))
      .whenComplete { event, exception ->
        characterStorage = event.storage
        if (exception != null) {
          Terre.logger.error("Failed to initialize character storage for $name", exception)
          disconnectAndForget(textOf("Failed to initialize character storage."))
        } else {
          autoJoinServer()
        }
      }
  }

  private fun autoJoinServer() {
    // Try to connect to one of the servers
    val possibleServers = ProxyImpl.servers.asSequence()
      .filter { it.allowAutoJoin }
      .toList()
    connectToAnyWithFuture(possibleServers).whenComplete { result, _ ->
      if (result is ConnectResult.Failure)
        disconnectAndForget(failedToConnectReason(result.reason))
    }
  }

  private sealed interface ConnectResult {
    data class Success(val server: Server) : ConnectResult
    data class Failure(val reason: Text?) : ConnectResult
  }

  private fun connectToAnyWithFuture(
    servers: Iterable<Server>
  ): CompletableFuture<ConnectResult> {
    val queue = servers.toMutableList()
    if (queue.isEmpty())
      return CompletableFuture.completedFuture(null)

    var failureReason: Text? = null
    val future = CompletableFuture<ConnectResult>()

    fun connectNextOrComplete() {
      if (queue.isEmpty()) {
        future.complete(ConnectResult.Failure(failureReason))
        return
      }
      val next = queue.removeAt(0)
      connectToWithFuture(next).whenComplete { result, _ ->
        if (result is ServerConnectionRequestResult.Success) {
          future.complete(ConnectResult.Success(result.server))
        } else {
          if (failureReason == null && result is ServerConnectionRequestResult.Disconnected)
            failureReason = result.reason
          connectNextOrComplete()
        }
      }
    }
    connectNextOrComplete()

    return future
  }

  /**
   * Called when the player loses connection to the backing server.
   */
  fun disconnectedFromServer(connection: ServerConnectionImpl) {
    if (serverConnection != connection)
      return
    serverConnection = null

    val server = connection.server
    // Evacuate the player to another server
    val possibleServers = ProxyImpl.servers.asSequence()
      .filter { it.allowAutoJoin && it != server }
      .toList()
    connectToAnyWithFuture(possibleServers).whenComplete { result, _ ->
      if (result is ConnectResult.Failure)
        disconnectAndForget(failedToConnectReason(result.reason))
    }
  }

  private fun failedToConnectReason(reason: Text?): Text {
    val message = "Failed to connect to a server"
    return when {
      reason != null -> "$message: ".text() + reason
      else -> "$message.".text()
    }
  }

  override fun connectToAnyAsync(servers: Iterable<Server>) =
    connectToAnyWithFuture(servers)
      .thenApply { (it as? ConnectResult.Success)?.server }
      .asDeferred()

  fun cleanup() {
    val serverConnection = serverConnection
    val result = if (serverConnection != null) {
      val leaveEvent = PlayerLeaveServerEvent(this@PlayerImpl, serverConnection.server)
      TerreEventBus.postAsyncWithFuture(leaveEvent)
    } else {
      CompletableFuture.completedFuture(Unit)
    }
    result
      .handle { _, _ ->
        serverConnection?.connection?.close()
        saveAndCleanupCharacter()
      }
      .whenComplete { _, ex ->
        ProxyImpl.mutablePlayers.remove(this)
        if (ex != null) {
          Terre.logger.error("An exception occurred while saving and cleaning up character", ex)
        }
        cleanedUp.complete(Unit)
      }
  }

  override fun hasPermission(permission: String): Boolean = permissionChecker(permission)

  override fun sendMessage(message: Text) {
    clientConnection.send(ChatMessagePacket(message))
  }

  override fun sendMessageAs(message: Text, sender: MessageSender) {
    // If the player is on a server and the sender is also a player and on the same server,
    // send as a player chat message, this will show the text message above the head of the sender.
    if (sender is PlayerImpl) {
      val serverConnection = sender.serverConnection
      if (serverConnection != null && this.serverConnection?.server == serverConnection.server) {
        clientConnection.send(PlayerChatMessagePacket(serverConnection.playerId, message))
        return
      }
    }
    super<MessageReceiverImpl>.sendMessageAs(message, sender)
  }

  override fun openPortal(
    type: PortalType, position: Vec2f, builder: PortalBuilder.() -> Unit
  ): Portal = serverConnection!!.server.openPortalFor(type, position, builder, this)

  override fun showCombatText(text: Text, position: Vec2f) {
    clientConnection.send(CombatMessagePacket(position, text))
  }

  override fun executeCommandOnServer(command: String): Boolean {
    val serverConnection = serverConnection
    return if (serverConnection != null) {
      serverConnection.ensureConnected().send(PlayerCommandPacket("Say", "/$command"))
      true
    } else false
  }

  override fun showStatusText(text: Text, showShadows: Boolean) {
    val statusText = StatusPacket(0, text, true, showShadows)
    this.statusText = statusText
    if (statusCounter == 0)
      clientConnection.send(statusText)
  }

  override fun resetStatusText() {
    if (statusText == null)
      return
    statusText = null
    if (statusCounter == 0) {
      clientConnection.send(StatusPacket(0, textOf(),
        hidePercentage = false, showShadows = false))
    }
  }

  private fun disconnectAndForget(reason: Text) {
    clientConnection.close(reason)
    serverConnection?.connection?.close()
  }

  override fun disconnectAsync(reason: Text): Job {
    disconnectAndForget(reason)
    return cleanedUp.asDeferred()
  }

  fun connectToWithFuture(server: Server): CompletableFuture<ServerConnectionRequestResult> {
    server as ServerImpl
    val connection = ServerConnectionImpl(server, this)
    return connection.connect()
      .thenCompose { result ->
        val serverConnection = serverConnection
        if (result is ServerConnectionRequestResult.Success && serverConnection != null) {
          // When switching servers, cleanup first and wait for the leave event, then continue
          // with the server connection
          val leaveEvent = PlayerLeaveServerEvent(this@PlayerImpl, serverConnection.server)
          TerreEventBus.postAsyncWithFuture(leaveEvent)
            .thenCompose { saveAndCleanupCharacter() }
            .thenApply { result }
        } else {
          CompletableFuture.completedFuture(result)
        }
      }
      .whenCompleteAsync({ result, ex ->
        if (ex != null) {
          Terre.logger.debug("Failed to establish connection to backend server: ${server.info}", ex)
        } else if (result is ServerConnectionRequestResult.Success) {
          val old = serverConnection?.connection
          if (old != null) {
            old.setConnectionHandler(null)
            old.close()
          }
          serverConnection?.server?.removePlayer(this)
          // Replace it with the successfully established one
          serverConnection = connection
          // Reset client and then accept the new connection
          resetClient()
          connection.server.initPlayer(this)
          handlingJoinServerEvent = true
          TerreEventBus.postAsyncWithFuture(PlayerJoinServerEvent(this@PlayerImpl, server))
            .whenCompleteAsync({ event, _ ->
              handlingJoinServerEvent = false
              if (event != null) {
                serverClientUniqueId = event.clientUniqueId
              }
              try {
                connection.accept()
                Terre.logger.debug { "Successfully established connection to backend server: ${server.info}" }
              } catch (ex: Exception) {
                Terre.logger.error("Failed to accept connection to backend server: ${server.info}", ex)
                clientConnection.close(textOf("Internal server error."))
              }
            }, clientConnection.eventLoop)
        }
      }, clientConnection.eventLoop)
  }

  override fun connectToAsync(server: Server) = connectToWithFuture(server).asDeferred()

  fun updateServerSideCharacter(
    serverSideCharacter: Boolean
  ): Boolean {
    // server side character refers to data being stored on the backing server
    this.serverSideCharacter = serverSideCharacter
    // Once set to false, cannot be changed back, items will be lost on servers that don't have
    // server side characters and if there is no character storage
    if (serverSideCharacter || characterStorage != null)
      clientSideCharacter = false
    return !clientSideCharacter
  }

  fun loadAndInitCharacter() {
    if (serverSideCharacter)
      return
    val characterStorage = characterStorage ?: return
    launchAsync {
      loadAndInitCharacterStorage(characterStorage)
    }
  }

  private suspend fun loadAndInitCharacterStorage(characterStorage: CharacterStorage) {
    inventory.clear()
    characterStorage.loadInventory(inventory)
    val serverConnection = serverConnection!!
    val playerId = serverConnection.playerId
    for (index in 0..<inventory.maxSize) {
      val packet = PlayerInventorySlotPacket(playerId, index, inventory[index])
      clientConnection.send(packet)
      serverConnection.ensureConnected().send(packet)
    }
    initItemPersistJob()
  }

  private fun saveAndCleanupCharacter(): CompletableFuture<Unit> {
    val characterStorage = characterStorage
    if (serverSideCharacter || characterStorage == null)
      return CompletableFuture.completedFuture(Unit)
    return async {
      val characterStoragePersistJob = characterStoragePersistJob
      if (characterStoragePersistJob != null) {
        // wait for all persists to complete
        characterStoragePersistJob.cancel()
        characterStoragePersistJob.join()
        this@PlayerImpl.characterStoragePersistJob = null
        this@PlayerImpl.characterStoragePersistQueue = null
      }
      try {
        characterStorage.saveInventory(inventory)
      } catch (ex: Exception) {
        Terre.logger.error("Failed to save inventory for player $name", ex)
      }
    }.asCompletableFuture()
  }

  private fun initItemPersistJob() {
    if (characterStoragePersistJob != null)
      return
    val characterStorage = characterStorage ?: return
    val characterStoragePersistQueue = Channel<Int>(Channel.UNLIMITED).distinct()
    this.characterStoragePersistQueue = characterStoragePersistQueue
    characterStoragePersistJob = launchAsync {
      while (true) {
        val index = characterStoragePersistQueue.receive()
        val item = inventory[index]
        characterStorage.saveItem(index, item)
      }
    }
  }

  private fun resetClient() {
    health = -1
    trackedPlayers.stream().forEach { id ->
      clientConnection.send(PlayerActivePacket(PlayerId(id), false))
    }
    trackedPlayers.clear()
    trackedNpcs.stream().forEach { id ->
      clientConnection.send(NpcUpdatePacket(NpcId(id), NpcType(0), Vec2f.Zero, 0))
    }
    trackedNpcs.clear()
    trackedItems.stream().forEach { id ->
      clientConnection.send(SimpleItemUpdatePacket(ItemId(id), Vec2f.Zero, ItemStack.Empty))
    }
    trackedItems.clear()
    trackedProjectiles.forEach { id, owner ->
      clientConnection.send(ProjectileDestroyPacket(id, owner))
    }
    trackedProjectiles.clear()
    trackedTeleportPylons.forEach { (type, position) ->
      clientConnection.send(TeleportPylonPacket(TeleportPylonPacket.Action.Removed, type, Vec2i(position)))
    }
    trackedTeleportPylons.clear()
  }

  fun handleHealth(packet: PlayerHealthPacket, connection: Connection): Boolean {
    val health = max(0, packet.current)
    val previousHealth = this.health
    this.health = health
    if (previousHealth != -1 && health == 0 && previousHealth != 0) {
      TerreEventBus.postAsyncWithFuture(PlayerDeathEvent(this))
        .whenCompleteAsync({ _, exception ->
          if (exception != null) {
            Terre.logger.error("Failed to handle player death event", exception)
          } else {
            connection.send(packet)
          }
        }, connection.eventLoop)
      return false // Do not forward
    }
    return true // Forward
  }

}

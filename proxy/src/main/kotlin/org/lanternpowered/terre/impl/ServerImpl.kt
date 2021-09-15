/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import kotlinx.coroutines.Job
import org.lanternpowered.terre.Player
import org.lanternpowered.terre.ProtocolVersion
import org.lanternpowered.terre.Server
import org.lanternpowered.terre.ServerInfo
import org.lanternpowered.terre.coroutines.delay
import org.lanternpowered.terre.dispatcher.launchAsync
import org.lanternpowered.terre.impl.event.EventExecutor
import org.lanternpowered.terre.impl.network.VersionedProtocol
import org.lanternpowered.terre.impl.network.buffer.ProjectileId
import org.lanternpowered.terre.impl.player.PlayerImpl
import org.lanternpowered.terre.impl.portal.PortalBuilderImpl
import org.lanternpowered.terre.impl.portal.PortalImpl
import org.lanternpowered.terre.math.Vec2f
import org.lanternpowered.terre.portal.Portal
import org.lanternpowered.terre.portal.PortalBuilder
import org.lanternpowered.terre.portal.PortalType
import org.lanternpowered.terre.text.MessageSender
import org.lanternpowered.terre.text.Text
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

internal class ServerImpl(
  override val info: ServerInfo,
  override var allowAutoJoin: Boolean = false,
  val versionedProtocol: VersionedProtocol? = null
) : Server {

  var unregistered = false
    private set

  val registerLock = Any()

  private val mutablePlayers = MutablePlayerCollection.concurrentOf()

  private val projectileIdAllocator = ProjectileIdAllocator()
  private val portalsByProjectileId = ConcurrentHashMap<Int, PortalImpl>()
  private val perPlayerProjectileIdAllocator = PerPlayerProjectileIdAllocator()
  private val portals: MutableSet<PortalImpl> = Collections.newSetFromMap(ConcurrentHashMap())

  private inner class PerPlayerProjectileIdAllocator {

    private val ids = Int2ObjectOpenHashMap<MutableSet<UUID>>()

    fun allocate(player: PlayerImpl): ProjectileId {
      for ((id, uniqueIds) in ids) {
        if (uniqueIds.add(player.uniqueId))
          return ProjectileId(id)
      }
      val id = projectileIdAllocator.allocate()
      val uniqueIds = HashSet<UUID>()
      uniqueIds += player.uniqueId
      ids[id.value] = uniqueIds
      return id
    }

    fun release(player: PlayerImpl, id: ProjectileId) {
      val uniqueIds = ids.get(id.value)
      if (uniqueIds != null
        && uniqueIds.remove(player.uniqueId)
        && uniqueIds.isEmpty()
      ) {
        ids.remove(id.value)
        projectileIdAllocator.release(id)
      }
    }
  }

  private var portalUpdateJob: Job? = null
  private var portalCollisionJob: Job? = null

  /**
   * The last server version that was noticed by connecting clients. Is used to speed up
   * connection when multiple versions are possible.
   */
  @Volatile var lastKnownVersion: ProtocolVersion? = null

  override val players
    get() = mutablePlayers.toImmutable()

  fun initPlayer(player: Player) {
    mutablePlayers.add(player)

    // Send all the portals
    player as PlayerImpl
    for (portal in portalsByProjectileId.values)
      sendOpenPortalTo(portal, player)
  }

  fun removePlayer(player: Player) {
    mutablePlayers.remove(player)

    // Destroy all the portals
    player as PlayerImpl
    for (portal in portalsByProjectileId.values)
      sendClosePortalTo(portal, player)

    for (portal in portals.toList()) {
      if (portal.player == player)
        portal.close()
    }
  }

  fun init() {
    portalCollisionJob = launchAsync(EventExecutor.dispatcher) {
      while (true) {
        val allPlayers = mutablePlayers.toList()
        // Update all portals, so the projectiles don't expire
        for (portal in portals) {
          val players = if (portal.player != null) listOf(portal.player!!) else allPlayers
          for (player in players) {
            val intersects = player.boundingBox.intersects(portal.boundingBox)
            if (intersects && portal.colliding.add(player)) {
              portal.onStartCollide(portal, player)
            } else if (!intersects && portal.colliding.remove(player)) {
              portal.onStopCollide(portal, player)
            }
          }
        }
        delay(25)
      }
    }
    portalUpdateJob = launchAsync(EventExecutor.dispatcher) {
      while (true) {
        val allPlayers = mutablePlayers.toList()
        // Update all portals, so the projectiles don't expire
        for (portal in portals) {
          val players = if (portal.player != null) listOf(portal.player!!) else allPlayers
          sendOpenPortalTo(portal, players)
        }
        delay(1000)
      }
    }
  }

  private fun cleanup() {
    portalUpdateJob?.cancel()
    portalUpdateJob = null
    portalCollisionJob?.cancel()
    portalCollisionJob = null
  }

  override fun unregister() {
    unregistered = true
    ProxyImpl.servers.unregister(this)
    cleanup()
  }

  override fun evacuate() {
    TODO("not implemented")
  }

  override fun sendMessage(message: String) {
    mutablePlayers.forEach { it.sendMessage(message) }
  }

  override fun sendMessage(message: Text) {
    mutablePlayers.forEach { it.sendMessage(message) }
  }

  override fun sendMessageAs(message: Text, sender: MessageSender) {
    mutablePlayers.forEach { it.sendMessageAs(message, sender) }
  }

  override fun sendMessageAs(message: String, sender: MessageSender) {
    mutablePlayers.forEach { it.sendMessageAs(message, sender) }
  }

  override fun openPortal(
    type: PortalType, position: Vec2f, builder: PortalBuilder.() -> Unit
  ): Portal {
    val builderImpl = PortalBuilderImpl(projectileIdAllocator::allocate, this, type, position)
    builderImpl.builder()
    val portal = builderImpl.build()
    portalsByProjectileId[portal.projectileId.value] = portal
    portals += portal
    // Open for all the players that are currently connected
    sendOpenPortalTo(portal, mutablePlayers)
    return portal
  }

  fun openPortalFor(
    type: PortalType, position: Vec2f, builder: PortalBuilder.() -> Unit, player: PlayerImpl
  ): Portal {
    val idAllocator = { perPlayerProjectileIdAllocator.allocate(player) }
    val builderImpl = PortalBuilderImpl(idAllocator, this, type, position)
    builderImpl.builder()
    val portal = builderImpl.build()
    portal.player = player
    portals += portal
    sendOpenPortalTo(portal, player)
    return portal
  }

  private fun sendOpenPortalTo(portal: PortalImpl, players: Iterable<Player>) {
    val packet = portal.createUpdatePacket()
    if (packet != null) {
      for (player in players)
        (player as PlayerImpl).clientConnection.send(packet)
    }
  }

  private fun sendOpenPortalTo(portal: PortalImpl, player: Player) {
    val packet = portal.createUpdatePacket()
    if (packet != null)
      (player as PlayerImpl).clientConnection.send(packet)
  }

  private fun sendClosePortalTo(portal: PortalImpl, players: Iterable<Player>) {
    val packet = portal.createDestroyPacket()
    if (packet != null) {
      for (player in players)
        (player as PlayerImpl).clientConnection.send(packet)
    }
  }

  private fun sendClosePortalTo(portal: PortalImpl, player: Player) {
    val packet = portal.createDestroyPacket()
    if (packet != null)
      (player as PlayerImpl).clientConnection.send(packet)
  }

  fun closePortal(portal: PortalImpl) {
    if (!portals.remove(portal))
      return
    val player = portal.player
    if (player != null) {
      portal.player = null
      perPlayerProjectileIdAllocator.release(player, portal.projectileId)
      return
    }
    portalsByProjectileId.remove(portal.projectileId.value)
    projectileIdAllocator.release(portal.projectileId)
    sendClosePortalTo(portal, mutablePlayers)
  }
}

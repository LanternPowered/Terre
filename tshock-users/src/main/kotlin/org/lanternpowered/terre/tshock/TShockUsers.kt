/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.tshock

import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.jetbrains.exposed.sql.Database
import org.lanternpowered.terre.Console
import org.lanternpowered.terre.Player
import org.lanternpowered.terre.config.ConfigDirectory
import org.lanternpowered.terre.dispatcher.launchAsync
import org.lanternpowered.terre.event.Subscribe
import org.lanternpowered.terre.event.chat.ServerChatEvent
import org.lanternpowered.terre.event.permission.InitPermissionSubjectEvent
import org.lanternpowered.terre.event.proxy.ProxyInitializeEvent
import org.lanternpowered.terre.event.server.PlayerJoinServerEvent
import org.lanternpowered.terre.event.server.PlayerLeaveServerEvent
import org.lanternpowered.terre.logger.Logger
import org.lanternpowered.terre.plugin.Plugin
import org.lanternpowered.terre.plugin.inject
import org.lanternpowered.terre.sql.SqlManager
import org.lanternpowered.terre.text.text
import org.lanternpowered.terre.tshock.group.Groups
import org.lanternpowered.terre.tshock.user.UserTable
import org.lanternpowered.terre.tshock.user.requireUserByName
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * This plugin connects to the same mysql database that is used by a network of tShock servers.
 *
 * Features and limitations:
 * - All tShock servers must be connected to a single mysql database, this means that all the
 *   character data is shared between servers.
 * - Hooks into the user permission database so tShock commands can be used to assign proxy level
 *   permissions to players. New permissions will only be picked up after logging out and back in.
 * - UUID login must be enabled on the backing tShock servers to ensure seamless switching between
 *   servers. UUID login can be disabled on proxy level, which will generate a client unique UUID
 *   that is used for the servers while connected to the proxy, the first time the player will
 *   need to log in.
 * - UUID bans on tShock servers is useless if UUID login is disabled on the proxy.
 */
@Plugin(id = "tshock-users")
object TShockUsers {

  private val logger = inject<Logger>()
  private val configDir = inject<ConfigDirectory>()

  private var database: Database? = null
  private var uuidLogin = true

  private val playerCache = Caffeine
    .newBuilder()
    .weakKeys()
    .build<Player, TShockPlayer> { player ->
      // If uuid login is disabled, generate a random UUID that will be used to connect to the
      // backing server so the player will need to log in every time it connects again to the proxy
      TShockPlayer(if (uuidLogin) player.clientUniqueId else UUID.randomUUID())
    }

  @Subscribe
  private suspend fun onInit(event: ProxyInitializeEvent) {
    logger.info { "Initializing tShock users plugin!" }

    val config = configDir.config {
      addSpec(TShockUsersConfigSpec)
    }
    config.loadOrCreate()

    uuidLogin = config[TShockUsersConfigSpec.uuidLogin]
    val host = config[TShockUsersConfigSpec.Database.host]
    val port = config[TShockUsersConfigSpec.Database.port]
    val user = config[TShockUsersConfigSpec.Database.user]
    val password = config[TShockUsersConfigSpec.Database.password]
    var databaseName = config[TShockUsersConfigSpec.Database.database]
    if (databaseName.isEmpty())
      databaseName = user
    if (host.isNotEmpty() && user.isNotEmpty() && password.isNotEmpty() && databaseName.isNotEmpty()) {
      val dataSource = SqlManager.dataSource(
        url = "mysql://$host:$port/$databaseName",
        user = user,
        password = password,
      )
      database = Database.connect(dataSource)
    } else {
      logger.warn { "No database is configured, plugin will be disabled." }
    }
  }

  /**
   * Returns the [TShockPlayer] for the [Player].
   */
  private val Player.tShock: TShockPlayer
    get() = playerCache.get(this)

  @Subscribe
  private fun onInitPermissionSubject(event: InitPermissionSubjectEvent) {
    val subject = event.subject
    if (subject is Console) {
      event.permissionChecker = { true }
    } else if (subject is Player) {
      event.permissionChecker = subject.tShock::hasPermission
    }
  }

  @Subscribe
  private fun onPlayerJoinServer(event: PlayerJoinServerEvent) {
    event.clientUniqueId = event.player.tShock.clientUniqueId
  }

  @Subscribe
  private suspend fun onPlayerLeaveServer(event: PlayerLeaveServerEvent) {
    val player = event.player
    val tShockPlayer = player.tShock
    if (!tShockPlayer.loggedIn)
      return
    player.executeCommandOnServer("logout")
    val logoutDone = CompletableDeferred<Unit>()
    tShockPlayer.logoutDone = logoutDone
    tShockPlayer.blockLoggedOutSSCEnabledMessage = true
    try {
      withTimeout(5.seconds) {
        logoutDone.await()
        logger.info { "Player ${player.name} logged out from ${event.server.name}." }
      }
    } catch (ex: TimeoutCancellationException) {
      logger.error("Timed out while waiting for tshock to logout.", ex)
    }
  }

  @Subscribe
  private suspend fun onServerChat(event: ServerChatEvent) {
    val database = database
    if (event.sender != null || database == null)
      return
    val player = event.player
    val tShockPlayer = player.tShock
    val logoutDone = tShockPlayer.logoutDone
    val message = event.message.toPlain()
    if (TShockMessages.isLoggedOut(message)) {
      if (logoutDone != null) {
        event.cancelled = true
        logoutDone.complete(Unit)
        tShockPlayer.logoutDone = null
      } else {
        // Only mark as logged out if explicitly logged out
        tShockPlayer.loggedIn = false
      }
    } else if (tShockPlayer.blockLoggedOutSSCEnabledMessage &&
      TShockMessages.isLoggedOutSSCEnabled(message)
    ) {
      event.cancelled = true
      tShockPlayer.blockLoggedOutSSCEnabledMessage = false
    } else if (tShockPlayer.loggedIn && TShockMessages.isHasJoined(message)) {
      event.cancelled = true
    } else {
      val name = TShockMessages.findAuthenticatedUser(message)
      if (name != null) {
        if (tShockPlayer.loggedIn) {
          event.cancelled = true
        }
        tShockPlayer.loggedIn = true
        tShockPlayer.name = name
        var user = UserTable.requireUserByName(database, name)
        suspend fun userLoggedIn() {
          logger.info { "Logged in user $name, group: ${user.group}" }
          Groups.load(database)
          tShockPlayer.groupName = user.group
        }
        if (user.clientIdentifier != tShockPlayer.clientIdentifier) {
          event.cancelled = true
          // Recheck the db after a bit of time because the UUID is updated after the message is
          // sent, so it is possible that the UUID is updated yet
          launchAsync {
            for (i in 0..<6) {
              delay(500.milliseconds)
              user = UserTable.requireUserByName(database, name)
              if (user.clientIdentifier == tShockPlayer.clientIdentifier) {
                userLoggedIn()
                return@launchAsync
              }
            }
            logger.error("Kicking $name for client identifier mismatch.")
            player.disconnectAsync("Client identifier mismatch.".text())
          }
        } else {
          userLoggedIn()
        }
      }
    }
  }
}

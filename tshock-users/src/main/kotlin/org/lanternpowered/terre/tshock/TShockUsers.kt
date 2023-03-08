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
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.jetbrains.exposed.sql.Database
import org.lanternpowered.terre.Console
import org.lanternpowered.terre.Player
import org.lanternpowered.terre.config.ConfigDirectory
import org.lanternpowered.terre.event.Subscribe
import org.lanternpowered.terre.event.chat.ServerChatEvent
import org.lanternpowered.terre.event.permission.InitPermissionSubjectEvent
import org.lanternpowered.terre.event.proxy.ProxyInitializeEvent
import org.lanternpowered.terre.event.server.PlayerLeaveServerEvent
import org.lanternpowered.terre.logger.Logger
import org.lanternpowered.terre.plugin.Plugin
import org.lanternpowered.terre.plugin.inject
import org.lanternpowered.terre.tshock.user.User
import kotlin.time.Duration.Companion.seconds

@Plugin(id = "tshock-users")
object TShockUsers {

  private val logger = inject<Logger>()
  private val configDir = inject<ConfigDirectory>()

  private var database: Database? = null

  private val playerCache = Caffeine
    .newBuilder()
    .weakKeys()
    .build<Player, TShockPlayer> { player ->
      TShockPlayer(logger, database!!, User.generateIdentifier(player.clientUniqueId))
    }

  @Subscribe
  private suspend fun onInit(event: ProxyInitializeEvent) {
    logger.info { "Initializing tShock users plugin!" }

    val config = configDir.config {
      addSpec(TShockUsersConfigSpec)
    }
    config.loadOrCreate()

    val host = config[TShockUsersConfigSpec.Database.host]
    val port = config[TShockUsersConfigSpec.Database.port]
    val user = config[TShockUsersConfigSpec.Database.user]
    val password = config[TShockUsersConfigSpec.Database.password]
    var databaseName = config[TShockUsersConfigSpec.Database.database]
    if (databaseName.isEmpty())
      databaseName = user
    if (host.isNotEmpty() && user.isNotEmpty() && password.isNotEmpty() && databaseName.isNotEmpty()) {
      val dataSource = HikariDataSource().apply {
        this.jdbcUrl = "jdbc:mariadb://$host:$port/$databaseName"
        this.username = user
        this.password = password
      }
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

  private val authenticatedRegex = "^Authenticated as (.+) successfully.\$".toRegex()
  private val loggedOutMessages = setOf(
    "You are not logged-in. Therefore, you cannot logout.",
    "You have been successfully logged out of your account.",
  )
  private const val serverSideCharacterMessage =
    "Server side characters are enabled. You need to be logged-in to play."

  @Subscribe
  private suspend fun onPlayerLeaveServer(event: PlayerLeaveServerEvent) {
    val player = event.player
    val tShockPlayer = player.tShock
    if (!tShockPlayer.loggedIn)
      return
    player.executeCommandOnServer("logout")
    val logoutDone = CompletableDeferred<Unit>()
    tShockPlayer.logoutDone = logoutDone
    tShockPlayer.blockServerSideCharacterMessage = true
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
    if (event.sender != null || database == null)
      return
    val tShockPlayer = event.player.tShock
    val logoutDone = tShockPlayer.logoutDone
    val message = event.message.toPlain()
    if (tShockPlayer.loggedIn) {
      if (logoutDone != null && message in loggedOutMessages) {
        event.cancelled = true
        logoutDone.complete(Unit)
        tShockPlayer.logoutDone = null
      }
    } else {
      // tShock sends a message to the client when a user is authenticated, so we can hook into that
      val authenticatedMatch = authenticatedRegex.find(message)
      if (authenticatedMatch != null) {
        val name = authenticatedMatch.groupValues[1]
        when (val result = tShockPlayer.login(name)) {
          LoginResult.Success -> {}
          LoginResult.AlreadyLoggedIn -> {
            // Don't log message when switching servers
            event.cancelled = true
          }
          is LoginResult.Denied -> {
            event.cancelled = true
            event.player.disconnectAsync(result.reason)
          }
        }
      }
    }
    if (message == serverSideCharacterMessage && tShockPlayer.blockServerSideCharacterMessage) {
      event.cancelled = true
      tShockPlayer.blockServerSideCharacterMessage = false
    }
  }
}

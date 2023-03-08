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

import kotlinx.coroutines.CompletableDeferred
import org.jetbrains.exposed.sql.Database
import org.lanternpowered.terre.logger.Logger
import org.lanternpowered.terre.text.text
import org.lanternpowered.terre.tshock.group.Group
import org.lanternpowered.terre.tshock.group.Groups
import org.lanternpowered.terre.tshock.user.UserTable
import org.lanternpowered.terre.tshock.user.loadUserByName

class TShockPlayer(
  private val logger: Logger,
  private val db: Database,
  private val clientIdentifier: String,
) {

  var name: String = ""
    private set

  var loggedIn: Boolean = false
    private set

  var blockServerSideCharacterMessage = false

  private var groupName: String? = null

  private val group: Group?
    get() {
      val groupName = groupName
      return if (groupName != null) Groups[groupName] else null
    }

  var logoutDone: CompletableDeferred<Unit>? = null

  suspend fun login(name: String): LoginResult {
    if (loggedIn && this.name == name)
      return LoginResult.AlreadyLoggedIn
    loggedIn = true
    val user = UserTable.loadUserByName(db, name)
      ?: error("No user data for: $name")
    if (user.clientIdentifier != clientIdentifier) {
      logger.error("Kicking $name for client identifier mismatch.")
      return LoginResult.Denied("Client identifier mismatch.".text())
    }
    logger.info { "Logged in user $name, group: ${user.group}" }
    Groups.load(db)
    groupName = user.group
    return LoginResult.Success
  }

  fun hasPermission(permission: String): Boolean {
    val group = group
    if (group != null)
      return group.hasPermission(permission)
    return false
  }
}

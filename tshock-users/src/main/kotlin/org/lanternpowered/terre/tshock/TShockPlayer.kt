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
import org.lanternpowered.terre.tshock.group.Group
import org.lanternpowered.terre.tshock.group.Groups
import org.lanternpowered.terre.tshock.user.User
import java.util.UUID

class TShockPlayer(
  var clientUniqueId: UUID,
) {

  val clientIdentifier = User.generateIdentifier(clientUniqueId)
  var name: String = ""
  var loggedIn: Boolean = false
  var blockLoggedOutSSCEnabledMessage = false
  var groupName: String? = null
  var logoutDone: CompletableDeferred<Unit>? = null

  val group: Group?
    get() {
      val groupName = groupName
      return if (groupName != null) Groups[groupName] else null
    }

  fun hasPermission(permission: String): Boolean {
    val group = group
    if (group != null)
      return group.hasPermission(permission)
    return false
  }
}

/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.event.permission

import org.lanternpowered.terre.Player
import org.lanternpowered.terre.event.Event
import org.lanternpowered.terre.event.connection.PlayerPreLoginEvent
import org.lanternpowered.terre.permission.PermissionSubject

/**
 * An event that is thrown when setting up the permission function of a specific [subject].
 *
 * For [Player]s, this event is thrown before [PlayerPreLoginEvent].
 *
 * @property subject The subject that is being setup.
 * @property permissionChecker A function that checks permissions.
 */
data class InitPermissionSubjectEvent(
  val subject: PermissionSubject,
  var permissionChecker: (permission: String) -> Boolean = { true },
) : Event

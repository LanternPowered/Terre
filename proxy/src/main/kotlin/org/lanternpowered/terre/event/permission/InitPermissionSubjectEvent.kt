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

import org.lanternpowered.terre.event.Event
import org.lanternpowered.terre.permission.PermissionSubject

/**
 * An event that is thrown when setting up the permission function of a specific [subject].
 *
 * @property subject The subject that is being setup.
 * @property permissionFunction Provides a permission function.
 */
data class InitPermissionSubjectEvent(
  val subject: PermissionSubject,
  var permissionFunction: (permission: String) -> Boolean? = { null },
) : Event

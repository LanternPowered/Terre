/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.permission

/**
 * Represents an object that has a set of permissions.
 */
interface PermissionSubject {

  /**
   * Returns if this subject has the specified [permission].
   */
  fun hasPermission(permission: String): Boolean
}

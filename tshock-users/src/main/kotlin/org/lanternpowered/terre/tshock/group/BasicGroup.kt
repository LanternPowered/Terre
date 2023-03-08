/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.tshock.group

import org.lanternpowered.terre.util.Color
import org.lanternpowered.terre.util.Colors

data class BasicGroup(
  override val name: String,
  val permissions: Set<String> = setOf(),
  val negatedPermissions: Set<String> = setOf(),
  val chatColor: Color = Colors.White,
  val prefix: String = "",
  val suffix: String = "",
  var parent: Group? = null,
  val parentName: String = parent?.name ?: "",
): Group {

  override fun hasPermission(permission: String): Boolean {
    val value = permissionValue(permission)
    if (value != null)
      return value
    val parent = parent
    if (parent != null)
      return parent.hasPermission(permission)
    return false
  }

  private fun permissionValue(permission: String): Boolean? {
    if (permission in permissions)
      return true
    if (permission in negatedPermissions)
      return false
    var index = permission.length
    while (true) {
      index = permission.lastIndexOf(char = '.', startIndex = index - 1)
      if (index == -1)
        return null
      var wildcard = permission.substring(0, index)
      if (wildcard in permissions)
        return true
      if (wildcard in negatedPermissions)
        return false
      wildcard += ".*"
      if (wildcard in permissions)
        return true
      if (wildcard in negatedPermissions)
        return false
    }
  }
}


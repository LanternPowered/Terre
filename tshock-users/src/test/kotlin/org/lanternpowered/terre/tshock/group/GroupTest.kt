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

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GroupTest {

  @Test
  fun positive() {
    val group = BasicGroup(
      name = "Test",
      permissions = setOf(
        "commands.first.first",
        "commands.first.second",
      )
    )
    assertTrue(group.hasPermission("commands.first.first"))
    assertTrue(group.hasPermission("commands.first.second"))
    assertFalse(group.hasPermission("commands.first.third"))
    assertFalse(group.hasPermission("commands.second"))
  }

  @Test
  fun `positive wildcard`() {
    val group = BasicGroup(
      name = "Test",
      permissions = setOf(
        "commands.first.*",
      )
    )
    assertTrue(group.hasPermission("commands.first.first"))
    assertTrue(group.hasPermission("commands.first.second"))
    assertTrue(group.hasPermission("commands.first.third"))
    assertFalse(group.hasPermission("commands.second"))
  }

  @Test
  fun `positive wildcard without star`() {
    val group = BasicGroup(
      name = "Test",
      permissions = setOf(
        "commands.first",
      )
    )
    assertTrue(group.hasPermission("commands.first.first"))
    assertTrue(group.hasPermission("commands.first.second"))
    assertTrue(group.hasPermission("commands.first.third"))
    assertFalse(group.hasPermission("commands.second"))
  }

  @Test
  fun `positive wildcard 2`() {
    val group = BasicGroup(
      name = "Test",
      permissions = setOf(
        "commands.*",
      )
    )
    assertTrue(group.hasPermission("commands.first.first"))
    assertTrue(group.hasPermission("commands.first.second"))
    assertTrue(group.hasPermission("commands.first.third"))
    assertTrue(group.hasPermission("commands.second"))
    assertFalse(group.hasPermission("portals.commands.first"))
  }

  @Test
  fun `positive super admin`() {
    val group = BasicGroup(
      name = "Test",
      permissions = setOf(
        "commands.first.first",
        "commands.first.second",
      ),
      parent = SuperAdminGroup
    )
    assertTrue(group.hasPermission("commands.first.first"))
    assertTrue(group.hasPermission("commands.first.second"))
    assertTrue(group.hasPermission("commands.first.third"))
    assertTrue(group.hasPermission("commands.second"))
  }

  @Test
  fun `negate super admin`() {
    val group = BasicGroup(
      name = "Test",
      negatedPermissions = setOf(
        "commands.first.first",
        "commands.first.second",
      ),
      parent = SuperAdminGroup
    )
    assertFalse(group.hasPermission("commands.first.first"))
    assertFalse(group.hasPermission("commands.first.second"))
    assertTrue(group.hasPermission("commands.first.third"))
    assertTrue(group.hasPermission("commands.second"))
  }

  @Test
  fun `negate super admin wildcard`() {
    val group = BasicGroup(
      name = "Test",
      negatedPermissions = setOf(
        "commands.first.*"
      ),
      parent = SuperAdminGroup
    )
    assertFalse(group.hasPermission("commands.first.first"))
    assertFalse(group.hasPermission("commands.first.second"))
    assertFalse(group.hasPermission("commands.first.third"))
    assertTrue(group.hasPermission("commands.second"))
  }
}

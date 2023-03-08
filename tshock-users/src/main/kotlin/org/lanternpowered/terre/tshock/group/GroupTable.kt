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

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.lanternpowered.terre.util.Color

object GroupTable : Table("GroupList") {
  val name = varchar("GroupName", 32)
  val parent = varchar("Parent", 32)
  val commands = text("Commands")
  val chatColor = text("ChatColor")
  val prefix = text("Prefix")
  val suffix = text("Suffix")
  override val primaryKey = PrimaryKey(name)
}

suspend fun GroupTable.loadGroups(db: Database): Collection<Group> =
  newSuspendedTransaction(Dispatchers.IO, db) {
    val groups = hashMapOf<String, Group>()
    selectAll()
      .map { it.toBasicGroup() }
      .associateByTo(groups) { it.name }
    groups[SuperAdminGroup.name] = SuperAdminGroup
    groups.values.forEach { group ->
      if (group is BasicGroup) {
        val parentName = group.parentName
        if (parentName.isNotEmpty())
          group.parent = groups[parentName]
      }
    }
    groups.values
  }

private fun ResultRow.toBasicGroup(): BasicGroup {
  val name = this[GroupTable.name]
  val permissions = hashSetOf<String>()
  val negatedPermissions = hashSetOf<String>()
  this[GroupTable.commands]
    .splitToSequence(',')
    .forEach { permission ->
      if (permission[0] == '!') {
        negatedPermissions += permission.substring(1)
      } else {
        permissions += permission
      }
    }
  val chatColor = this[GroupTable.chatColor]
    .split(',')
    .let { Color(it[0].toInt(), it[1].toInt(), it[2].toInt()) }
  val prefix = this[GroupTable.prefix]
  val suffix = this[GroupTable.suffix]
  val parent = this[GroupTable.parent]
  return BasicGroup(name, permissions, negatedPermissions,
    chatColor, prefix, suffix, null, parent)
}

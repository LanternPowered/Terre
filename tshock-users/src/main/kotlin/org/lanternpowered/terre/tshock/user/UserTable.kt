/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.tshock.user

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object UserTable : Table("Users") {
  val id = integer("ID").autoIncrement()
  val name = varchar("Username", 32).uniqueIndex()
  val password = varchar("Password", 128)
  val clientIdentifier = varchar("UUID", 128)
  val group = text("Usergroup")
  val registered = text("Registered")
  val lastAccessed = text("LastAccessed")
  val knownIPs = text("KnownIPs")
  override val primaryKey = PrimaryKey(id)
}

suspend fun UserTable.loadUserByName(db: Database, name: String): User? =
  newSuspendedTransaction(Dispatchers.IO, db) {
    select { UserTable.name.eq(name) }.map { it.toUser() }.firstOrNull()
  }

suspend fun UserTable.requireUserByName(db: Database, name: String): User =
  loadUserByName(db, name) ?: error("No user data for: $name")

private fun parseDateTime(value: String): LocalDateTime =
  DateTimeFormatter.ISO_DATE_TIME.parse(value, LocalDateTime::from)

private fun ResultRow.toUser(): User {
  val id = this[UserTable.id]
  val name = this[UserTable.name]
  val password = this[UserTable.password]
  val clientIdentifier = this[UserTable.clientIdentifier]
  val group = this[UserTable.group]
  val registered = parseDateTime(this[UserTable.registered])
  val lastAccessed = parseDateTime(this[UserTable.lastAccessed])
  val knownIPs = this[UserTable.knownIPs]
  return User(id, name, password, clientIdentifier, group, registered, lastAccessed, knownIPs)
}

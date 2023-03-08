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

import org.jetbrains.exposed.sql.Database
import java.util.concurrent.ConcurrentHashMap

object Groups {

  private val groups = ConcurrentHashMap<String, Group>()

  suspend fun load(db: Database) {
    GroupTable.loadGroups(db)
      .associateByTo(groups) { it.name }
  }

  operator fun get(name: String): Group? = groups[name]
}

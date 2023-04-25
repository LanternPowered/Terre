/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.sql

import org.lanternpowered.terre.impl.sql.SqlManagerImpl
import javax.sql.DataSource

interface SqlManager {

  fun dataSource(url: String): DataSource

  fun dataSource(url: String, user: String, password: String): DataSource

  companion object : SqlManager by SqlManagerImpl
}

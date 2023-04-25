/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.sql

import com.zaxxer.hikari.HikariDataSource
import org.lanternpowered.terre.sql.SqlManager
import java.util.Collections
import java.util.WeakHashMap
import javax.sql.DataSource

internal object SqlManagerImpl : SqlManager {

  private val dataSources = Collections.newSetFromMap(WeakHashMap<HikariDataSource, Boolean>())

  override fun dataSource(url: String): DataSource = dataSource(url, "", "")

  override fun dataSource(url: String, user: String, password: String): DataSource {
    var jdbcUrl = url
    if (!jdbcUrl.startsWith("jdbc:"))
      jdbcUrl = "jdbc:$jdbcUrl"
    if (jdbcUrl.startsWith("jdbc:mysql:") && !jdbcUrl.contains("permitMysqlScheme")) {
      jdbcUrl += if (jdbcUrl.contains("?")) "&" else "?"
      jdbcUrl += "permitMysqlScheme"
    }
    val dataSource = HikariDataSource().apply {
      this.jdbcUrl = jdbcUrl
      if (user.isNotEmpty())
        this.username = user
      if (password.isNotEmpty())
        this.password = password
    }
    synchronized(dataSources) {
      dataSources.add(dataSource)
    }
    return dataSource
  }

  fun shutdown() {
    synchronized(dataSources) {
      dataSources.forEach { it.close() }
      dataSources.clear()
    }
  }
}

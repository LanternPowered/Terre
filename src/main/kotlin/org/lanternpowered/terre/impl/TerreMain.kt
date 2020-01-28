/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl

internal fun main(args: Array<String>) {
  System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager")

  val server = ProxyImpl
  server.init()
}

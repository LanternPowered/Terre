/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.util

import java.net.InetSocketAddress
import java.net.URI

internal fun parseInetAddress(address: String): InetSocketAddress {
  val uri = URI.create("tcp://$address")
  check(uri.port != -1) { "Port couldn't be parsed from: $address" }
  return InetSocketAddress.createUnresolved(uri.host, uri.port)
}

internal fun InetSocketAddress.resolve(): InetSocketAddress {
  return InetSocketAddress(this.hostName, this.port)
}

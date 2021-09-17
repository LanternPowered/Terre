/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre

import java.net.SocketAddress

/**
 * Represents the inbound connection of a [Player] from the client to the proxy server.
 */
interface InboundConnection {

  /**
   * The remote address that was used to connect with the proxy server.
   */
  val remoteAddress: SocketAddress

  /**
   * The protocol version used by the inbound connection.
   */
  val protocolVersion: ProtocolVersion
}

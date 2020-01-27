/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network

/**
 * Represents the direction in which the
 * packet is being sent.
 */
enum class PacketDirection {
  /**
   * From the client to the server.
   */
  ClientToServer,
  /**
   * From the server to the client.
   */
  ServerToClient,
}

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

internal interface PacketRegistration<P : Packet> {

  /**
   * The directions applicable to the registration.
   */
  val directions: Set<PacketDirection>

  /**
   * The type of the packet.
   */
  val packetType: Class<P>

  /**
   * The opcode of the registration.
   */
  val opcode: Int
}

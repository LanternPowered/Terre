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

internal class PacketEncoderRegistrationImpl<P : Packet>(
  override val packetType: Class<P>,
  override val opcode: Int,
  override val encoder: PacketEncoder<in P>,
  override val directions: Set<PacketDirection>
) : PacketEncoderRegistration<P>

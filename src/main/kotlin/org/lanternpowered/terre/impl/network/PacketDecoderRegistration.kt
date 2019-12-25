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
 * Represents a registered packet codec.
 */
internal interface PacketDecoderRegistration<P : Packet> : PacketRegistration<P> {

  /**
   * The decoder.
   */
  val decoder: PacketDecoder<out P>
}

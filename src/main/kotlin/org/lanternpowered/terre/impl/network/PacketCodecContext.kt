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

import io.netty.buffer.ByteBufAllocator

internal interface PacketCodecContext : NetworkContext {

  /**
   * The byte buf allocator.
   */
  val byteBufAllocator: ByteBufAllocator

  /**
   * The protocol used during the encoding or decoding.
   *
   * May throw an [IllegalStateException] if the protocol
   * is not yet known.
   */
  val protocol: Protocol
}

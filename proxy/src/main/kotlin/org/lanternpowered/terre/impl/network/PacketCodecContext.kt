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
import org.lanternpowered.terre.impl.network.buffer.PlayerId

internal interface PacketCodecContext : NetworkContext {

  /**
   * The byte buf allocator.
   */
  val byteBufAllocator: ByteBufAllocator
    get() = connection.byteBufAlloc

  /**
   * The protocol used during the encoding or decoding.
   *
   * May throw an [IllegalStateException] if the protocol is not yet known.
   */
  val protocol: Protocol
    get() = connection.protocol

  val nonePlayerId: PlayerId
    get() = connection.nonePlayerId

  /**
   * The direction for which the packet is being encoded or decoded.
   */
  val direction: PacketDirection
}

internal class PacketCodecContextImpl(
  override val connection: Connection,
  override val direction: PacketDirection,
) : PacketCodecContext

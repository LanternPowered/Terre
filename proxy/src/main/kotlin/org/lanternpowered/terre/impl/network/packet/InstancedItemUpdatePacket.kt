/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet

import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.buffer.ItemId
import org.lanternpowered.terre.item.ItemStack
import org.lanternpowered.terre.math.Vec2f

internal data class InstancedItemUpdatePacket(
  val id: ItemId,
  val position: Vec2f,
  val stack: ItemStack,
  val velocity: Vec2f = Vec2f.Zero,
  val noDelay: Boolean = false,
) : Packet

internal val InstancedItemUpdateEncoder = PacketEncoder<InstancedItemUpdatePacket> { buf, packet ->
  buf.writeItemUpdate(packet.id, packet.position, packet.stack, packet.velocity,  packet.noDelay)
}

internal val InstancedItemUpdateDecoder =
  PacketDecoder { buf -> buf.readItemUpdate(::InstancedItemUpdatePacket) }

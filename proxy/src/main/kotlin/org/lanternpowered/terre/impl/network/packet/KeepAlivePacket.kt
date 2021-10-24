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
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.buffer.ItemId
import org.lanternpowered.terre.impl.network.buffer.writeItemId

/**
 * A packet send between the client and server to keep the connection alive.
 */
internal object KeepAlivePacket : Packet

/**
 * The item id used for the keep alive packet.
 *
 * This is not an official packet available in the protocol, but reuses other packet types in
 * specific conditions that don't affect their regular usage.
 */
internal val KeepAliveItemId = ItemId(400)

internal val KeepAliveEncoder = PacketEncoder<KeepAlivePacket> { buf, _ ->
  buf.writeItemId(KeepAliveItemId)
}

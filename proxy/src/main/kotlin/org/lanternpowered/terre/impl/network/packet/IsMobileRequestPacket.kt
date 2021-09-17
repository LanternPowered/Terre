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

internal object IsMobileRequestPacket : Packet

/**
 * The item id used to check whether the client is a mobile client.
 */
internal const val IsMobileItemId = 400

internal val IsMobileRequestEncoder = PacketEncoder<IsMobileRequestPacket> { buf, _ ->
  buf.writeShortLE(IsMobileItemId)
}

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
import org.lanternpowered.terre.util.toString

internal object WorldInfoRequestPacket : Packet {

  override fun toString() = toString {}
}

internal val RequestWorldInfoDecoder = PacketDecoder { WorldInfoRequestPacket }

internal val RequestWorldInfoEncoder = PacketEncoder<WorldInfoRequestPacket> { _, _ -> }

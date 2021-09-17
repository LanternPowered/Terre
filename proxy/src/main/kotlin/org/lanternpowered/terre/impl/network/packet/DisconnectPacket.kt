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
import org.lanternpowered.terre.impl.network.buffer.readPlainText
import org.lanternpowered.terre.impl.network.buffer.writePlainText
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.text.Text

internal data class DisconnectPacket(val reason: Text) : Packet

internal val DisconnectEncoder = PacketEncoder<DisconnectPacket> { buf, packet ->
  buf.writePlainText(packet.reason)
}

internal val DisconnectDecoder = PacketDecoder { buf -> DisconnectPacket(buf.readPlainText()) }

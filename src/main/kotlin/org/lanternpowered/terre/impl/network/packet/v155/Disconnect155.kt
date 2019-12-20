/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet.v155

import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.packet.DisconnectPacket
import org.lanternpowered.terre.impl.network.packetEncoderOf

internal val Disconnect155Encoder = packetEncoderOf<DisconnectPacket> { buf, packet ->
  buf.writeString(packet.reason.toPlain())
}

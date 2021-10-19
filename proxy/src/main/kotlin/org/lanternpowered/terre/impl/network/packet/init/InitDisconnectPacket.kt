/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet.init

import org.lanternpowered.terre.ProtocolVersion
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.packet.DisconnectEncoder
import org.lanternpowered.terre.impl.network.packet.DisconnectPacket
import org.lanternpowered.terre.text.Text

/**
 * A disconnect packet sent during the init phase, when the protocol isn't resolved yet.
 */
internal data class InitDisconnectClientPacket(
  val version: ProtocolVersion,
  val reason: Text
) : Packet

internal val InitDisconnectClientEncoder =
  PacketEncoder<InitDisconnectClientPacket> { packet ->
    val disconnectPacket = DisconnectPacket(packet.reason)
    if (packet.version is ProtocolVersion.Vanilla && packet.version.protocol <= 156) {
      LegacyDisconnectEncoder.encode(this, disconnectPacket)
    } else {
      DisconnectEncoder.encode(this, disconnectPacket)
    }
  }

private val LegacyDisconnectEncoder = PacketEncoder<DisconnectPacket> { buf, packet ->
  buf.writeString(packet.reason.toPlain())
}

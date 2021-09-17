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
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.player.Team
import org.lanternpowered.terre.impl.player.TeamRegistry

internal data class PlayerTeamPacket(
  val playerId: PlayerId,
  val team: Team
) : Packet

internal val PlayerTeamEncoder = PacketEncoder<PlayerTeamPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeByte(packet.team.numericId)
}

internal val PlayerTeamDecoder = PacketDecoder { buf ->
  val playerId = buf.readPlayerId()
  val team = TeamRegistry.require(buf.readByte().toInt())
  PlayerTeamPacket(playerId, team)
}

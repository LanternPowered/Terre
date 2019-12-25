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
import org.lanternpowered.terre.impl.network.buffer.readColor
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.buffer.writeColor
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf
import org.lanternpowered.terre.util.Color

internal data class PlayerInfoPacket(
    val playerId: PlayerId,
    val playerName: String,
    val difficulty: Int,
    val skinVariant: Int,
    val skinColor: Color,
    val hair: Int,
    val hairDye: Int,
    val hairColor: Color,
    val hideVisuals: Int,
    val eyeColor: Color,
    val shirtColor: Color,
    val underShirtColor: Color,
    val pantsColor: Color,
    val shoeColor: Color,
    val extraAccessory: Boolean
) : Packet

internal val PlayerInfoDecoder = packetDecoderOf { buf ->
  val playerId = buf.readPlayerId()
  val skinVariant = buf.readByte().toInt()
  val hair = buf.readByte().toInt()
  val playerName = buf.readString()
  val hairDye = buf.readByte().toInt()
  var hideVisuals = buf.readByte().toInt()
  hideVisuals = hideVisuals or ((buf.readByte().toInt() and 0x3) shl 8)
  hideVisuals = hideVisuals or (buf.readByte().toInt() shl 10)
  val hairColor = buf.readColor()
  val skinColor = buf.readColor()
  val eyeColor = buf.readColor()
  val shirtColor = buf.readColor()
  val underShirtColor = buf.readColor()
  val pantsColor = buf.readColor()
  val shoeColor = buf.readColor()
  val difficultyAndExtraAccessory = buf.readByte().toInt()
  val difficulty = difficultyAndExtraAccessory and 0x3
  val extraAccessory = (difficultyAndExtraAccessory and 0x4) != 0
  PlayerInfoPacket(playerId, playerName, difficulty, skinVariant, skinColor, hair, hairDye,
      hairColor, hideVisuals, eyeColor, shirtColor, underShirtColor, pantsColor, shoeColor, extraAccessory)
}

internal val PlayerInfoEncoder = packetEncoderOf<PlayerInfoPacket> { buf, packet ->
  buf.writePlayerId(packet.playerId)
  buf.writeByte(packet.skinVariant)
  buf.writeByte(packet.hair)
  buf.writeString(packet.playerName)
  buf.writeByte(packet.hairDye)
  val hideVisuals = packet.hideVisuals
  // hideVisuals1 and 2, 10 bits in total
  buf.writeByte(hideVisuals and 0xff)
  buf.writeByte((hideVisuals shr 8) and 0x3)
  // hideMisc, 2 bits, related to light pets
  buf.writeByte(hideVisuals shr 10)
  buf.writeColor(packet.hairColor)
  buf.writeColor(packet.skinColor)
  buf.writeColor(packet.eyeColor)
  buf.writeColor(packet.shirtColor)
  buf.writeColor(packet.underShirtColor)
  buf.writeColor(packet.pantsColor)
  buf.writeColor(packet.shoeColor)
  var difficultyAndExtraAccessory = packet.difficulty
  if (packet.extraAccessory) {
    difficultyAndExtraAccessory = difficultyAndExtraAccessory or 0x4
  }
  buf.writeByte(difficultyAndExtraAccessory)
}

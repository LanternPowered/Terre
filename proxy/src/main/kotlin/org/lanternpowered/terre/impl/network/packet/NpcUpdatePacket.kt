/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("FunctionName")

package org.lanternpowered.terre.impl.network.packet

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.handler.codec.DecoderException
import org.lanternpowered.terre.math.Vec2f
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.PacketCodecContext
import org.lanternpowered.terre.impl.network.buffer.LeftOrRight
import org.lanternpowered.terre.impl.network.buffer.NpcId
import org.lanternpowered.terre.impl.network.buffer.NpcType
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.UpOrDown
import org.lanternpowered.terre.impl.network.buffer.readNpcId
import org.lanternpowered.terre.impl.network.buffer.readPlayerId
import org.lanternpowered.terre.impl.network.buffer.readVec2f
import org.lanternpowered.terre.impl.network.buffer.writeNpcId
import org.lanternpowered.terre.impl.network.buffer.writePlayerId
import org.lanternpowered.terre.impl.network.buffer.writeVec2f
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.buffer.readImmutableBytes
import org.lanternpowered.terre.impl.network.buffer.readVarInt
import org.lanternpowered.terre.impl.network.buffer.writeBytes
import org.lanternpowered.terre.impl.network.buffer.writeVarInt
import org.lanternpowered.terre.util.Bytes

internal data class NpcUpdatePacket(
  val id: NpcId,
  val type: NpcType,
  val position: Vec2f,
  val life: Int?,
  val velocity: Vec2f = Vec2f.Zero,
  val spriteDirection: LeftOrRight = LeftOrRight.Right,
  val direction: LeftOrRight = LeftOrRight.Right,
  val directionY: UpOrDown = UpOrDown.Up,
  val target: PlayerId? = null,
  val ai: NpcAI = NpcAI.None,
  val releaseOwner: PlayerId? = null,
  val playerCountForMultiplayerDifficultyOverride: Int = 1,
  val spawnedFromStatue: Boolean = false,
  val strengthMultiplier: Float = 1f,
  val modData: Bytes = Bytes.Empty,
) : Packet

internal data class NpcAI(
  val ai1: Float,
  val ai2: Float,
  val ai3: Float,
  val ai4: Float,
) {

  companion object {

    val None = NpcAI(0f, 0f, 0f, 0f)
  }
}

internal val NpcUpdateEncoder = PacketEncoder<NpcUpdatePacket> { buf, packet ->
  writeNpcUpdate(buf, packet)
}

private val EmptyNpcModData = Unpooled.buffer().run {
  writeVarInt(0) // empty bits array
  readImmutableBytes()
}

internal fun PacketCodecContext.writeNpcUpdate(
  buf: ByteBuf, packet: NpcUpdatePacket, modded: Boolean = false
) {
  buf.writeNpcId(packet.id)
  buf.writeVec2f(packet.position)
  buf.writeVec2f(packet.velocity)
  val target = packet.target
  var targetId = -1
  if (target != null)
    targetId = (if (target == PlayerId.None) nonePlayerId else target).value
  buf.writeShortLE(targetId)
  var flags = 0
  if (packet.direction.isRight)
    flags += 0x1
  if (packet.directionY.isUp)
    flags += 0x2
  if (packet.spriteDirection.isRight)
    flags += 0x40
  val ai = packet.ai
  if (ai.ai1 != 0f)
    flags += 0x4
  if (ai.ai2 != 0f)
    flags += 0x8
  if (ai.ai3 != 0f)
    flags += 0x10
  if (ai.ai4 != 0f)
    flags += 0x20
  val life = packet.life
  if (life == null)
    flags += 0x80
  if (packet.playerCountForMultiplayerDifficultyOverride != 1)
    flags += 0x01_00
  if (packet.spawnedFromStatue)
    flags += 0x02_00
  if (packet.strengthMultiplier != 1f)
    flags += 0x04_00
  buf.writeByte(flags)
  buf.writeByte(flags shr 8)
  if (ai.ai1 != 0f)
    buf.writeFloatLE(ai.ai1)
  if (ai.ai2 != 0f)
    buf.writeFloatLE(ai.ai2)
  if (ai.ai3 != 0f)
    buf.writeFloatLE(ai.ai3)
  if (ai.ai4 != 0f)
    buf.writeFloatLE(ai.ai4)
  buf.writeShortLE(packet.type.value)
  if (packet.playerCountForMultiplayerDifficultyOverride != 1)
    buf.writeByte(packet.playerCountForMultiplayerDifficultyOverride)
  if (packet.strengthMultiplier != 1f)
    buf.writeFloatLE(packet.strengthMultiplier)
  if (life != null) {
    buf.writeByte(when {
      life <= Byte.MAX_VALUE -> Byte.SIZE_BYTES
      life <= Short.MAX_VALUE -> Short.SIZE_BYTES
      else -> Int.SIZE_BYTES
    })
    when {
      life <= Byte.MAX_VALUE -> buf.writeByte(life)
      life <= Short.MAX_VALUE -> buf.writeShortLE(life)
      else -> buf.writeIntLE(life)
    }
  }
  val releaseOwner = packet.releaseOwner
  if (releaseOwner != null)
    buf.writePlayerId(if (releaseOwner == PlayerId.None) nonePlayerId else releaseOwner)
  if (modded) {
    var modData = packet.modData
    if (modData.isEmpty())
      modData = EmptyNpcModData
    buf.writeVarInt(modData.size)
    buf.writeBytes(modData)
  }
}

internal val NpcUpdateDecoder = PacketDecoder { buf ->
  readNpcUpdate(buf)
}

internal fun PacketCodecContext.readNpcUpdate(
  buf: ByteBuf, modded: Boolean = false
): NpcUpdatePacket {
  val npcId = buf.readNpcId()
  val position = buf.readVec2f()
  val velocity = buf.readVec2f()
  val targetId = buf.readUnsignedShortLE()
  val target = if (targetId == -1) null else {
    val playerId = PlayerId(targetId)
    if (playerId == nonePlayerId) PlayerId.None else playerId
  }
  val flags = buf.readUnsignedShortLE()
  val direction = LeftOrRight((flags and 0x1) != 0)
  val directionY = UpOrDown((flags and 0x2) != 0)
  val spriteDirection = LeftOrRight((flags and 0x40) != 0)
  val ai1 = if ((flags and 0x4) != 0) buf.readFloatLE() else 0f
  val ai2 = if ((flags and 0x8) != 0) buf.readFloatLE() else 0f
  val ai3 = if ((flags and 0x10) != 0) buf.readFloatLE() else 0f
  val ai4 = if ((flags and 0x20) != 0) buf.readFloatLE() else 0f
  val ai = NpcAI(ai1, ai2, ai3, ai4)
  val npcType = NpcType(buf.readShortLE().toInt())
  val playerCountForMultiplayerDifficultyOverride =
    if ((flags and 0x01_00) != 0) buf.readUnsignedByte().toInt() else 1
  val strengthMultiplier = if ((flags and 0x04_00) != 0) buf.readFloatLE() else 1f
  val spawnedFromStatue = (flags and 0x02_00) != 0
  val life = if ((flags and 0x80) == 0) {
    when (val length = buf.readByte().toInt()) {
      Byte.SIZE_BYTES -> buf.readByte().toInt()
      Short.SIZE_BYTES -> buf.readShortLE().toInt()
      Int.SIZE_BYTES -> buf.readIntLE()
      else -> throw DecoderException("Invalid life length: $length")
    }
  } else null
  var hasReleaseOwner = false
  if (modded) {
    // check if the release owner exists before the modded data
    buf.markReaderIndex()
    try {
      if (buf.readVarInt() != buf.readableBytes()) {
        hasReleaseOwner = true
      }
    } catch (ignored: Exception) {
      hasReleaseOwner = true
    } finally {
      buf.resetReaderIndex()
    }
  } else {
    hasReleaseOwner = buf.readableBytes() > 0
  }
  val releaseOwner = if (hasReleaseOwner) {
    val playerId = buf.readPlayerId()
    if (playerId == nonePlayerId) PlayerId.None else playerId
  } else null
  val modData = if (modded) buf.readImmutableBytes(buf.readVarInt()) else Bytes.Empty
  return NpcUpdatePacket(npcId, npcType, position, life, velocity, spriteDirection, direction,
    directionY, target, ai, releaseOwner, playerCountForMultiplayerDifficultyOverride,
    spawnedFromStatue, strengthMultiplier, modData)
}

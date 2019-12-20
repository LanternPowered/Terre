/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("FunctionName", "NOTHING_TO_INLINE")

package org.lanternpowered.terre.impl.network.packet

import io.netty.buffer.ByteBuf
import org.lanternpowered.terre.impl.math.Vec2i
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.readShortVec2i
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.buffer.readUByte
import org.lanternpowered.terre.impl.network.buffer.readUUID
import org.lanternpowered.terre.impl.network.buffer.writeByte
import org.lanternpowered.terre.impl.network.buffer.writeShortVec2i
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.buffer.writeUUID
import org.lanternpowered.terre.impl.network.packetDecoderOf
import org.lanternpowered.terre.impl.network.packetEncoderOf
import org.lanternpowered.terre.util.ToStringHelper
import java.util.Objects
import java.util.UUID

internal data class WorldInfoPacket(
    val id: Int,
    val uniqueId: UUID,
    val name: String,
    val generatorVersion: Long,
    val time: Int,
    val moonPhase: Byte,
    val moonStyle: Byte,
    val size: Vec2i,
    val spawnPosition: Vec2i,
    val surfacePosition: Int,
    val rockLayerPosition: Int,
    val flags: Long,
    val invasionType: Byte,
    val backgroundStyles: WorldBackgroundStyles,
    val treeStyles: WorldZoneBasedStyles,
    val windSpeed: Float,
    val clouds: Byte,
    val rain: Float
) : Packet

internal data class WorldBackgroundStyles(
    val forest: Int,
    val corruption: Int,
    val jungle: Int,
    val snow: Int,
    val hallow: Int,
    val crimson: Int,
    val desert: Int,
    val ocean: Int,
    val iceBack: Int,
    val jungleBack: Int,
    val hellBack: Int,
    val cave: WorldZoneBasedStyles
)

/**
 * Represents a style that is different based
 * on the region in the world.
 *
 * The length of [zonesEndX] will always be
 * the length of [zoneStyles] - 1.
 */
internal class WorldZoneBasedStyles(
    val zonesEndX: IntArray,
    val zoneStyles: ByteArray
) {

  override fun equals(other: Any?): Boolean {
    return other is WorldZoneBasedStyles &&
        other.zonesEndX.contentEquals(this.zonesEndX) &&
        other.zoneStyles.contentEquals(this.zoneStyles)
  }

  override fun hashCode() = Objects.hash(
      this.zonesEndX.contentHashCode(),
      this.zoneStyles.contentHashCode())

  override fun toString() = ToStringHelper(this)
      .add("zonesEndX", this.zonesEndX.joinToString(separator = ", ", prefix = "[", postfix = "]"))
      .add("zoneStyles", this.zoneStyles.joinToString(separator = ", ", prefix = "[", postfix = "]"))
      .toString()
}

internal fun ByteBuf.writeZoneBasedStyles(styles: WorldZoneBasedStyles) {
  check(styles.zonesEndX.size == 3)
  check(styles.zoneStyles.size == 4)
  for (i in styles.zonesEndX)
    writeIntLE(i)
  for (i in styles.zoneStyles)
    writeByte(i)
}

internal fun ByteBuf.readZoneBasedStyles(): WorldZoneBasedStyles {
  val zonesEndX = IntArray(3)
  val zoneStyles = ByteArray(4)
  for (i in zonesEndX.indices)
    zonesEndX[i] = readIntLE()
  for (i in zoneStyles.indices)
    zoneStyles[i] = readByte()
  return WorldZoneBasedStyles(zonesEndX, zoneStyles)
}

internal fun newWorldInfoEncoder(version155: Boolean = false) = packetEncoderOf<WorldInfoPacket> { buf, packet ->
  val flags = packet.flags
  buf.writeIntLE(packet.time)
  buf.writeByte((flags and 0xff).toInt())
  buf.writeByte(packet.moonPhase)
  buf.writeShortVec2i(packet.size)
  buf.writeShortVec2i(packet.spawnPosition)
  buf.writeShortLE(packet.surfacePosition)
  buf.writeShortLE(packet.rockLayerPosition)
  buf.writeIntLE(packet.id)
  buf.writeString(packet.name)
  if (!version155) {
    buf.writeUUID(packet.uniqueId)
    buf.writeLongLE(packet.generatorVersion)
  }
  buf.writeByte(packet.moonStyle)
  val backgroundStyles = packet.backgroundStyles
  buf.writeByte(backgroundStyles.forest)
  buf.writeByte(backgroundStyles.corruption)
  buf.writeByte(backgroundStyles.jungle)
  buf.writeByte(backgroundStyles.snow)
  buf.writeByte(backgroundStyles.hallow)
  buf.writeByte(backgroundStyles.crimson)
  buf.writeByte(backgroundStyles.desert)
  buf.writeByte(backgroundStyles.ocean)
  buf.writeByte(backgroundStyles.iceBack)
  buf.writeByte(backgroundStyles.jungleBack)
  buf.writeByte(backgroundStyles.hellBack)
  buf.writeFloatLE(packet.windSpeed)
  buf.writeByte(packet.clouds)
  buf.writeZoneBasedStyles(packet.treeStyles)
  buf.writeZoneBasedStyles(backgroundStyles.cave)
  buf.writeFloatLE(packet.rain)
  buf.writeByte(((flags ushr 8) and 0xff).toInt())
  buf.writeByte(((flags ushr 16) and 0xff).toInt())
  buf.writeByte(((flags ushr 24) and 0xff).toInt())
  buf.writeByte(((flags ushr 32) and 0xff).toInt())
  buf.writeByte(((flags ushr 40) and 0xff).toInt())
  buf.writeByte(packet.invasionType)
  buf.writeLongLE(0) // Lobby ID -> Only used for Steam integration
}

private val EmptyUUID = UUID(0L, 0L)

internal fun newWorldInfoDecoder(version155: Boolean = false) = packetDecoderOf { buf ->
  val time = buf.readIntLE()
  var flags = buf.readByte().toLong()
  val moonPhase = buf.readByte()
  val size = buf.readShortVec2i()
  val spawnPosition = buf.readShortVec2i()
  val surfacePosition = buf.readShortLE().toInt()
  val rockLayerPosition = buf.readShortLE().toInt()
  val id = buf.readIntLE()
  val name = buf.readString()
  val uniqueId = if (version155) EmptyUUID else buf.readUUID()
  val generatorVersion = if (version155) 0L else buf.readLongLE()
  val moonStyle = buf.readByte()
  val forestBackground = buf.readUByte().toInt()
  val corruptionBackground = buf.readUByte().toInt()
  val jungleBackground = buf.readUByte().toInt()
  val snowBackground = buf.readUByte().toInt()
  val hallowBackground = buf.readUByte().toInt()
  val crimsonBackground = buf.readUByte().toInt()
  val desertBackground = buf.readUByte().toInt()
  val oceanBackground = buf.readUByte().toInt()
  val iceBack = buf.readUByte().toInt()
  val jungleBack = buf.readUByte().toInt()
  val hellBack = buf.readUByte().toInt()
  val windSpeed = buf.readFloatLE()
  val clouds = buf.readByte()
  val treeStyles = buf.readZoneBasedStyles()
  val caveBackgroundStyles = buf.readZoneBasedStyles()
  val backgroundStyles = WorldBackgroundStyles(forestBackground, corruptionBackground, jungleBackground,
      snowBackground, hallowBackground, crimsonBackground, desertBackground, oceanBackground, iceBack,
      jungleBack, hellBack, caveBackgroundStyles)
  val rain = buf.readFloatLE()
  flags += buf.readByte().toLong() shl 8
  flags += buf.readByte().toLong() shl 16
  flags += buf.readByte().toLong() shl 24
  flags += buf.readByte().toLong() shl 32
  flags += buf.readByte().toLong() shl 40
  val invasionType = buf.readByte()
  buf.readLongLE() // Lobby ID -> Only used for Steam integration
  WorldInfoPacket(id, uniqueId, name, generatorVersion, time, moonPhase, moonStyle, size, spawnPosition,
      surfacePosition, rockLayerPosition, flags, invasionType, backgroundStyles, treeStyles, windSpeed, clouds, rain)
}

internal val WorldInfoEncoder = newWorldInfoEncoder()

internal val WorldInfoDecoder = newWorldInfoDecoder()

/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("NOTHING_TO_INLINE")

package org.lanternpowered.terre.impl.network.buffer

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.DecoderException
import org.lanternpowered.terre.impl.text.TextImpl
import org.lanternpowered.terre.impl.text.fromTaggedVanillaText
import org.lanternpowered.terre.impl.text.toPlainVanillaText
import org.lanternpowered.terre.impl.text.toTaggedVanillaText
import org.lanternpowered.terre.math.Vec2f
import org.lanternpowered.terre.math.Vec2i
import org.lanternpowered.terre.text.*
import org.lanternpowered.terre.util.Color
import org.lanternpowered.terre.util.collection.immutableListBuilderOf
import java.util.UUID

/**
 * Reads a string.
 */
internal fun ByteBuf.readString(): String {
  val length = readVarInt()
  val bytes = ByteArray(length)
  readBytes(bytes)
  return String(bytes, Charsets.UTF_8)
}

/**
 * Writes a string.
 */
internal fun ByteBuf.writeString(string: String) = apply {
  val bytes = string.toByteArray(Charsets.UTF_8)
  writeVarInt(bytes.size)
  writeBytes(bytes)
}

/**
 * Reads an int with varint encoding.
 */
internal fun ByteBuf.readVarInt(): Int {
  var value = 0
  var i = 0
  var b: Int
  while (true) {
    b = readByte().toInt()
    if (b and 0x80 == 0) break
    value = value or (b and 0x7F shl i)
    i += 7
    if (i > 35) {
      throw DecoderException("Variable length is too long!")
    }
  }
  return value or (b shl i)
}

/**
 * Writes an int with varint encoding.
 */
internal fun ByteBuf.writeVarInt(value: Int) = apply {
  var v = value
  while ((v.toLong() and 0xFFFFFF80) != 0L) {
    writeByte((v and 0x7F) or 0x80)
    v = v ushr 7
  }
  writeByte(v and 0x7F)
}

/**
 * Reads a color.
 */
internal fun ByteBuf.readColor(): Color {
  val red = readByte()
  val green = readByte()
  val blue = readByte()
  return Color(red, green, blue)
}

/**
 * Writes a color.
 */
internal fun ByteBuf.writeColor(color: Color) = apply {
  writeByte(color.red)
  writeByte(color.green)
  writeByte(color.blue)
}

/**
 * Reads a plain text object.
 */
internal fun ByteBuf.readPlainText(): Text = readRawText()

/**
 * Reads a tagged text object.
 */
internal fun ByteBuf.readTaggedText(): Text =
  (readRawText() as TextImpl).fromTaggedVanillaText()

/**
 * Reads a raw text object.
 */
private fun ByteBuf.readRawText(): Text {
  return when (val type = readByte().toInt()) {
    0 -> {
      val literal = readString()
      textOf(literal)
    }
    1 -> {
      val format = readString()
      val substitutions = readRawTextList()
      formattedTextOf(format, substitutions)
    }
    2 -> {
      val key = readString()
      val substitutions = readRawTextList()
      localizedTextOf(key, substitutions)
    }
    else -> throw DecoderException("Unsupported text type: $type")
  }
}

/**
 * Writes plain text, without any coloring tags.
 */
internal fun ByteBuf.writePlainText(text: Text): ByteBuf {
  return writeRawText((text as TextImpl).toPlainVanillaText())
}

/**
 * Writes text which can contain tags for coloring.
 */
internal fun ByteBuf.writeTaggedText(text: Text): ByteBuf {
  return writeRawText((text as TextImpl).toTaggedVanillaText())
}

/**
 * Writes the text object.
 */
private fun ByteBuf.writeRawText(text: Text): ByteBuf {
  when (text) {
    is LocalizedText -> {
      writeByte(2)
      writeString(text.key)
      writeRawTextList(text.substitutions)
    }
    is FormattableText -> {
      // If there are no substitutions, it's just literal text
      if (text.substitutions.isEmpty()) {
        writeByte(0)
        writeString(text.format)
      } else {
        writeByte(1)
        writeString(text.format)
        writeRawTextList(text.substitutions)
      }
    }
    is LiteralText -> {
      writeByte(0)
      writeString(text.literal)
    }
    is GroupedText -> {
      writeByte(1)
      val children = text.children
      // 3 characters per format for 0 - 9, 4 for 10 - 99
      val formatBuilder = StringBuilder(children.size * if (children.size > 10) 4 else 3)
      for (i in children.indices) {
        formatBuilder.append('{').append(i).append('}')
      }
      writeString(formatBuilder.toString())
      writeRawTextList(children)
    }
    else -> {
      writeByte(0)
      writeString(text.toPlain())
    }
  }
  return this
}

private fun ByteBuf.writeRawTextList(list: List<Text>) {
  writeByte(list.size)
  for (text in list) {
    writeRawText(text)
  }
}

private fun ByteBuf.readRawTextList(): List<Text> {
  val length = readUnsignedByte().toInt()
  val builder = immutableListBuilderOf<Text>(expectedSize = length)
  for (i in 0 until length) {
    builder.add(readRawText())
  }
  return builder.build()
}

/**
 * Reads a player id.
 */
internal inline fun ByteBuf.readPlayerId(): PlayerId =
  PlayerId(readUnsignedByte().toInt())

/**
 * Writes a player id.
 */
internal inline fun ByteBuf.writePlayerId(id: PlayerId): ByteBuf =
  writeByte(id.value)

/**
 * Reads a npc id.
 */
internal inline fun ByteBuf.readNpcId(): NpcId =
  NpcId(readUnsignedShortLE())

/**
 * Writes a npc id.
 */
internal inline fun ByteBuf.writeNpcId(id: NpcId): ByteBuf =
  writeShortLE(id.value)

/**
 * Reads a projectile id.
 */
internal inline fun ByteBuf.readProjectileId(): ProjectileId =
  ProjectileId(readUnsignedShortLE())

/**
 * Writes a projectile id.
 */
internal inline fun ByteBuf.writeProjectileId(id: ProjectileId): ByteBuf =
  writeShortLE(id.value)

/**
 * Reads an item id.
 */
internal inline fun ByteBuf.readItemId(): ItemId =
  ItemId(readUnsignedShortLE())

/**
 * Writes an item id.
 */
internal inline fun ByteBuf.writeItemId(id: ItemId): ByteBuf =
  writeShortLE(id.value)

/**
 * Reads a position where x and y are encoded as short.
 */
internal fun ByteBuf.readShortVec2i(): Vec2i {
  val x = readShortLE().toInt()
  val y = readShortLE().toInt()
  return Vec2i(x, y)
}

/**
 * Writes a position where x and y are encoded as short.
 */
internal fun ByteBuf.writeShortVec2i(vec2i: Vec2i) = apply {
  writeShortLE(vec2i.x)
  writeShortLE(vec2i.y)
}

/**
 * Reads a position where x and y.
 */
internal fun ByteBuf.readVec2i(): Vec2i {
  val x = readIntLE()
  val y = readIntLE()
  return Vec2i(x, y)
}

/**
 * Writes a position where x and y.
 */
internal fun ByteBuf.writeVec2i(vec2i: Vec2i) = apply {
  writeIntLE(vec2i.x)
  writeIntLE(vec2i.y)
}

/**
 * Reads a float vector.
 */
internal fun ByteBuf.readVec2f(): Vec2f {
  val x = readFloatLE()
  val y = readFloatLE()
  return Vec2f(x, y)
}

/**
 * Writes a float vector.
 */
internal fun ByteBuf.writeVec2f(vec2f: Vec2f) = apply {
  writeFloatLE(vec2f.x)
  writeFloatLE(vec2f.y)
}

/**
 * Reads a [UUID].
 */
internal fun ByteBuf.readUUID(): UUID {
  val most = readLong()
  val least = readLong()
  return UUID(most, least)
}

/**
 * Writes a [UUID].
 */
internal fun ByteBuf.writeUUID(uuid: UUID) = apply {
  writeLong(uuid.mostSignificantBits)
  writeLong(uuid.leastSignificantBits)
}

/**
 * Writes a byte.
 */
internal inline fun ByteBuf.writeByte(value: Byte): ByteBuf = writeByte(value.toInt())

/**
 * Reads an unsigned byte.
 */
internal inline fun ByteBuf.readUByte(): UByte = readByte().toUByte()

/**
 * Writes an unsigned byte.
 */
internal inline fun ByteBuf.writeUByte(value: UByte): ByteBuf = writeByte(value.toInt())

/**
 * Writes a short.
 */
internal inline fun ByteBuf.writeShort(value: Short): ByteBuf = writeShort(value.toInt())

/**
 * Writes a short in LE format.
 */
internal inline fun ByteBuf.writeShortLE(value: Short): ByteBuf = writeShortLE(value.toInt())

/**
 * Reads an unsigned short.
 */
internal inline fun ByteBuf.readUShort(): UShort = readShort().toUShort()

/**
 * Writes an unsigned short.
 */
internal inline fun ByteBuf.writeUShort(value: UShort): ByteBuf = writeShort(value.toInt())

/**
 * Reads an unsigned short in LE format.
 */
internal inline fun ByteBuf.readUShortLE(): UShort = readShortLE().toUShort()

/**
 * Writes an unsigned short in LE format.
 */
internal inline fun ByteBuf.writeUShortLE(value: UShort): ByteBuf = writeShortLE(value.toInt())

/**
 * Reads an unsigned int.
 */
internal inline fun ByteBuf.readUInt(): UInt = readInt().toUInt()

/**
 * Writes an unsigned int.
 */
internal inline fun ByteBuf.writeUInt(value: UInt): ByteBuf = writeInt(value.toInt())

/**
 * Reads an unsigned int in LE format.
 */
internal inline fun ByteBuf.readUIntLE(): UInt = readIntLE().toUInt()

/**
 * Writes an unsigned int in LE format.
 */
internal inline fun ByteBuf.writeUIntLE(value: UInt): ByteBuf = writeIntLE(value.toInt())

/**
 * Reads an unsigned long.
 */
internal inline fun ByteBuf.readULong(): ULong = readLong().toULong()

/**
 * Writes an unsigned long.
 */
internal inline fun ByteBuf.writeULong(value: ULong): ByteBuf = writeLong(value.toLong())

/**
 * Reads an unsigned long in LE format.
 */
internal inline fun ByteBuf.readULongLE(): ULong = readLongLE().toULong()

/**
 * Writes an unsigned long in LE format.
 */
internal inline fun ByteBuf.writeULongLE(value: ULong): ByteBuf = writeLongLE(value.toLong())

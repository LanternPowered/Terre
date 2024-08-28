/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet.tmodloader

import com.google.common.io.BaseEncoding
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder

internal data class SyncModsPacket(
  val mods: List<Mod>
) : Packet {

  data class Mod(
    val name: String,
    val version: String,
    val fileHash: String,
    val configs: List<ModConfig>
  )
}

internal val SyncModsEncoder = PacketEncoder<SyncModsPacket> { buf, packet ->
  val mods = packet.mods
  buf.writeIntLE(mods.size)
  for (mod in mods) {
    buf.writeString(mod.name)
    buf.writeString(mod.version)
    buf.writeBytes(BaseEncoding.base16().decode(mod.fileHash))
    val configs = mod.configs
    buf.writeIntLE(configs.size)
    for (config in configs) {
      buf.writeString(config.name)
      buf.writeString(config.content)
    }
  }
}

internal val SyncModsDecoder = PacketDecoder { buf ->
  val modCount = buf.readIntLE()
  val mods = ArrayList<SyncModsPacket.Mod>(modCount)
  repeat(modCount) {
    val name = buf.readString()
    val version = buf.readString()
    val fileHashBytes = ByteArray(20)
    buf.readBytes(fileHashBytes)
    val fileHash = BaseEncoding.base16().encode(fileHashBytes)
    val configCount = buf.readIntLE()
    val configs = ArrayList<ModConfig>(configCount)
    repeat(configCount) {
      val configName = buf.readString()
      val content = buf.readString()
      configs += ModConfig(configName, content)
    }
    mods += SyncModsPacket.Mod(name, version, fileHash, configs)
  }
  SyncModsPacket(mods)
}

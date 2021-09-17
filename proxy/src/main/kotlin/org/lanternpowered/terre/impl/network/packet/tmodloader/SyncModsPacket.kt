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
  val allowVanillaClients: Boolean,
  val mods: List<Mod>
) : Packet {

  data class Mod(
    val name: String,
    val version: String,
    val fileHash: String,
    val fileValidModBrowserSignature: Boolean,
    val configs: List<Config>
  ) {

    data class Config(
      val name: String,
      val content: String
    )
  }
}

internal val SyncModsEncoder = PacketEncoder<SyncModsPacket> { buf, packet ->
  buf.writeBoolean(packet.allowVanillaClients)
  val mods = packet.mods
  buf.writeIntLE(mods.size)
  for (mod in mods) {
    buf.writeString(mod.name)
    buf.writeString(mod.version)
    buf.writeBytes(BaseEncoding.base16().decode(mod.fileHash))
    buf.writeBoolean(mod.fileValidModBrowserSignature)
    val configs = mod.configs
    buf.writeIntLE(configs.size)
    for (config in configs) {
      buf.writeString(config.name)
      buf.writeString(config.content)
    }
  }
}

internal val SyncModsDecoder = PacketDecoder { buf ->
  val allowVanillaClients = buf.readBoolean()
  val modCount = buf.readIntLE()
  val mods = mutableListOf<SyncModsPacket.Mod>()
  repeat(modCount) {
    val name = buf.readString()
    val version = buf.readString()
    val fileHashBytes = ByteArray(20)
    buf.readBytes(fileHashBytes)
    val fileHash = BaseEncoding.base16().encode(fileHashBytes)
    val fileValidModBrowserSignature = buf.readBoolean()
    val configCount = buf.readIntLE()
    val configs = mutableListOf<SyncModsPacket.Mod.Config>()
    repeat(configCount) {
      val configName = buf.readString()
      val content = buf.readString()
      configs += SyncModsPacket.Mod.Config(configName, content)
    }
    mods += SyncModsPacket.Mod(name, version, fileHash, fileValidModBrowserSignature, configs)
  }
  SyncModsPacket(allowVanillaClients, mods)
}

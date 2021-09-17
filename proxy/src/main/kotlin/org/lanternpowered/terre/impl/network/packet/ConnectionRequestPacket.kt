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

import io.netty.handler.codec.DecoderException
import org.lanternpowered.terre.ProtocolVersion
import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.ProtocolVersions
import org.lanternpowered.terre.impl.network.buffer.readString
import org.lanternpowered.terre.impl.network.buffer.writeString
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.util.Version

internal data class ConnectionRequestPacket(val version: ProtocolVersion) : Packet

private const val vanillaVersionPrefix = "Terraria"

private const val tModLoaderVersionPrefix = "tModLoader"
private val tModLoaderVersionRegex =
  "^$tModLoaderVersionPrefix v([0-9.]*)(?: ([^\\s]*))?(?: Beta ([0-9]*))?\$".toRegex()

internal val ConnectionRequestDecoder = PacketDecoder { buf ->
  val value = buf.readString()

  val clientVersion = run {
    if (value.startsWith(vanillaVersionPrefix)) {
      val protocol = value.substring(vanillaVersionPrefix.length).toInt()
      ProtocolVersions[protocol] ?: ProtocolVersion.Vanilla(Version(0), protocol)
    } else if (value.startsWith(tModLoaderVersionPrefix)) {
      val result = tModLoaderVersionRegex.matchEntire(value)
      if (result != null) {
        val version = Version(result.groupValues[1])
        val branch = result.groups[2]?.value
        val beta = result.groups[3]?.value?.toInt()
        ProtocolVersion.TModLoader(version, branch, beta)
      } else {
        throw DecoderException("Invalid tModLoader client version: $value")
      }
    } else throw DecoderException("Unsupported client: $value")
  }

  ConnectionRequestPacket(clientVersion)
}

internal val ConnectionRequestEncoder = PacketEncoder<ConnectionRequestPacket> { buf, packet ->
  val value = when (val version = packet.version) {
    is ProtocolVersion.Vanilla -> "$vanillaVersionPrefix${version.protocol}"
    is ProtocolVersion.TModLoader -> {
      val builder = StringBuilder("$tModLoaderVersionPrefix v${version.version}")
      if (version.branch != null)
        builder.append(" ${version.branch}")
      if (version.beta != null)
        builder.append(" Beta ${version.beta}")
      builder.toString()
    }
  }
  buf.writeString(value)
}

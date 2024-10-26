/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.addon

import io.netty.buffer.Unpooled
import org.lanternpowered.terre.impl.network.buffer.readString
import java.io.InputStream

class Mod(
  val name: String,
  val version: String,
  val tModLoaderVersion: String,
  val hash: ByteArray,
  val data: ByteArray
) {

  companion object {

    fun load(inputStream: InputStream): Mod {
      val data = inputStream.readAllBytes()
      val buf = Unpooled.wrappedBuffer(data)

      val header = ByteArray(4)
      buf.readBytes(header)
      if (!"TMOD".toByteArray(Charsets.US_ASCII).contentEquals(header)) {
        error("Not a mod file.")
      }
      val tModLoaderVersion = buf.readString()

      val hash = ByteArray(20)
      buf.readBytes(hash)

      buf.skipBytes(256) // signature
      buf.readIntLE() // data length for following data

      val name = buf.readString()
      val version = buf.readString()

      return Mod(name, version, tModLoaderVersion, hash, data)
    }
  }
}

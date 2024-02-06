/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.backend

import org.lanternpowered.terre.ProtocolVersion
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.packet.tmodloader.ModDataPacket
import org.lanternpowered.terre.impl.network.packet.tmodloader.SyncModsPacket
import org.lanternpowered.terre.text.Text

/**
 * Represents the result of a [ServerInitConnectionHandler].
 */
internal sealed class ServerInitConnectionResult {

  data class Success(
    val playerId: PlayerId,
    val syncModsPacket: SyncModsPacket? = null,
    val syncModNetIdsPacket: ModDataPacket? = null,
  ) : ServerInitConnectionResult()

  data class Disconnected(val reason: Text?) : ServerInitConnectionResult()

  data class UnsupportedProtocol(val reason: Text?) : ServerInitConnectionResult()

  data class NotModded(val reason: Text?) : ServerInitConnectionResult()

  data class TModLoaderVersionMismatch(
    val version: ProtocolVersion.TModLoader
  ) : ServerInitConnectionResult()

  data class TModLoaderClientExpected(val reason: Text?) : ServerInitConnectionResult()
}

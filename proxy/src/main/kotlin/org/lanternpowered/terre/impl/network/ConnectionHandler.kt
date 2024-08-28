/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network

import io.netty.buffer.ByteBuf
import org.lanternpowered.terre.impl.network.packet.AddPlayerBuffPacket
import org.lanternpowered.terre.impl.network.packet.ChatMessagePacket
import org.lanternpowered.terre.impl.network.packet.ClientUniqueIdPacket
import org.lanternpowered.terre.impl.network.packet.CompleteConnectionPacket
import org.lanternpowered.terre.impl.network.packet.ConnectionApprovedPacket
import org.lanternpowered.terre.impl.network.packet.ConnectionRequestPacket
import org.lanternpowered.terre.impl.network.packet.CustomPayloadPacket
import org.lanternpowered.terre.impl.network.packet.DisconnectPacket
import org.lanternpowered.terre.impl.network.packet.ItemRemoveOwnerPacket
import org.lanternpowered.terre.impl.network.packet.ItemUpdateOwnerPacket
import org.lanternpowered.terre.impl.network.packet.ItemUpdatePacket
import org.lanternpowered.terre.impl.network.packet.NpcUpdatePacket
import org.lanternpowered.terre.impl.network.packet.PasswordRequestPacket
import org.lanternpowered.terre.impl.network.packet.PasswordResponsePacket
import org.lanternpowered.terre.impl.network.packet.PlayerActivePacket
import org.lanternpowered.terre.impl.network.packet.PlayerChatMessagePacket
import org.lanternpowered.terre.impl.network.packet.PlayerCommandPacket
import org.lanternpowered.terre.impl.network.packet.PlayerDeathPacket
import org.lanternpowered.terre.impl.network.packet.PlayerHealthPacket
import org.lanternpowered.terre.impl.network.packet.PlayerInfoPacket
import org.lanternpowered.terre.impl.network.packet.PlayerInventorySlotPacket
import org.lanternpowered.terre.impl.network.packet.PlayerPvPPacket
import org.lanternpowered.terre.impl.network.packet.PlayerSpawnPacket
import org.lanternpowered.terre.impl.network.packet.PlayerTeamPacket
import org.lanternpowered.terre.impl.network.packet.PlayerUpdatePacket
import org.lanternpowered.terre.impl.network.packet.ProjectileDestroyPacket
import org.lanternpowered.terre.impl.network.packet.ProjectileUpdatePacket
import org.lanternpowered.terre.impl.network.packet.StatusPacket
import org.lanternpowered.terre.impl.network.packet.TeleportPylonPacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoPacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoRequestPacket
import org.lanternpowered.terre.impl.network.packet.tmodloader.ModDataPacket
import org.lanternpowered.terre.impl.network.packet.tmodloader.ModFileRequestPacket
import org.lanternpowered.terre.impl.network.packet.tmodloader.ModFileResponsePacket
import org.lanternpowered.terre.impl.network.packet.tmodloader.SyncModsDonePacket
import org.lanternpowered.terre.impl.network.packet.tmodloader.SyncModsPacket

internal interface ConnectionHandler {

  fun initialize()

  fun disconnect()

  fun exception(throwable: Throwable)

  fun afterWrite(packet: Any) {
  }

  fun handle(packet: CustomPayloadPacket): Boolean {
    return false
  }

  fun handle(packet: SyncModsPacket): Boolean {
    return false
  }

  fun handle(packet: SyncModsDonePacket): Boolean {
    return false
  }

  fun handle(packet: ModFileRequestPacket): Boolean {
    return false
  }

  fun handle(packet: ModFileResponsePacket): Boolean {
    return false
  }

  fun handle(packet: ModDataPacket): Boolean {
    return false
  }

  fun handle(packet: ChatMessagePacket): Boolean {
    return false
  }

  fun handle(packet: ClientUniqueIdPacket): Boolean {
    return false
  }

  fun handle(packet: CompleteConnectionPacket): Boolean {
    return false
  }

  fun handle(packet: ConnectionApprovedPacket): Boolean {
    return false
  }

  fun handle(packet: ConnectionRequestPacket): Boolean {
    return false
  }

  fun handle(packet: DisconnectPacket): Boolean {
    return false
  }

  fun handle(packet: PasswordRequestPacket): Boolean {
    return false
  }

  fun handle(packet: PasswordResponsePacket): Boolean {
    return false
  }

  fun handle(packet: PlayerActivePacket): Boolean {
    return false
  }

  fun handle(packet: PlayerChatMessagePacket): Boolean {
    return false
  }

  suspend fun handle(packet: PlayerCommandPacket): Boolean {
    return false
  }

  fun handle(packet: PlayerHealthPacket): Boolean {
    return false
  }

  fun handle(packet: PlayerDeathPacket): Boolean {
    return false
  }

  fun handle(packet: PlayerInfoPacket): Boolean {
    return false
  }

  fun handle(packet: PlayerInventorySlotPacket): Boolean {
    return false
  }

  fun handle(packet: StatusPacket): Boolean {
    return false
  }

  fun handle(packet: ItemUpdateOwnerPacket): Boolean {
    return false
  }

  fun handle(packet: ItemRemoveOwnerPacket): Boolean {
    return false
  }

  fun handle(packet: NpcUpdatePacket): Boolean {
    return false
  }

  fun handle(packet: WorldInfoPacket): Boolean {
    return false
  }

  fun handle(packet: WorldInfoRequestPacket): Boolean {
    return false
  }

  fun handle(packet: AddPlayerBuffPacket): Boolean {
    return false
  }

  fun handle(packet: PlayerSpawnPacket): Boolean {
    return false
  }

  fun handle(packet: PlayerUpdatePacket): Boolean {
    return false
  }

  fun handle(packet: ProjectileDestroyPacket): Boolean {
    return false
  }

  fun handle(packet: ProjectileUpdatePacket): Boolean {
    return false
  }

  fun handle(packet: PlayerTeamPacket): Boolean {
    return false
  }

  fun handle(packet: PlayerPvPPacket): Boolean {
    return false
  }

  fun handle(packet: ItemUpdatePacket): Boolean {
    return false
  }

  fun handle(packet: TeleportPylonPacket): Boolean {
    return false
  }

  fun handleGeneric(packet: Packet)

  fun handleUnknown(packet: ByteBuf)
}

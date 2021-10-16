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

import org.lanternpowered.terre.impl.network.packet.*
import org.lanternpowered.terre.impl.network.packet.tmodloader.ModDataDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.ModDataEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.ModFileRequestDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.ModFileRequestEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.SyncModsDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.SyncModsDoneDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.SyncModsDoneEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.SyncModsEncoder
import org.lanternpowered.terre.impl.network.packet.v194.*

internal val Protocol194 = multistateProtocol("194") {
  bind(0x01, ConnectionRequestEncoder, ConnectionRequestDecoder, PacketDirection.ClientToServer)
  bind(0x02, DisconnectEncoder, DisconnectDecoder, PacketDirection.ServerToClient)
  bind(0x03, ConnectionApprovedEncoder, ConnectionApprovedDecoder, PacketDirection.ServerToClient)
  bind(0x04, PlayerInfoEncoder, PlayerInfoDecoder)
  bind(0x06, RequestWorldInfoEncoder, RequestWorldInfoDecoder, PacketDirection.ClientToServer)
  bind(0x25, PasswordRequestEncoder, PasswordRequestDecoder, PacketDirection.ServerToClient)
  bind(0x26, PasswordResponseEncoder, PasswordResponseDecoder, PacketDirection.ClientToServer)
  bind(0x44, ClientUniqueIdEncoder, ClientUniqueIdDecoder, PacketDirection.ClientToServer)

  // tModLoader
  bind(0xFA, ModDataEncoder, ModDataDecoder, PacketDirection.ClientToServer)
  bind(0xFB, SyncModsEncoder, SyncModsDecoder, PacketDirection.ServerToClient)
  bind(0xFB, SyncModsDoneEncoder, SyncModsDoneDecoder, PacketDirection.ClientToServer)
  bind(0xFC, ModFileRequestEncoder, ModFileRequestDecoder, PacketDirection.ClientToServer)

  init {
    bind(0x16, IsMobileResponseDecoder, PacketDirection.ClientToServer)
    bind(0x27, IsMobileRequestEncoder, PacketDirection.ServerToClient)
  }

  play {
    bind(0x05, PlayerInventorySlot194Encoder, PlayerInventorySlot194Decoder)
    bind(0x07, WorldInfo194Encoder, WorldInfo194Decoder, PacketDirection.ServerToClient)
    bind(0x08, EssentialTilesRequestEncoder, PacketDirection.ClientToServer)
    bind(0x09, Status194Encoder, Status194Decoder, PacketDirection.ServerToClient)
    bind(0x0C, PlayerSpawn194Encoder, PlayerSpawn194Decoder)
    bind(0x0D, PlayerUpdate194Encoder, PlayerUpdate194Decoder)
    bind(0x0E, PlayerActiveEncoder, PlayerActiveDecoder, PacketDirection.ServerToClient)
    bind(0x10, PlayerHealthEncoder, PlayerHealthDecoder)
    bind(0x16, UpdateItemOwnerEncoder)
    bind(0x16, UpdateItemOwnerDecoder) // And keep alive
    bind(0x17, UpdateNpc194Encoder, UpdateNpc194Decoder)
    bind(0x1B, ProjectileUpdate194Encoder, ProjectileUpdate194Decoder)
    bind(0x1D, ProjectileDestroyEncoder, ProjectileDestroyDecoder)
    bind(0x27, KeepAliveEncoder, PacketDirection.ServerToClient)
    bind(0x2A, PlayerManaEncoder, PlayerManaDecoder)
    bind(0x2D, PlayerTeamEncoder, PlayerTeamDecoder, PacketDirection.ServerToClient)
    bind(0x31, CompleteConnectionEncoder, CompleteConnectionDecoder, PacketDirection.ServerToClient)
    bind(0x32, PlayerBuffsEncoder, PlayerBuffsDecoder)
    bind(0x37, AddPlayerBuff194Encoder, AddPlayerBuff194Decoder)
    bind(0x38, UpdateNpcName194Encoder, UpdateNpcName194Decoder, PacketDirection.ServerToClient)
    bind(0x43, CustomPayloadEncoder, CustomPayloadDecoder)
    bind(0x51, CombatMessageEncoder, CombatMessageDecoder, PacketDirection.ServerToClient)
    bind(0x5B, SpeechBubble194Encoder)
    bind(0x60, PlayerTeleportThroughPortalEncoder, PlayerTeleportThroughPortalDecoder)
    // bind(0x66, NebulaLevelUpRequestEncoder, PacketDirection.ServerToClient)
    bind(0x6B, ChatMessageEncoder, ChatMessageDecoder, PacketDirection.ServerToClient)
    bind(0x75, PlayerHurtEncoder, PlayerHurtDecoder)
    bind(0x76, PlayerDeathEncoder, PlayerDeathDecoder)

    bind(0x01FF, PlayerCommandEncoder, PlayerCommandDecoder, PacketDirection.ClientToServer)
    bind(0x01FF, PlayerChatMessageEncoder, PlayerChatMessageDecoder, PacketDirection.ServerToClient)
  }
}

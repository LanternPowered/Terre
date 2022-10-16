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

import org.lanternpowered.terre.impl.network.packet.AddPlayerBuffDecoder
import org.lanternpowered.terre.impl.network.packet.AddPlayerBuffEncoder
import org.lanternpowered.terre.impl.network.packet.ChatMessageDecoder
import org.lanternpowered.terre.impl.network.packet.ChatMessageEncoder
import org.lanternpowered.terre.impl.network.packet.ClientUniqueIdDecoder
import org.lanternpowered.terre.impl.network.packet.ClientUniqueIdEncoder
import org.lanternpowered.terre.impl.network.packet.CombatMessageDecoder
import org.lanternpowered.terre.impl.network.packet.CombatMessageEncoder
import org.lanternpowered.terre.impl.network.packet.CompleteConnectionDecoder
import org.lanternpowered.terre.impl.network.packet.CompleteConnectionEncoder
import org.lanternpowered.terre.impl.network.packet.ConnectionRequestDecoder
import org.lanternpowered.terre.impl.network.packet.ConnectionRequestEncoder
import org.lanternpowered.terre.impl.network.packet.CustomPayloadDecoder
import org.lanternpowered.terre.impl.network.packet.CustomPayloadEncoder
import org.lanternpowered.terre.impl.network.packet.DisconnectDecoder
import org.lanternpowered.terre.impl.network.packet.DisconnectEncoder
import org.lanternpowered.terre.impl.network.packet.EssentialTilesRequestEncoder
import org.lanternpowered.terre.impl.network.packet.InstancedItemUpdateDecoder
import org.lanternpowered.terre.impl.network.packet.ClientPlayerLimitRequestEncoder
import org.lanternpowered.terre.impl.network.packet.ClientPlayerLimitResponseDecoder
import org.lanternpowered.terre.impl.network.packet.ItemUpdateEncoder
import org.lanternpowered.terre.impl.network.packet.ItemUpdateOwnerDecoder
import org.lanternpowered.terre.impl.network.packet.ItemUpdateOwnerEncoder
import org.lanternpowered.terre.impl.network.packet.KeepAliveEncoder
import org.lanternpowered.terre.impl.network.packet.NpcUpdateDecoder
import org.lanternpowered.terre.impl.network.packet.NpcUpdateEncoder
import org.lanternpowered.terre.impl.network.packet.PasswordRequestDecoder
import org.lanternpowered.terre.impl.network.packet.PasswordRequestEncoder
import org.lanternpowered.terre.impl.network.packet.PasswordResponseDecoder
import org.lanternpowered.terre.impl.network.packet.PasswordResponseEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerActiveDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerActiveEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerBuffsDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerBuffsEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerChatMessageDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerChatMessageEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerCommandDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerCommandEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerDeathDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerDeathEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerHealthDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerHealthEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerHurtDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerHurtEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerInfoDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerInfoEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerInventorySlotDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerInventorySlotEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerManaDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerManaEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerTeamDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerTeamEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerTeleportThroughPortalDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerTeleportThroughPortalEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerUpdateDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerUpdateEncoder
import org.lanternpowered.terre.impl.network.packet.ProjectileDestroyDecoder
import org.lanternpowered.terre.impl.network.packet.ProjectileDestroyEncoder
import org.lanternpowered.terre.impl.network.packet.ProjectileUpdateDecoder
import org.lanternpowered.terre.impl.network.packet.ProjectileUpdateEncoder
import org.lanternpowered.terre.impl.network.packet.RequestWorldInfoDecoder
import org.lanternpowered.terre.impl.network.packet.RequestWorldInfoEncoder
import org.lanternpowered.terre.impl.network.packet.SimpleItemUpdateDecoder
import org.lanternpowered.terre.impl.network.packet.SpeechBubbleEncoder
import org.lanternpowered.terre.impl.network.packet.StatusDecoder
import org.lanternpowered.terre.impl.network.packet.StatusEncoder
import org.lanternpowered.terre.impl.network.packet.TileSquareDecoder
import org.lanternpowered.terre.impl.network.packet.TileSquareEncoder
import org.lanternpowered.terre.impl.network.packet.WorldInfoDecoder
import org.lanternpowered.terre.impl.network.packet.WorldInfoEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.ModDataDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.ModDataEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.ModFileRequestDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.ModFileRequestEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.SyncModsDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.SyncModsDoneDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.SyncModsDoneEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.SyncModsEncoder
import org.lanternpowered.terre.impl.network.packet.v238.ConnectionApproved238Decoder
import org.lanternpowered.terre.impl.network.packet.v238.ConnectionApproved238Encoder
import org.lanternpowered.terre.impl.network.packet.v238.PlayerSpawn238Decoder
import org.lanternpowered.terre.impl.network.packet.v238.PlayerSpawn238Encoder

internal val Protocol238 = multistateProtocol("238") {
  bind(0x01, ConnectionRequestEncoder, ConnectionRequestDecoder, PacketDirection.ClientToServer)
  bind(0x02, DisconnectEncoder, DisconnectDecoder, PacketDirection.ServerToClient)
  bind(0x03, ConnectionApproved238Encoder, ConnectionApproved238Decoder, PacketDirection.ServerToClient)
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
    bind(0x16, ClientPlayerLimitResponseDecoder, PacketDirection.ClientToServer)
    bind(0x27, ClientPlayerLimitRequestEncoder, PacketDirection.ServerToClient)
  }

  play {
    bind(0x05, PlayerInventorySlotEncoder, PlayerInventorySlotDecoder)
    bind(0x07, WorldInfoEncoder, WorldInfoDecoder, PacketDirection.ServerToClient)
    bind(0x08, EssentialTilesRequestEncoder, PacketDirection.ClientToServer)
    bind(0x09, StatusEncoder, StatusDecoder, PacketDirection.ServerToClient)
    bind(0x0C, PlayerSpawn238Encoder, PlayerSpawn238Decoder)
    bind(0x0D, PlayerUpdateEncoder, PlayerUpdateDecoder)
    bind(0x0E, PlayerActiveEncoder, PlayerActiveDecoder, PacketDirection.ServerToClient)
    bind(0x10, PlayerHealthEncoder, PlayerHealthDecoder)
    bind(0x14, TileSquareEncoder, TileSquareDecoder)
    bind(0x15, ItemUpdateEncoder, SimpleItemUpdateDecoder)
    bind(0x16, ItemUpdateOwnerEncoder)
    bind(0x16, ItemUpdateOwnerDecoder) // And keep alive
    bind(0x17, NpcUpdateEncoder, NpcUpdateDecoder, PacketDirection.ServerToClient)
    bind(0x1B, ProjectileUpdateEncoder, ProjectileUpdateDecoder)
    bind(0x1D, ProjectileDestroyEncoder, ProjectileDestroyDecoder)
    bind(0x27, KeepAliveEncoder, PacketDirection.ServerToClient)
    bind(0x2A, PlayerManaEncoder, PlayerManaDecoder)
    bind(0x2D, PlayerTeamEncoder, PlayerTeamDecoder, PacketDirection.ServerToClient)
    bind(0x31, CompleteConnectionEncoder, CompleteConnectionDecoder, PacketDirection.ServerToClient)
    bind(0x32, PlayerBuffsEncoder, PlayerBuffsDecoder)
    bind(0x37, AddPlayerBuffEncoder, AddPlayerBuffDecoder)
    bind(0x43, CustomPayloadEncoder, CustomPayloadDecoder)
    bind(0x51, CombatMessageEncoder, CombatMessageDecoder, PacketDirection.ServerToClient) // TODO
    bind(0x5A, ItemUpdateEncoder, InstancedItemUpdateDecoder)
    bind(0x5B, SpeechBubbleEncoder)
    bind(0x60, PlayerTeleportThroughPortalEncoder, PlayerTeleportThroughPortalDecoder)
    // bind(0x66, NebulaLevelUpRequestEncoder, PacketDirection.ServerToClient)
    bind(0x6B, ChatMessageEncoder, ChatMessageDecoder, PacketDirection.ServerToClient)
    bind(0x75, PlayerHurtEncoder, PlayerHurtDecoder)
    bind(0x76, PlayerDeathEncoder, PlayerDeathDecoder)

    bind(0x01FF, PlayerCommandEncoder, PlayerCommandDecoder, PacketDirection.ClientToServer)
    bind(0x01FF, PlayerChatMessageEncoder, PlayerChatMessageDecoder, PacketDirection.ServerToClient)
  }
}

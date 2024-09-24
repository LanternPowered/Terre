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
import org.lanternpowered.terre.impl.network.packet.ItemRemoveOwnerDecoder
import org.lanternpowered.terre.impl.network.packet.ItemRemoveOwnerEncoder
import org.lanternpowered.terre.impl.network.packet.ItemUpdateEncoder
import org.lanternpowered.terre.impl.network.packet.ItemUpdateOwnerDecoder
import org.lanternpowered.terre.impl.network.packet.ItemUpdateOwnerEncoder
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
import org.lanternpowered.terre.impl.network.packet.PlayerHealthDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerHealthEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerInfoDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerInfoEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerInventorySlotDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerInventorySlotEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerManaDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerManaEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerPvPDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerPvPEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerTeamDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerTeamEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerUpdateDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerUpdateEncoder
import org.lanternpowered.terre.impl.network.packet.ProjectileDestroyDecoder
import org.lanternpowered.terre.impl.network.packet.ProjectileDestroyEncoder
import org.lanternpowered.terre.impl.network.packet.ProjectileUpdateDecoder
import org.lanternpowered.terre.impl.network.packet.ProjectileUpdateEncoder
import org.lanternpowered.terre.impl.network.packet.RealIPEncoder
import org.lanternpowered.terre.impl.network.packet.RequestWorldInfoDecoder
import org.lanternpowered.terre.impl.network.packet.RequestWorldInfoEncoder
import org.lanternpowered.terre.impl.network.packet.SimpleItemUpdateDecoder
import org.lanternpowered.terre.impl.network.packet.SpeechBubbleEncoder
import org.lanternpowered.terre.impl.network.packet.StatusDecoder
import org.lanternpowered.terre.impl.network.packet.StatusEncoder
import org.lanternpowered.terre.impl.network.packet.TeleportPylonDecoder
import org.lanternpowered.terre.impl.network.packet.TeleportPylonEncoder
import org.lanternpowered.terre.impl.network.packet.TileSquareDecoder
import org.lanternpowered.terre.impl.network.packet.TileSquareEncoder
import org.lanternpowered.terre.impl.network.packet.WorldInfoDecoder
import org.lanternpowered.terre.impl.network.packet.WorldInfoEncoder
import org.lanternpowered.terre.impl.network.packet.v238.ConnectionApproved238Decoder
import org.lanternpowered.terre.impl.network.packet.v238.ConnectionApproved238Encoder
import org.lanternpowered.terre.impl.network.packet.v238.PlayerSpawn238Decoder
import org.lanternpowered.terre.impl.network.packet.v238.PlayerSpawn238Encoder

internal val Protocol248 = protocol("248") {
  bind(1, ConnectionRequestEncoder, ConnectionRequestDecoder, PacketDirection.ClientToServer)
  bind(2, DisconnectEncoder, DisconnectDecoder, PacketDirection.ServerToClient)
  bind(3, ConnectionApproved238Encoder, ConnectionApproved238Decoder, PacketDirection.ServerToClient)
  bind(4, PlayerInfoEncoder, PlayerInfoDecoder)
  bind(5, PlayerInventorySlotEncoder, PlayerInventorySlotDecoder)
  bind(6, RequestWorldInfoEncoder, RequestWorldInfoDecoder, PacketDirection.ClientToServer)
  bind(7, WorldInfoEncoder, WorldInfoDecoder, PacketDirection.ServerToClient)
  bind(8, EssentialTilesRequestEncoder, PacketDirection.ClientToServer)
  bind(9, StatusEncoder, StatusDecoder, PacketDirection.ServerToClient)
  bind(12, PlayerSpawn238Encoder, PlayerSpawn238Decoder)
  bind(13, PlayerUpdateEncoder, PlayerUpdateDecoder)
  bind(14, PlayerActiveEncoder, PlayerActiveDecoder, PacketDirection.ServerToClient)
  bind(16, PlayerHealthEncoder, PlayerHealthDecoder)
  bind(20, TileSquareEncoder, TileSquareDecoder)
  bind(21, ItemUpdateEncoder, SimpleItemUpdateDecoder)
  bind(22, ItemUpdateOwnerEncoder, ItemUpdateOwnerDecoder)
  bind(23, NpcUpdateEncoder, NpcUpdateDecoder, PacketDirection.ServerToClient)
  bind(27, ProjectileUpdateEncoder, ProjectileUpdateDecoder)
  bind(29, ProjectileDestroyEncoder, ProjectileDestroyDecoder)
  bind(30, PlayerPvPEncoder, PlayerPvPDecoder)
  bind(37, PasswordRequestEncoder, PasswordRequestDecoder, PacketDirection.ServerToClient)
  bind(38, PasswordResponseEncoder, PasswordResponseDecoder, PacketDirection.ClientToServer)
  bind(39, ItemRemoveOwnerEncoder, ItemRemoveOwnerDecoder)
  bind(42, PlayerManaEncoder, PlayerManaDecoder)
  bind(45, PlayerTeamEncoder, PlayerTeamDecoder)
  bind(49, CompleteConnectionEncoder, CompleteConnectionDecoder, PacketDirection.ServerToClient)
  bind(50, PlayerBuffsEncoder, PlayerBuffsDecoder)
  bind(55, AddPlayerBuffEncoder, AddPlayerBuffDecoder)
  bind(67, RealIPEncoder, PacketDirection.ClientToServer)
  bind(67, CustomPayloadEncoder, CustomPayloadDecoder)
  bind(68, ClientUniqueIdEncoder, ClientUniqueIdDecoder, PacketDirection.ClientToServer)
  bind(90, ItemUpdateEncoder, InstancedItemUpdateDecoder)
  bind(91, SpeechBubbleEncoder, PacketDirection.ServerToClient)
  bind(107, ChatMessageEncoder, ChatMessageDecoder, PacketDirection.ServerToClient)
  bind(119, CombatMessageEncoder, PacketDirection.ServerToClient)

  // modules
  bind(0x01FF, PlayerCommandEncoder, PlayerCommandDecoder, PacketDirection.ClientToServer)
  bind(0x01FF, PlayerChatMessageEncoder, PlayerChatMessageDecoder, PacketDirection.ServerToClient)
  bind(0x08FF, TeleportPylonEncoder, TeleportPylonDecoder, PacketDirection.ServerToClient)
}

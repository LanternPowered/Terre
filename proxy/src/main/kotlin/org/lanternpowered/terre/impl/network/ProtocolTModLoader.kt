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
import org.lanternpowered.terre.impl.network.packet.ConnectionApprovedDecoder
import org.lanternpowered.terre.impl.network.packet.ConnectionApprovedEncoder
import org.lanternpowered.terre.impl.network.packet.ConnectionRequestDecoder
import org.lanternpowered.terre.impl.network.packet.ConnectionRequestEncoder
import org.lanternpowered.terre.impl.network.packet.CustomPayloadDecoder
import org.lanternpowered.terre.impl.network.packet.CustomPayloadEncoder
import org.lanternpowered.terre.impl.network.packet.DisconnectDecoder
import org.lanternpowered.terre.impl.network.packet.DisconnectEncoder
import org.lanternpowered.terre.impl.network.packet.EssentialTilesRequestEncoder
import org.lanternpowered.terre.impl.network.packet.HatRackItemDecoder
import org.lanternpowered.terre.impl.network.packet.HatRackItemEncoder
import org.lanternpowered.terre.impl.network.packet.ItemRemoveOwnerDecoder
import org.lanternpowered.terre.impl.network.packet.ItemRemoveOwnerEncoder
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
import org.lanternpowered.terre.impl.network.packet.PlayerDeathDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerDeathEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerHealthDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerHealthEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerInfoDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerInfoEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerManaDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerManaEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerPvPDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerPvPEncoder
import org.lanternpowered.terre.impl.network.packet.PlayerSpawnDecoder
import org.lanternpowered.terre.impl.network.packet.PlayerSpawnEncoder
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
import org.lanternpowered.terre.impl.network.packet.SpeechBubbleEncoder
import org.lanternpowered.terre.impl.network.packet.StatusDecoder
import org.lanternpowered.terre.impl.network.packet.StatusEncoder
import org.lanternpowered.terre.impl.network.packet.TeleportPylonDecoder
import org.lanternpowered.terre.impl.network.packet.TeleportPylonEncoder
import org.lanternpowered.terre.impl.network.packet.TileSquareDecoder
import org.lanternpowered.terre.impl.network.packet.TileSquareEncoder
import org.lanternpowered.terre.impl.network.packet.WorldInfoDecoder
import org.lanternpowered.terre.impl.network.packet.WorldInfoEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.CannotBeTakenByEnemiesItemUpdateDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.FoodPlatterItemDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.FoodPlatterItemEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.InstancedItemUpdateDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.ItemUpdateEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.KeepAliveDuringModReloadDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.KeepAliveDuringModReloadEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.ModDataDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.ModDataEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.ModFileRequestDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.ModFileRequestEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.ModFileResponseDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.ModFileResponseEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.NpcStrikeDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.NpcStrikeEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.PlayerInventorySlotDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.PlayerInventorySlotEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.ShimmeredItemUpdateDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.SimpleItemUpdateDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.SyncModsDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.SyncModsDoneDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.SyncModsDoneEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.SyncModsEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.WeaponsRackItemDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.WeaponsRackItemEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.ChestItemDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.ChestItemEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.ItemFrameItemDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.ItemFrameItemEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.UpdateModConfigRequestDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.UpdateModConfigRequestEncoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.UpdateModConfigResponseDecoder
import org.lanternpowered.terre.impl.network.packet.tmodloader.UpdateModConfigResponseEncoder

/**
 * Protocol that will be used for tModLoader when vanilla clients aren't allowed.
 *
 * ModNet.AllowVanillaClients = false
 */
internal val ProtocolTModLoader = protocol("tModLoader") {
  // https://github.com/tModLoader/tModLoader/blob/1.4.4/patches/tModLoader/Terraria/NetMessage.cs.patch
  // https://github.com/tModLoader/tModLoader/blob/1.4.4/patches/tModLoader/Terraria/ModLoader/IO/ItemIO.cs

  bind(1, ConnectionRequestEncoder, ConnectionRequestDecoder, PacketDirection.ClientToServer)
  bind(2, DisconnectEncoder, DisconnectDecoder, PacketDirection.ServerToClient)
  bind(3, ConnectionApprovedEncoder, ConnectionApprovedDecoder, PacketDirection.ServerToClient)
  bind(4, PlayerInfoEncoder, PlayerInfoDecoder)
  bind(5, PlayerInventorySlotEncoder, PlayerInventorySlotDecoder)
  bind(6, RequestWorldInfoEncoder, RequestWorldInfoDecoder, PacketDirection.ClientToServer)
  bind(7, WorldInfoEncoder, WorldInfoDecoder, PacketDirection.ServerToClient)
  bind(8, EssentialTilesRequestEncoder, PacketDirection.ClientToServer)
  bind(9, StatusEncoder, StatusDecoder, PacketDirection.ServerToClient)
  bind(12, PlayerSpawnEncoder, PlayerSpawnDecoder)
  bind(13, PlayerUpdateEncoder, PlayerUpdateDecoder)
  bind(14, PlayerActiveEncoder, PlayerActiveDecoder, PacketDirection.ServerToClient)
  bind(16, PlayerHealthEncoder, PlayerHealthDecoder)
  bind(20, TileSquareEncoder, TileSquareDecoder)
  bind(21, ItemUpdateEncoder, SimpleItemUpdateDecoder)
  bind(22, ItemUpdateOwnerEncoder, ItemUpdateOwnerDecoder)
  bind(23, NpcUpdateEncoder, NpcUpdateDecoder, PacketDirection.ServerToClient)
  bind(27, ProjectileUpdateEncoder, ProjectileUpdateDecoder)
  bind(28, NpcStrikeEncoder, NpcStrikeDecoder)
  bind(29, ProjectileDestroyEncoder, ProjectileDestroyDecoder)
  bind(30, PlayerPvPEncoder, PlayerPvPDecoder)
  bind(32, ChestItemEncoder, ChestItemDecoder)
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
  bind(89, ItemFrameItemEncoder, ItemFrameItemDecoder)
  bind(90, ItemUpdateEncoder, InstancedItemUpdateDecoder)
  bind(91, SpeechBubbleEncoder, PacketDirection.ServerToClient)
  bind(107, ChatMessageEncoder, ChatMessageDecoder, PacketDirection.ServerToClient)
  bind(118, PlayerDeathEncoder, PlayerDeathDecoder)
  bind(119, CombatMessageEncoder, PacketDirection.ServerToClient)
  bind(123, WeaponsRackItemEncoder, WeaponsRackItemDecoder)
  bind(124, HatRackItemEncoder, HatRackItemDecoder)
  bind(133, FoodPlatterItemEncoder, FoodPlatterItemDecoder)
  bind(145, ItemUpdateEncoder, ShimmeredItemUpdateDecoder)
  bind(148, ItemUpdateEncoder, CannotBeTakenByEnemiesItemUpdateDecoder)

  // modules
  bind(0x01FF, PlayerCommandEncoder, PlayerCommandDecoder, PacketDirection.ClientToServer)
  bind(0x01FF, PlayerChatMessageEncoder, PlayerChatMessageDecoder, PacketDirection.ServerToClient)
  bind(0x08FF, TeleportPylonEncoder, TeleportPylonDecoder, PacketDirection.ServerToClient)

  // tModLoader
  bind(0xF9, UpdateModConfigRequestEncoder, UpdateModConfigRequestDecoder, PacketDirection.ClientToServer)
  bind(0xF9, UpdateModConfigResponseEncoder, UpdateModConfigResponseDecoder, PacketDirection.ServerToClient)
  bind(0xFA, ModDataEncoder, ModDataDecoder)
  bind(0xFB, SyncModsEncoder, SyncModsDecoder, PacketDirection.ServerToClient)
  bind(0xFB, SyncModsDoneEncoder, SyncModsDoneDecoder, PacketDirection.ClientToServer)
  bind(0xFC, ModFileRequestEncoder, ModFileRequestDecoder, PacketDirection.ClientToServer)
  bind(0xFC, ModFileResponseEncoder, ModFileResponseDecoder, PacketDirection.ServerToClient)
  bind(0xFD, KeepAliveDuringModReloadEncoder, KeepAliveDuringModReloadDecoder, PacketDirection.ClientToServer)
}

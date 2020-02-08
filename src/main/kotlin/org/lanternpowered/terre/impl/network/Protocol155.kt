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
import org.lanternpowered.terre.impl.network.packet.v155.*

internal val Protocol155 = MultistateProtocol(155) {
  // Packets used in both the init and play states
  bind(0x01, ConnectionRequestEncoder, ConnectionRequestDecoder, PacketDirection.ClientToServer)
  bind(0x02, Disconnect155Encoder, Disconnect155Decoder, PacketDirection.ServerToClient)
  bind(0x03, ConnectionApprovedEncoder, ConnectionApprovedDecoder, PacketDirection.ServerToClient)
  bind(0x04, PlayerInfoEncoder, PlayerInfoDecoder)
  bind(0x06, RequestWorldInfoEncoder, RequestWorldInfoDecoder, PacketDirection.ClientToServer)
  bind(0x25, PasswordRequestEncoder, PasswordRequestDecoder, PacketDirection.ServerToClient)
  bind(0x26, PasswordResponseEncoder, PasswordResponseDecoder, PacketDirection.ClientToServer)
  bind(0x44, ClientUniqueIdEncoder, ClientUniqueIdDecoder, PacketDirection.ClientToServer)

  init {
    bind(0x16, IsMobileResponseDecoder, PacketDirection.ClientToServer)
    bind(0x27, IsMobileRequestEncoder, PacketDirection.ServerToClient)
  }

  play {
    bind(0x05, PlayerInventorySlotEncoder, PlayerInventorySlotDecoder)
    bind(0x07, WorldInfo155Encoder, WorldInfo155Decoder, PacketDirection.ServerToClient)
    bind(0x08, EssentialTilesRequestEncoder, PacketDirection.ClientToServer)
    bind(0x09, Status155Encoder, Status155Decoder, PacketDirection.ServerToClient)
    bind(0x0C, PlayerSpawnEncoder, PlayerSpawnDecoder)
    bind(0x0E, PlayerActiveEncoder, PlayerActiveDecoder, PacketDirection.ServerToClient)
    bind(0x10, PlayerHealthEncoder, PlayerHealthDecoder)
    bind(0x16, UpdateItemOwnerEncoder)
    bind(0x16, UpdateItemOwnerDecoder) // And keep alive
    bind(0x17, UpdateNpc155Encoder, UpdateNpc155Decoder)
    bind(0x19, PlayerCommand155Encoder, PlayerCommand155Decoder, PacketDirection.ClientToServer)
    bind(0x19, PlayerChatMessage155Encoder, PlayerChatMessage155Decoder, PacketDirection.ServerToClient)
    bind(0x1A, PlayerHurt155Encoder, PlayerHurt155Decoder)
    bind(0x27, KeepAliveEncoder, PacketDirection.ServerToClient)
    bind(0x2A, PlayerManaEncoder, PlayerManaDecoder)
    bind(0x2C, PlayerDeath155Encoder, PlayerDeath155Decoder)
    bind(0x31, CompleteConnectionEncoder, CompleteConnectionDecoder, PacketDirection.ServerToClient)
    bind(0x32, PlayerBuffsEncoder, PlayerBuffsDecoder)
    bind(0x37, AddPlayerBuff155Encoder, AddPlayerBuff155Decoder)
    bind(0x38, UpdateNpcNameEncoder, UpdateNpcNameDecoder, PacketDirection.ServerToClient)
    bind(0x51, CombatMessage155Encoder, CombatMessage155Decoder, PacketDirection.ServerToClient)
    bind(0x5B, SpeechBubbleEncoder, PacketDirection.ServerToClient)
    // bind(0x66, NebulaLevelUpRequestEncoder, PacketDirection.ServerToClient)
    bind(0x6B, ChatMessage155Encoder, ChatMessage155Decoder)
  }
}

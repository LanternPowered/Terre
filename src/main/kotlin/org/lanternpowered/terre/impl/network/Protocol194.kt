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
import org.lanternpowered.terre.impl.network.packet.v155.AddPlayerBuff155Decoder
import org.lanternpowered.terre.impl.network.packet.v155.AddPlayerBuff155Encoder

internal val Protocol194 = MultistateProtocol(194) {
  bind(0x01, ConnectionRequestEncoder, ConnectionRequestDecoder, PacketDirection.ClientToServer)
  bind(0x02, DisconnectEncoder, DisconnectDecoder, PacketDirection.ServerToClient)
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
    bind(0x07, WorldInfoEncoder, WorldInfoDecoder, PacketDirection.ServerToClient)
    bind(0x09, StatusEncoder, StatusDecoder, PacketDirection.ServerToClient)
    bind(0x0C, PlayerSpawnEncoder, PlayerSpawnDecoder)
    bind(0x0E, PlayerActiveEncoder, PlayerActiveDecoder, PacketDirection.ServerToClient)
    bind(0x16, UpdateItemOwnerEncoder)
    bind(0x16, UpdateItemOwnerDecoder) // And keep alive
    bind(0x17, UpdateNpcEncoder, UpdateNpcDecoder)
    bind(0x27, KeepAliveEncoder, PacketDirection.ServerToClient)
    bind(0x31, CompleteConnectionEncoder, CompleteConnectionDecoder, PacketDirection.ServerToClient)
    bind(0x37, AddPlayerBuffEncoder, AddPlayerBuffDecoder)
    bind(0x38, UpdateNpcNameEncoder, UpdateNpcNameDecoder, PacketDirection.ServerToClient)
    bind(0x51, CombatMessageEncoder, CombatMessageDecoder, PacketDirection.ServerToClient)
    bind(0x5B, SpeechBubbleEncoder)
    // bind(0x66, NebulaLevelUpRequestEncoder, PacketDirection.ServerToClient)
    bind(0x6B, ChatMessageEncoder, ChatMessageDecoder, PacketDirection.ServerToClient)
    bind(0x75, PlayerHurtEncoder, PlayerHurtDecoder)
    bind(0x76, PlayerDeathEncoder, PlayerDeathDecoder)

    bind(0x01FF, PlayerCommandEncoder, PlayerCommandDecoder, PacketDirection.ClientToServer)
    bind(0x01FF, PlayerChatMessageEncoder, PlayerChatMessageDecoder, PacketDirection.ServerToClient)
  }
}

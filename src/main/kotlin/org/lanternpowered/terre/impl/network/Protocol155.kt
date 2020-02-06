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

internal object Protocol155 : Protocol(155) {
  init {
    bind(0x01, ConnectionRequestEncoder, ConnectionRequestDecoder, PacketDirection.ClientToServer)
    bind(0x02, Disconnect155Encoder, Disconnect155Decoder, PacketDirection.ServerToClient)
    bind(0x03, ConnectionApprovedEncoder, ConnectionApprovedDecoder, PacketDirection.ServerToClient)
    bind(0x04, PlayerInfoEncoder, PlayerInfoDecoder)
    bind(0x06, RequestWorldInfoEncoder, RequestWorldInfoDecoder, PacketDirection.ClientToServer)
    bind(0x07, WorldInfoEncoder, WorldInfoDecoder, PacketDirection.ServerToClient)
    bind(0x09, Status155Encoder, Status155Decoder, PacketDirection.ServerToClient)
    bind(0x0C, PlayerSpawnEncoder, PlayerSpawnDecoder)
    bind(0x0E, PlayerActiveEncoder, PlayerActiveDecoder, PacketDirection.ServerToClient)
    bind(0x16, UpdateItemOwnerEncoder)
    bind(0x16, UpdateItemOwnerDecoder) // And keep alive
    bind(0x17, UpdateNpcEncoder, UpdateNpcDecoder)
    bind(0x19, PlayerCommand155Encoder, PlayerCommand155Decoder, PacketDirection.ClientToServer)
    bind(0x19, PlayerChatMessage155Encoder, PlayerChatMessage155Decoder, PacketDirection.ServerToClient)
    bind(0x1A, PlayerHurt155Encoder, PlayerHurt155Decoder)
    bind(0x2C, PlayerDeath155Encoder, PlayerDeath155Decoder)
    bind(0x25, PasswordRequestEncoder, PasswordRequestDecoder, PacketDirection.ServerToClient)
    bind(0x26, PasswordResponseEncoder, PasswordResponseDecoder, PacketDirection.ClientToServer)
    bind(0x27, KeepAliveEncoder, PacketDirection.ServerToClient)
    bind(0x31, CompleteConnectionEncoder, PacketDirection.ServerToClient)
    bind(0x38, UpdateNpcNameEncoder, UpdateNpcNameDecoder, PacketDirection.ServerToClient)
    bind(0x44, ClientUniqueIdEncoder, ClientUniqueIdDecoder, PacketDirection.ClientToServer)
    bind(0x51, CombatMessage155Encoder, CombatMessage155Decoder, PacketDirection.ServerToClient)
    bind(0x5B, SpeechBubbleEncoder, PacketDirection.ServerToClient)
    bind(0x6B, ChatMessage155Encoder, ChatMessage155Decoder)
  }
}

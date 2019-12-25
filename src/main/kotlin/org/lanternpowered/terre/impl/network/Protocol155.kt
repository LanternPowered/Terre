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
    bind(0x01, ConnectionRequestEncoder, ConnectionRequestDecoder)
    bind(0x02, Disconnect155Encoder)
    bind(0x03, ConnectionApprovedEncoder, ConnectionApprovedDecoder)
    bind(0x04, PlayerInfoEncoder, PlayerInfoDecoder)
    bind(0x06, RequestWorldInfoEncoder, RequestWorldInfoDecoder)
    bind(0x07, WorldInfo155Encoder, WorldInfo155Decoder)
    bind(0x09, Status155Encoder)
    bind(0x0E, PlayerActiveEncoder)
    bind(0x16, UpdateItemOwnerEncoder)
    bind(0x16, UpdateItemOwnerDecoder) // And keep alive
    bind(0x17, UpdateNpcEncoder, UpdateNpcDecoder)
    bind(0x19, PlayerCommand155Decoder)
    bind(0x19, PlayerChatMessage155Encoder)
    bind(0x1A, PlayerHurt155Encoder, PlayerHurt155Decoder)
    bind(0x2C, PlayerDeath155Encoder, PlayerDeath155Decoder)
    bind(0x25, PasswordRequestEncoder)
    bind(0x26, PasswordResponseDecoder)
    bind(0x27, KeepAliveEncoder)
    bind(0x31, CompleteConnectionEncoder) // TODO: Could be unneeded
    bind(0x38, UpdateNpcNameEncoder, UpdateNpcNameDecoder)
    bind(0x44, ClientUniqueIdEncoder, ClientUniqueIdDecoder)
    bind(0x51, CombatMessage155Encoder)
    bind(0x5B, SpeechBubbleEncoder)
    bind(0x6B, ChatMessage155Encoder)
  }
}

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
import org.lanternpowered.terre.impl.network.packet.CombatMessagePacket
import org.lanternpowered.terre.impl.network.packet.CompleteConnectionPacket
import org.lanternpowered.terre.impl.network.packet.ConnectionApprovedPacket
import org.lanternpowered.terre.impl.network.packet.ConnectionRequestPacket
import org.lanternpowered.terre.impl.network.packet.DisconnectPacket
import org.lanternpowered.terre.impl.network.packet.IsMobileResponsePacket
import org.lanternpowered.terre.impl.network.packet.KeepAlivePacket
import org.lanternpowered.terre.impl.network.packet.PasswordRequestPacket
import org.lanternpowered.terre.impl.network.packet.PasswordResponsePacket
import org.lanternpowered.terre.impl.network.packet.PlayerActivePacket
import org.lanternpowered.terre.impl.network.packet.PlayerChatMessagePacket
import org.lanternpowered.terre.impl.network.packet.PlayerCommandPacket
import org.lanternpowered.terre.impl.network.packet.PlayerDeathPacket
import org.lanternpowered.terre.impl.network.packet.PlayerHurtPacket
import org.lanternpowered.terre.impl.network.packet.PlayerInfoPacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoRequestPacket
import org.lanternpowered.terre.impl.network.packet.SpeechBubblePacket
import org.lanternpowered.terre.impl.network.packet.StatusPacket
import org.lanternpowered.terre.impl.network.packet.UpdateItemOwnerPacket
import org.lanternpowered.terre.impl.network.packet.UpdateNpcNamePacket
import org.lanternpowered.terre.impl.network.packet.UpdateNpcPacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoPacket
import java.lang.Exception

internal interface ConnectionHandler {

  fun initialize()

  fun disconnect()

  fun exception(throwable: Throwable)

  fun handle(packet: ChatMessagePacket): Boolean {
    return false
  }

  fun handle(packet: ClientUniqueIdPacket): Boolean {
    return false
  }

  fun handle(packet: CombatMessagePacket): Boolean {
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

  fun handle(packet: KeepAlivePacket): Boolean {
    // Never forward keep alive packets, this is just a
    // concept in Terre and does not exist officially.
    return true
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

  fun handle(packet: PlayerCommandPacket): Boolean {
    return false
  }

  fun handle(packet: PlayerDeathPacket): Boolean {
    return false
  }

  fun handle(packet: PlayerHurtPacket): Boolean {
    return false
  }

  fun handle(packet: PlayerInfoPacket): Boolean {
    return false
  }

  fun handle(packet: SpeechBubblePacket): Boolean {
    return false
  }

  fun handle(packet: StatusPacket): Boolean {
    return false
  }

  fun handle(packet: UpdateItemOwnerPacket): Boolean {
    return false
  }

  fun handle(packet: UpdateNpcNamePacket): Boolean {
    return false
  }

  fun handle(packet: UpdateNpcPacket): Boolean {
    return false
  }

  fun handle(packet: WorldInfoPacket): Boolean {
    return false
  }

  fun handle(packet: WorldInfoRequestPacket): Boolean {
    return false
  }

  fun handle(packet: IsMobileResponsePacket): Boolean {
    return false
  }

  fun handle(packet: AddPlayerBuffPacket): Boolean {
    return false
  }

  fun handleGeneric(packet: Packet)

  fun handleUnknown(packet: ByteBuf)
}

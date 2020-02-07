/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("UNCHECKED_CAST")

package org.lanternpowered.terre.impl.network

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
import org.lanternpowered.terre.impl.network.packet.SpeechBubblePacket
import org.lanternpowered.terre.impl.network.packet.StatusPacket
import org.lanternpowered.terre.impl.network.packet.UpdateItemOwnerPacket
import org.lanternpowered.terre.impl.network.packet.UpdateNpcNamePacket
import org.lanternpowered.terre.impl.network.packet.UpdateNpcPacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoPacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoRequestPacket
import kotlin.reflect.KClass

internal object ConnectionHandlerBindings {

  private val handlersByPacketType = mutableMapOf<Class<*>, ConnectionHandler.(Packet) -> Boolean>()

  init {
    bind<ChatMessagePacket>(ConnectionHandler::handle)
    bind<ClientUniqueIdPacket>(ConnectionHandler::handle)
    bind<CombatMessagePacket>(ConnectionHandler::handle)
    bind<CompleteConnectionPacket>(ConnectionHandler::handle)
    bind<ConnectionApprovedPacket>(ConnectionHandler::handle)
    bind<ConnectionRequestPacket>(ConnectionHandler::handle)
    bind<DisconnectPacket>(ConnectionHandler::handle)
    bind<KeepAlivePacket>(ConnectionHandler::handle)
    bind<PasswordRequestPacket>(ConnectionHandler::handle)
    bind<PasswordResponsePacket>(ConnectionHandler::handle)
    bind<PlayerActivePacket>(ConnectionHandler::handle)
    bind<PlayerChatMessagePacket>(ConnectionHandler::handle)
    bind<PlayerCommandPacket>(ConnectionHandler::handle)
    bind<PlayerDeathPacket>(ConnectionHandler::handle)
    bind<PlayerHurtPacket>(ConnectionHandler::handle)
    bind<PlayerInfoPacket>(ConnectionHandler::handle)
    bind<SpeechBubblePacket>(ConnectionHandler::handle)
    bind<StatusPacket>(ConnectionHandler::handle)
    bind<UpdateItemOwnerPacket>(ConnectionHandler::handle)
    bind<UpdateNpcNamePacket>(ConnectionHandler::handle)
    bind<UpdateNpcPacket>(ConnectionHandler::handle)
    bind<WorldInfoPacket>(ConnectionHandler::handle)
    bind<WorldInfoRequestPacket>(ConnectionHandler::handle)
    bind<AddPlayerBuffPacket>(ConnectionHandler::handle)
    bind<IsMobileResponsePacket>(ConnectionHandler::handle)
  }

  internal fun <P : Packet> getHandler(packetType: Class<P>): (ConnectionHandler.(packet: P) -> Boolean)?
      = this.handlersByPacketType[packetType]

  private inline fun <reified P : Packet> bind(
      noinline handler: ConnectionHandler.(packet: P) -> Boolean) {
    bind(P::class, handler)
  }

  private fun <P : Packet> bind(type: KClass<P>, handler: ConnectionHandler.(packet: P) -> Boolean) {
    this.handlersByPacketType[type.java] = handler as ConnectionHandler.(packet: Packet) -> Boolean
  }
}

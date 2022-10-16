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
import org.lanternpowered.terre.impl.network.packet.CannotBeTakenByEnemiesItemUpdatePacket
import org.lanternpowered.terre.impl.network.packet.ChatMessagePacket
import org.lanternpowered.terre.impl.network.packet.ClientUniqueIdPacket
import org.lanternpowered.terre.impl.network.packet.CombatMessagePacket
import org.lanternpowered.terre.impl.network.packet.CompleteConnectionPacket
import org.lanternpowered.terre.impl.network.packet.ConnectionApprovedPacket
import org.lanternpowered.terre.impl.network.packet.ConnectionRequestPacket
import org.lanternpowered.terre.impl.network.packet.CustomPayloadPacket
import org.lanternpowered.terre.impl.network.packet.ProjectileDestroyPacket
import org.lanternpowered.terre.impl.network.packet.DisconnectPacket
import org.lanternpowered.terre.impl.network.packet.InstancedItemUpdatePacket
import org.lanternpowered.terre.impl.network.packet.ClientPlayerLimitResponsePacket
import org.lanternpowered.terre.impl.network.packet.KeepAlivePacket
import org.lanternpowered.terre.impl.network.packet.PasswordRequestPacket
import org.lanternpowered.terre.impl.network.packet.PasswordResponsePacket
import org.lanternpowered.terre.impl.network.packet.PlayerActivePacket
import org.lanternpowered.terre.impl.network.packet.PlayerChatMessagePacket
import org.lanternpowered.terre.impl.network.packet.PlayerCommandPacket
import org.lanternpowered.terre.impl.network.packet.PlayerDeathPacket
import org.lanternpowered.terre.impl.network.packet.PlayerHurtPacket
import org.lanternpowered.terre.impl.network.packet.PlayerInfoPacket
import org.lanternpowered.terre.impl.network.packet.PlayerSpawnPacket
import org.lanternpowered.terre.impl.network.packet.PlayerTeamPacket
import org.lanternpowered.terre.impl.network.packet.PlayerTeleportThroughPortalPacket
import org.lanternpowered.terre.impl.network.packet.PlayerUpdatePacket
import org.lanternpowered.terre.impl.network.packet.SpeechBubblePacket
import org.lanternpowered.terre.impl.network.packet.StatusPacket
import org.lanternpowered.terre.impl.network.packet.ItemUpdateOwnerPacket
import org.lanternpowered.terre.impl.network.packet.NpcUpdateNamePacket
import org.lanternpowered.terre.impl.network.packet.NpcUpdatePacket
import org.lanternpowered.terre.impl.network.packet.ProjectileUpdatePacket
import org.lanternpowered.terre.impl.network.packet.ShimmeredItemUpdatePacket
import org.lanternpowered.terre.impl.network.packet.SimpleItemUpdatePacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoPacket
import org.lanternpowered.terre.impl.network.packet.WorldInfoRequestPacket
import kotlin.reflect.KClass

internal object ConnectionHandlerBindings {

  private val handlersByPacketType = mutableMapOf<Class<*>, Binding<Packet>>()

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
    bindSuspend<PlayerCommandPacket>(ConnectionHandler::handle)
    bind<PlayerDeathPacket>(ConnectionHandler::handle)
    bind<PlayerHurtPacket>(ConnectionHandler::handle)
    bind<PlayerInfoPacket>(ConnectionHandler::handle)
    bind<SpeechBubblePacket>(ConnectionHandler::handle)
    bind<StatusPacket>(ConnectionHandler::handle)
    bind<ItemUpdateOwnerPacket>(ConnectionHandler::handle)
    bind<NpcUpdateNamePacket>(ConnectionHandler::handle)
    bind<NpcUpdatePacket>(ConnectionHandler::handle)
    bind<WorldInfoPacket>(ConnectionHandler::handle)
    bind<WorldInfoRequestPacket>(ConnectionHandler::handle)
    bind<AddPlayerBuffPacket>(ConnectionHandler::handle)
    bind<ClientPlayerLimitResponsePacket>(ConnectionHandler::handle)
    bind<PlayerSpawnPacket>(ConnectionHandler::handle)
    bind<CustomPayloadPacket>(ConnectionHandler::handle)
    bind<PlayerTeleportThroughPortalPacket>(ConnectionHandler::handle)
    bind<ProjectileDestroyPacket>(ConnectionHandler::handle)
    bind<ProjectileUpdatePacket>(ConnectionHandler::handle)
    bind<PlayerUpdatePacket>(ConnectionHandler::handle)
    bind<PlayerTeamPacket>(ConnectionHandler::handle)
    bind<SimpleItemUpdatePacket>(ConnectionHandler::handle)
    bind<InstancedItemUpdatePacket>(ConnectionHandler::handle)
    bind<ShimmeredItemUpdatePacket>(ConnectionHandler::handle)
    bind<CannotBeTakenByEnemiesItemUpdatePacket>(ConnectionHandler::handle)
  }

  internal fun <P : Packet> getBinding(
    packetType: Class<P>
  ): Binding<P>? = handlersByPacketType[packetType] as Binding<P>?

  private inline fun <reified P : Packet> bind(
    noinline handler: ConnectionHandler.(packet: P) -> Boolean
  ) {
    bind(P::class, SimpleBinding { connectionHandler, packet ->
      connectionHandler.handler(packet)
    })
  }

  private inline fun <reified P : Packet> bindSuspend(
    noinline handler: suspend ConnectionHandler.(packet: P) -> Boolean
  ) {
    bind(P::class, SuspendBinding { connectionHandler, packet ->
      connectionHandler.handler(packet)
    })
  }

  private fun <P : Packet> bind(type: KClass<P>, handler: Binding<P>) {
    handlersByPacketType[type.java] = handler as Binding<Packet>
  }

  interface Binding<P : Packet>

  fun interface SuspendBinding<P : Packet> : Binding<P> {

    suspend fun handle(connectionHandler: ConnectionHandler, packet: P): Boolean
  }

  fun interface SimpleBinding<P : Packet> : Binding<P> {

    fun handle(connectionHandler: ConnectionHandler, packet: P): Boolean
  }
}

/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl

import kotlinx.coroutines.Job
import org.lanternpowered.terre.Player
import org.lanternpowered.terre.ProtocolVersion
import org.lanternpowered.terre.Server
import org.lanternpowered.terre.ServerInfo
import org.lanternpowered.terre.coroutines.delay
import org.lanternpowered.terre.dispatcher.launchAsync
import org.lanternpowered.terre.impl.event.EventExecutor
import org.lanternpowered.terre.impl.network.VersionedProtocol
import org.lanternpowered.terre.impl.player.PlayerImpl
import org.lanternpowered.terre.impl.portal.PortalBuilderImpl
import org.lanternpowered.terre.impl.portal.PortalImpl
import org.lanternpowered.terre.math.Vec2f
import org.lanternpowered.terre.portal.Portal
import org.lanternpowered.terre.portal.PortalBuilder
import org.lanternpowered.terre.portal.PortalType
import org.lanternpowered.terre.text.MessageSender
import org.lanternpowered.terre.text.Text
import java.util.concurrent.ConcurrentHashMap

internal class ServerImpl(
    override val info: ServerInfo,
    override var allowAutoJoin: Boolean = false,
    val versionedProtocol: VersionedProtocol? = null
) : Server {

  var unregistered = false
    private set

  val registerLock = Any()

  private val mutablePlayers = MutablePlayerCollection.concurrentOf()

  val portalsByProjectileId = ConcurrentHashMap<Int, PortalImpl>()
  val projectileIdAllocator = ProjectileIdAllocator()

  private var portalUpdateJob: Job? = null
  private var portalCollisionJob: Job? = null

  /**
   * The last server version that was noticed by connecting clients. Is
   * used to speed up connection when multiple versions are possible.
   */
  @Volatile var lastKnownVersion: ProtocolVersion? = null

  override val players
    get() = mutablePlayers.toImmutable()

  fun initPlayer(player: Player) {
    mutablePlayers.add(player)

    // Send all the portals
    player as PlayerImpl
    for (portal in portalsByProjectileId.values)
      openPortalFor(portal, player)
  }

  fun removePlayer(player: Player) {
    mutablePlayers.remove(player)

    // Destroy all the portals
    player as PlayerImpl
    for (portal in portalsByProjectileId.values)
      closePortalFor(portal, player)
  }

  fun init() {
    portalCollisionJob = launchAsync(EventExecutor.dispatcher) {
      while (true) {
        val players = mutablePlayers.toList()
        // Update all portals, so the projectiles don't expire
        for (portal in portalsByProjectileId.values) {
          for (player in players) {
            val intersects = player.boundingBox.intersects(portal.boundingBox)
            if (intersects && portal.colliding.add(player)) {
              portal.onStartCollide(portal, player)
            } else if (!intersects && portal.colliding.remove(player)) {
              portal.onStopCollide(portal, player)
            }
          }
        }
        delay(25)
      }
    }
    portalUpdateJob = launchAsync(EventExecutor.dispatcher) {
      while (true) {
        // Update all portals, so the projectiles don't expire
        for (portal in portalsByProjectileId.values)
          openPortalFor(portal, mutablePlayers)
        delay(1000)
      }
    }
  }

  private fun cleanup() {
    portalUpdateJob?.cancel()
    portalUpdateJob = null
    portalCollisionJob?.cancel()
    portalCollisionJob = null
  }

  override fun unregister() {
    this.unregistered = true
    ProxyImpl.servers.unregister(this)
    cleanup()
  }

  override fun evacuate() {
    TODO("not implemented")
  }

  override fun sendMessage(message: String) {
    mutablePlayers.forEach { it.sendMessage(message) }
  }

  override fun sendMessage(message: Text) {
    mutablePlayers.forEach { it.sendMessage(message) }
  }

  override fun sendMessageAs(message: Text, sender: MessageSender) {
    mutablePlayers.forEach { it.sendMessageAs(message, sender) }
  }

  override fun sendMessageAs(message: String, sender: MessageSender) {
    mutablePlayers.forEach { it.sendMessageAs(message, sender) }
  }

  override fun openPortal(type: PortalType, position: Vec2f, fn: PortalBuilder.() -> Unit): Portal {
    val builder = PortalBuilderImpl(this, type, position)
    builder.fn()
    val portal = builder.build()
    portalsByProjectileId[portal.projectileId.value] = portal
    // Open for all the players that are currently connected
    openPortalFor(portal, mutablePlayers)
    return portal
  }

  fun openPortalFor(portal: PortalImpl, players: Iterable<Player>) {
    val packet = portal.createUpdatePacket()
    for (player in players)
      (player as PlayerImpl).clientConnection.send(packet)
  }

  fun openPortalFor(portal: PortalImpl, player: Player) {
    (player as PlayerImpl).clientConnection.send(portal.createUpdatePacket())
  }

  fun closePortalFor(portal: PortalImpl, player: Player) {
    (player as PlayerImpl).clientConnection.send(portal.createDestroyPacket())
  }

  fun closePortal(portal: PortalImpl) {
    if (portalsByProjectileId.remove(portal.projectileId.value) == null)
      return
    projectileIdAllocator.release(portal.projectileId)
    for (player in mutablePlayers)
      closePortalFor(portal, player)
  }
}

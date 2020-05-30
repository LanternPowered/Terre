/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.portals

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import org.lanternpowered.terre.Proxy
import org.lanternpowered.terre.Server
import org.lanternpowered.terre.config.ConfigDirectory
import org.lanternpowered.terre.event.Subscribe
import org.lanternpowered.terre.event.proxy.ProxyInitializeEvent
import org.lanternpowered.terre.event.proxy.ProxyShutdownEvent
import org.lanternpowered.terre.event.server.ServerRegisterEvent
import org.lanternpowered.terre.event.server.ServerUnregisterEvent
import org.lanternpowered.terre.logger.Logger
import org.lanternpowered.terre.math.Vec2f
import org.lanternpowered.terre.plugin.Plugin
import org.lanternpowered.terre.plugin.inject
import org.lanternpowered.terre.portal.Portal
import org.lanternpowered.terre.util.ColorHue
import java.nio.file.Files
import kotlin.random.Random

/**
 * A plugin which can be used to open portals in your servers
 * to teleport between them. Portals will persist between server
 * startup/shutdown, but only if the same world was loaded.
 */
@Plugin(id = "portals")
object Portals {

  private val logger = inject<Logger>()
  private val configDir = inject<ConfigDirectory>()

  private val portalsFile = configDir.path.resolve("portals.json")
  private val mutex = Mutex()

  /**
   * All the data of known portals that should be persisted.
   */
  private val portalData = mutableMapOf<String, PortalData>()

  /**
   * All the portals that are currently opened.
   */
  private val portals = mutableMapOf<String, Portal>()

  /**
   * Loads the portal data.
   */
  private suspend fun loadPortalData() {
    logger.info { "Loading portal data..." }
    withContext(Dispatchers.IO) {
      if (!Files.exists(portalsFile))
        return@withContext
      val content = Files.newBufferedReader(portalsFile).useLines { it.joinToString("") }
      val portals = Json.parse(PortalData.serializer().list, content)
      // TODO: Use mutex.withLock {}
      //   when it actually compiles...
      mutex.lock()
      try {
        portalData.clear()
        for (portal in portals)
          portalData[portal.name] = portal
        for (portal in this@Portals.portals.values)
          portal.close()
        this@Portals.portals.clear()
      } finally {
        mutex.unlock()
      }
    }
  }

  /**
   * Saves the portal data.
   */
  private suspend fun savePortalData() {
    logger.info { "Saving portal data..." }
    withContext(Dispatchers.IO) {
      val parent = portalsFile.parent
      if (!Files.exists(parent))
        Files.createDirectories(parent)
      val portals = mutex.withLock { portalData.values.toList() }
      val content = Json.stringify(PortalData.serializer().list, portals)
      Files.newBufferedWriter(portalsFile).use { writer -> writer.append(content) }
    }
  }

  /**
   * Creates a new portal with the given parameters.
   */
  private suspend fun createPortal(
      name: String, origin: String, destination: String, position: Vec2f, colorHue: ColorHue = randomColorHue()
  ): PortalData {
    val data = PortalData(name, position, colorHue, origin, destination)
    mutex.withLock {
      check(portalData.containsKey(name)) { "The name '$name' is already used." }
      portalData[name] = data
      val originServer = Proxy.servers[origin]
      if (originServer != null)
        openPortal(originServer, data)
      return data
    }
  }

  @Subscribe
  private suspend fun onInit(event: ProxyInitializeEvent) {
    logger.info { "Initializing Portals plugin!" }

    loadPortalData()
    for (server in Proxy.servers)
      loadPortals(server)

    // TODO: Register commands
  }

  @Subscribe
  private fun onShutdown(event: ProxyShutdownEvent) {

  }

  @Subscribe
  private fun onServerRegister(event: ServerRegisterEvent) {
    loadPortals(event.server)
  }

  @Subscribe
  private fun onServerUnregister(event: ServerUnregisterEvent) {
    closePortals(event.server)
  }

  private fun openPortal(server: Server, data: PortalData) {
    val portal = server.openPortal(data.position, data.colorHue)
    portal.onUse { player ->
      val destination = Proxy.servers[data.destination]
      // Do nothing if the server doesn't exist, otherwise teleport
      // the player to the other server
      if (destination != null)
        player.connectTo(destination)
    }
  }

  /**
   * Close all old portals, if there are any.
   */
  private fun closePortals(server: Server) {
    val itr = portals.values.iterator()
    while (itr.hasNext()) {
      val portal = itr.next()
      if (portal.server == server) {
        portal.close()
        itr.remove()
      }
    }
  }

  /**
   * Instantiates all the portals in the given server.
   */
  private fun loadPortals(server: Server) {
    closePortals(server)
    // Open them again
    for (data in portalData.values) {
      if (data.origin != server.info.name)
        continue
      openPortal(server, data)
    }
  }

  /**
   * Generates a random color hue.
   */
  fun randomColorHue(): ColorHue = ColorHue(Random.nextFloat())
}

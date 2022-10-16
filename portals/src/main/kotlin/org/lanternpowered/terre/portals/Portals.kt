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
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.lanternpowered.terre.Player
import org.lanternpowered.terre.Proxy
import org.lanternpowered.terre.Server
import org.lanternpowered.terre.command.CommandManager
import org.lanternpowered.terre.command.SimpleCommandExecutor
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
import org.lanternpowered.terre.portal.PortalType
import org.lanternpowered.terre.portal.PortalTypeRegistry
import org.lanternpowered.terre.portal.PortalTypes
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.joinToText
import org.lanternpowered.terre.text.text
import org.lanternpowered.terre.util.Color
import org.lanternpowered.terre.util.Colors
import java.nio.file.Files

/**
 * A plugin which can be used to open portals in your servers to teleport between them. Portals
 * will persist between server startup/shutdown, but only if the same world was loaded.
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

  private val listPortalDataSerializer = ListSerializer(PortalData.serializer())

  /**
   * Loads the portal data.
   */
  private suspend fun loadPortalData() {
    logger.info { "Loading portal data..." }
    withContext(Dispatchers.IO) {
      if (!Files.exists(portalsFile))
        return@withContext
      val content = Files.newBufferedReader(portalsFile).useLines { it.joinToString("") }
      val newPortalData = Json.decodeFromString(listPortalDataSerializer, content)
      mutex.withLock {
        portalData.clear()
        for (portal in newPortalData)
          portalData[portal.name] = portal
        for (portal in portals.values)
          portal.close()
        portals.clear()
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
      val content = Json.encodeToString(listPortalDataSerializer, portals)
      Files.newBufferedWriter(portalsFile).use { writer -> writer.append(content) }
    }
  }

  /**
   * Creates a new portal with the given parameters.
   */
  private suspend fun createPortal(
    name: String, origin: String, destination: String, type: PortalType, position: Vec2f
  ): PortalData {
    val data = PortalData(name, type, position, origin, destination)
    mutex.withLock {
      check(portalData.containsKey(name)) { "The name '$name' is already used." }
      portalData[name] = data
      val originServer = Proxy.servers[origin]
      if (originServer != null)
        openPortal(originServer, data)
      return data
    }
  }

  /**
   * Removes a portal with the given name, if it exists.
   */
  private suspend fun removePortal(name: String): Boolean {
    mutex.withLock {
      val data = portalData.remove(name)
      if (data != null) {
        portals.remove(name)?.close()
        return true
      }
    }
    return false
  }

  private val messagePrefix = "[Portals] ".text().color(Color(137, 0, 235))

  private val portalCommandExecutor = SimpleCommandExecutor { source, _, args ->
    val player = source as? Player
      ?: return@SimpleCommandExecutor
    fun send(text: Text) {
      player.sendMessage(messagePrefix + text)
    }
    // TODO: Use Kommando
    if (args.isEmpty()) {
      send("Usage: portal create|delete".text())
    } else {
      when (args[0]) {
        "create" -> {
          val name = if (args.size > 1) {
            args[1]
          } else {
            send("Usage: portal create <name> <destination> [--type <type>] [--pos <x> <y>]".text())
            return@SimpleCommandExecutor
          }
          val origin = player.serverConnection!!.server.info.name
          val destination = if (args.size > 2) {
            args[2]
          } else {
            send("Please specify the destination of the portal".text())
            return@SimpleCommandExecutor
          }
          var position: Vec2f = player.position
          var type = PortalTypes.Invisible
          var index = 3
          while (index < args.size) {
            var option = args[index++]
            if (!option.startsWith("--")) {
              send("Unexpected value: ".text() + option.text(color = Colors.Red))
              return@SimpleCommandExecutor
            }
            option = option.substring(2)
            when (option) {
              "type" -> {
                val typeName = args.getOrNull(index++)
                if (typeName == null) {
                  send("No portal type specified".text())
                  return@SimpleCommandExecutor
                }
                type = PortalTypeRegistry[typeName] ?: run {
                  val validTypes = PortalTypeRegistry.all.joinToText(
                    separator = ", ".text()
                  ) { it.name.text(color = Colors.Lime) }
                  send("Invalid portal type specified ".text() +
                    typeName.text(color = Colors.Red) +
                    ", the valid types are: )".text() +
                    validTypes)
                  return@SimpleCommandExecutor
                }
              }
              "pos" -> {
                val x = args.getOrNull(index++)
                val y = args.getOrNull(index++)
                if (x == null || y == null) {
                  send("No position specified".text())
                  return@SimpleCommandExecutor
                }
                if (x.toFloatOrNull() == null || y.toFloatOrNull() == null) {
                  send("Invalid position specified: ".text() + "$x $y".text(color = Colors.Red))
                  return@SimpleCommandExecutor
                }
                position = Vec2f(x.toFloat(), y.toFloat())
              }
            }
          }
          createPortal(name, origin, destination, type, position)
        }
        "delete" -> {
          val name = if (args.size > 1) {
            args[1]
          } else {
            send("Usage: portal delete <name>".text())
            return@SimpleCommandExecutor
          }
          if (args.size > 2) {
            send("Unexpected argument: ".text() + args[2].text(color = Colors.Red))
            return@SimpleCommandExecutor
          }
          removePortal(name)
        }
      }
    }
  }

  @Subscribe
  private suspend fun onInit(event: ProxyInitializeEvent) {
    logger.info { "Initializing Portals plugin!" }

    loadPortalData()
    for (server in Proxy.servers)
      loadPortals(server)

    CommandManager.register("portal", portalCommandExecutor)
  }

  @Subscribe
  private suspend fun onShutdown(event: ProxyShutdownEvent) {
    savePortalData()
  }

  @Subscribe
  private suspend fun onServerRegister(event: ServerRegisterEvent) {
    mutex.withLock {
      loadPortals(event.server)
    }
  }

  @Subscribe
  private suspend fun onServerUnregister(event: ServerUnregisterEvent) {
    mutex.withLock {
      closePortals(event.server)
    }
  }

  private fun openPortal(server: Server, data: PortalData) {
    val portal = server.openPortal(data.type, data.position) {
      onStartCollide { player ->
        val destination = Proxy.servers[data.destination]
        // Do nothing if the server doesn't exist, otherwise teleport the player to the other server
        if (destination != null)
          player.connectTo(destination)
      }
    }
    portals[data.name] = portal
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
}

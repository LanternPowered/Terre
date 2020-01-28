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

import com.fasterxml.jackson.databind.module.SimpleModule
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.toml
import com.uchuhimo.konf.source.toml.toToml
import kotlinx.coroutines.CoroutineDispatcher
import org.lanternpowered.terre.Console
import org.lanternpowered.terre.Player
import org.lanternpowered.terre.PlayerIdentifier
import org.lanternpowered.terre.Proxy
import org.lanternpowered.terre.event.proxy.ProxyInitializeEvent
import org.lanternpowered.terre.event.proxy.ProxyShutdownEvent
import org.lanternpowered.terre.impl.config.ServerConfigSpec
import org.lanternpowered.terre.impl.console.ConsoleImpl
import org.lanternpowered.terre.impl.event.TerreEventBus
import org.lanternpowered.terre.impl.network.NetworkManager
import org.lanternpowered.terre.impl.network.ProxyBroadcastTask
import org.lanternpowered.terre.impl.network.TransportType
import org.lanternpowered.terre.impl.plugin.PluginManagerImpl
import org.lanternpowered.terre.impl.text.TextDeserializer
import org.lanternpowered.terre.impl.text.TextSerializer
import org.lanternpowered.terre.plugin.PluginContainer
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.textOf
import java.net.BindException
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal object ProxyImpl : Proxy {

  val console = ConsoleImpl(::processCommand, ::shutdown)

  val pluginManager = PluginManagerImpl()

  val ioExecutor: Executor = Executors.newFixedThreadPool(10) // TODO: Proper amount

  private val config: Config = loadConfig()
  private lateinit var networkManager: NetworkManager

  private val playersByIdentifier = ConcurrentHashMap<PlayerIdentifier, Player>()

  override val players = PlayerCollectionImpl(this.playersByIdentifier)

  /**
   * Attempts to register a player to the proxy. Returns true if the
   * registration was successful and false in case of failure. Failure
   * can occur if there's already a player with the same identifier.
   */
  fun registerPlayer(player: Player): Boolean {
    return this.playersByIdentifier.putIfAbsent(player.identifier, player) == null
  }

  /**
   * Unregisters the given player.
   */
  fun unregisterPlayer(player: Player) {
    this.playersByIdentifier -= player.identifier
  }

  override var name: String
    get() = Terre.name
    set(value) {} // TODO

  override var messageOfTheDay: Text by this.config.property(ServerConfigSpec.messageOfTheDay)
  override var maxPlayers: Int by this.config.property(ServerConfigSpec.maxPlayers)
  override var password: String by this.config.property(ServerConfigSpec.password)

  override val address: InetSocketAddress
    get() = this.networkManager.address as InetSocketAddress

  override val dispatcher: CoroutineDispatcher
    get() = TerreEventBus.dispatcher

  override val pluginContainer: PluginContainer
    get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

  /**
   * Initializes the server.
   */
  fun init() {
    Terre.logger.info("Starting ${Terre.name} Server ${Terre.version}")

    initServer()

    // Post the proxy init event and wait for it to finish before continuing
    TerreEventBus.postAsyncWithFuture(ProxyInitializeEvent()).join()

    // Start the console, reading commands starts now
    this.console.start()

    // Start broadcasting the server information
    // to the LAN network, used by mobile
    // TODO: Make this configurable
    val broadcastTask = ProxyBroadcastTask(this)
    broadcastTask.init()
  }

  private fun processCommand(command: String) {
    // TODO: Process commands
    TerreEventBus.executor.execute {
      if (command.trim().toLowerCase() == "shutdown") {
        shutdown()
      } else {
        Console.sendMessage("Unknown command: $command")
      }
    }
  }

  override fun shutdown() {
    shutdown(textOf("Shutting down the server."))
  }

  override fun shutdown(reason: Text) {
    Terre.logger.info("Shutting down the server.")

    this.networkManager.shutdown(reason)
    this.console.stop()

    // Post the proxy shutdown event and wait for it to finish before continuing
    TerreEventBus.postAsyncWithFuture(ProxyShutdownEvent()).get(10, TimeUnit.SECONDS)

    TerreEventBus.executor.shutdown()
    if (!TerreEventBus.executor.awaitTermination(10, TimeUnit.SECONDS)) {
      TerreEventBus.executor.shutdownNow()
    }
  }

  private fun initServer() {
    val ip = this.config[ServerConfigSpec.host]
    val port = this.config[ServerConfigSpec.port]

    val address = if (ip.isBlank()) {
      InetSocketAddress(port)
    } else {
      InetSocketAddress(ip, port)
    }

    val transportType = TransportType.findBestType()

    this.networkManager = NetworkManager()
    val future = this.networkManager.init(address, transportType)
    future.awaitUninterruptibly()
    if (!future.isSuccess) {
      val cause = future.cause()
      if (cause is BindException) {
        throw cause
      }
      throw RuntimeException("Failed to bind server address", cause)
    }

    Terre.logger.info("Successfully bound to: $address")
  }

  private fun loadConfig(): Config {
    Terre.logger.info("Loading configuration file.")

    val configPath = Paths.get("config.toml")
    val module = SimpleModule().apply {
      addDeserializer(Text::class.java, TextDeserializer())
      addSerializer(TextSerializer())
    }
    var config = Config {
      this.mapper.registerModule(module)
      addSpec(ServerConfigSpec)
    }

    if (Files.exists(configPath)) {
      Files.newBufferedReader(configPath).use { reader ->
        config = config.from.toml.reader(reader)
      }
    } else {
      Files.newBufferedWriter(configPath).use { writer ->
        config.toToml.toWriter(writer)
      }
    }

    return config
  }
}

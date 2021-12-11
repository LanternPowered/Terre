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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.lanternpowered.terre.Console
import org.lanternpowered.terre.MaxPlayers
import org.lanternpowered.terre.Proxy
import org.lanternpowered.terre.coroutines.delay
import org.lanternpowered.terre.dispatcher.launchAsync
import org.lanternpowered.terre.event.proxy.ProxyInitializeEvent
import org.lanternpowered.terre.event.proxy.ProxyShutdownEvent
import org.lanternpowered.terre.impl.config.ProxyConfigSpec
import org.lanternpowered.terre.impl.config.RawServerInfo
import org.lanternpowered.terre.impl.config.RootConfigDirectoryImpl
import org.lanternpowered.terre.impl.console.ConsoleImpl
import org.lanternpowered.terre.impl.coroutines.tryWithTimeout
import org.lanternpowered.terre.impl.event.EventExecutor
import org.lanternpowered.terre.impl.event.TerreEventBus
import org.lanternpowered.terre.impl.network.NetworkManager
import org.lanternpowered.terre.impl.network.ProxyBroadcastTask
import org.lanternpowered.terre.impl.plugin.PluginManagerImpl
import org.lanternpowered.terre.impl.text.TextJsonDeserializer
import org.lanternpowered.terre.impl.text.TextJsonSerializer
import org.lanternpowered.terre.impl.util.dispatcher.joinBlocking
import org.lanternpowered.terre.text.MessageSender
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.textOf
import org.lanternpowered.terre.util.collection.toImmutableList
import java.net.BindException
import java.net.InetSocketAddress
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds

internal object ProxyImpl : Proxy {

  val console = ConsoleImpl(::processCommand, ::shutdown)

  val networkManager = NetworkManager()

  val mutablePlayers = MutablePlayerCollection.concurrentOf()

  override val players
    get() = mutablePlayers.toImmutable()

  override val servers = ServerCollectionImpl()

  val configDirectory = RootConfigDirectoryImpl(Paths.get("config"))
  private val config: Config = loadConfig()

  override var name by config.property(ProxyConfigSpec.name)

  override var maxPlayers: MaxPlayers
    get() {
      return when (val value = config[ProxyConfigSpec.maxPlayers]) {
        -1 -> MaxPlayers.Unlimited
        else -> MaxPlayers.Limited(value.coerceAtLeast(0))
      }
    }
    set(value) {
      config[ProxyConfigSpec.maxPlayers] = when (value) {
        is MaxPlayers.Limited -> value.amount
        MaxPlayers.Unlimited -> -1
      }
    }

  override var password by config.property(ProxyConfigSpec.password)

  override val address: InetSocketAddress by lazy {
    val ip = config[ProxyConfigSpec.host]
    val port = config[ProxyConfigSpec.port]

    if (ip.isBlank()) {
      InetSocketAddress(port)
    } else {
      InetSocketAddress(ip, port)
    }
  }

  override val dispatcher: CoroutineDispatcher = EventExecutor.pluginAwareDispatcher

  val pluginManager = PluginManagerImpl()

  /**
   * Initializes the server.
   */
  fun init() {
    console.init()
    Terre.logger.info("Starting ${Terre.name}")
    Terre.logger.info("  Version: ${Terre.version}")

    bindNetworkManager()
    initServers()

    // Instantiate all the plugins
    pluginManager.load(true, config[ProxyConfigSpec.disabledPlugins])

    // Post the proxy init event and wait for it to finish before continuing
    TerreEventBus.postAsyncWithFuture(ProxyInitializeEvent()).join()

    // Start the console, reading commands starts now
    console.start()

    if (config[ProxyConfigSpec.LocalBroadcast.enabled]) {
      // Start broadcasting the server information to the LAN network, used by mobile
      // TODO: Make values configurable?
      val broadcastTask = ProxyBroadcastTask(this)
      broadcastTask.init()
    }
  }

  private fun processCommand(command: String) {
    // TODO: Process commands
    EventExecutor.executor.execute {
      if (command.trim().lowercase() == "shutdown") {
        shutdown()
      } else {
        Console.sendMessage("Unknown command: $command")
      }
    }
  }

  override fun shutdown() {
    shutdown(textOf("Shutting down the server."))
  }

  private val shuttingDown = AtomicBoolean()

  override fun shutdown(reason: Text) {
    if (!shuttingDown.compareAndSet(false, true))
      return

    // Disconnect all players and wait for them, with a timeout of 10 seconds
    launchAsync(Dispatchers.Default) {
      Terre.logger.info("Shutting down the server.")

      // Prevent new connections
      networkManager.shutdown()

      var timeout = false

      timeout = tryWithTimeout(10.seconds) {
        players.toImmutableList()
            .map { it.disconnectAsync(reason) }
            .forEach { it.join() }
      }.isFailure || timeout

      // Stop reading the console
      console.stop()

      // Post the proxy shutdown event and wait for it to finish before continuing
      timeout = tryWithTimeout(10.seconds) {
        TerreEventBus.post(ProxyShutdownEvent())
      }.isFailure || timeout

      val executor = EventExecutor.executor
      val executorShutdownTimeout = 10.seconds
      // Shutdown the executor, give 10 seconds to finish remaining tasks
      executor.shutdown()

      var times = 100
      val delay = executorShutdownTimeout.inWholeMilliseconds / times
      while (!executor.isShutdown && times-- > 0) {
        delay(delay)
      }
      if (!executor.isShutdown) {
        executor.shutdownNow()
        timeout = true
      }

      if (timeout)
        Terre.logger.error("Shutting down the proxy took too long.")

      exitProcess(0)
    }
  }

  private fun bindNetworkManager() {
    val future = networkManager.bind(address)
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

  private fun initServers() {
    for (rawServer in config[ProxyConfigSpec.servers]) {
      val info = rawServer.toServerInfo()
      val allowAutoJoin = rawServer.allowAutoJoin

      val server = servers.register(info, silently = true)
      val passwordInfo = if (info.password.isNotEmpty()) ", password: ${info.password}" else ""
      val protocolInfo = if (info.protocolVersion != null) ", protocol: ${info.protocolVersion}" else ""
      Terre.logger.info("Registering server -> ${info.name} -> address: ${rawServer.address}$passwordInfo$protocolInfo")
      server.allowAutoJoin = allowAutoJoin
    }
  }

  private fun loadConfig(): Config {
    Terre.logger.info("Loading configuration file.")

    val module = SimpleModule().apply {
      addDeserializer(Text::class.java, TextJsonDeserializer())
      addSerializer(TextJsonSerializer())
    }
    val config = configDirectory.config("config") {
      mapper.registerModule(module)
      addSpec(ProxyConfigSpec)
    }
    try {
      if (config.exists) {
        config.loadAsync().joinBlocking()
      } else {
        // Add some examples
        config[ProxyConfigSpec.servers] = listOf(
          RawServerInfo(
            name = "lobby",
            address = "127.0.0.1:7778",
            password = "",
            allowAutoJoin = true
          ),
          RawServerInfo(
            name = "survival",
            address = "127.0.0.1:7779",
            password = "",
            allowAutoJoin = false
          )
        )
        config.saveAsync().joinBlocking()
      }
    } catch (ex: Exception) {
      Terre.logger.error("Failed to load or save configuration file", ex)
      return config
    }
    return config
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
}

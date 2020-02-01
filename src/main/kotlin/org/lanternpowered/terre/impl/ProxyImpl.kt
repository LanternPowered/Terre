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
import kotlinx.coroutines.Dispatchers
import org.lanternpowered.terre.Console
import org.lanternpowered.terre.MessageSender
import org.lanternpowered.terre.Proxy
import org.lanternpowered.terre.coroutines.delay
import org.lanternpowered.terre.dispatcher.launchAsync
import org.lanternpowered.terre.event.proxy.ProxyInitializeEvent
import org.lanternpowered.terre.event.proxy.ProxyShutdownEvent
import org.lanternpowered.terre.impl.config.ServerConfigSpec
import org.lanternpowered.terre.impl.console.ConsoleImpl
import org.lanternpowered.terre.impl.coroutines.tryWithTimeout
import org.lanternpowered.terre.impl.dispatcher.PluginContextCoroutineDispatcher
import org.lanternpowered.terre.impl.event.EventExecutor
import org.lanternpowered.terre.impl.event.TerreEventBus
import org.lanternpowered.terre.impl.network.NetworkManager
import org.lanternpowered.terre.impl.network.ProxyBroadcastTask
import org.lanternpowered.terre.impl.plugin.PluginManagerImpl
import org.lanternpowered.terre.impl.text.TextJsonDeserializer
import org.lanternpowered.terre.impl.text.TextJsonSerializer
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.textOf
import org.lanternpowered.terre.util.collection.toImmutableList
import java.net.BindException
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess
import kotlin.time.seconds

internal object ProxyImpl : Proxy {

  val console = ConsoleImpl(::processCommand, ::shutdown)

  private val config: Config = loadConfig()
  val networkManager = NetworkManager()

  val mutablePlayers = MutablePlayerCollection.concurrentOf()

  override val players
    get() = mutablePlayers.toImmutable()

  override var name: String
    get() = Terre.name
    set(value) {} // TODO

  override var messageOfTheDay: Text by this.config.property(ServerConfigSpec.messageOfTheDay)
  override var maxPlayers: Int by this.config.property(ServerConfigSpec.maxPlayers)
  override var password: String by this.config.property(ServerConfigSpec.password)

  override val address: InetSocketAddress by lazy {
    val ip = this.config[ServerConfigSpec.host]
    val port = this.config[ServerConfigSpec.port]

    if (ip.isBlank()) {
      InetSocketAddress(port)
    } else {
      InetSocketAddress(ip, port)
    }
  }

  override val dispatcher: CoroutineDispatcher
      = PluginContextCoroutineDispatcher(EventExecutor.dispatcher) // Expose a safely wrapped dispatcher

  val pluginManager = PluginManagerImpl()

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
    EventExecutor.executor.execute {
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

  private val shuttingDown = AtomicBoolean()

  override fun shutdown(reason: Text) {
    if (!this.shuttingDown.compareAndSet(false, true)) {
      return
    }

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
      val delay = executorShutdownTimeout.toLongMilliseconds() / times
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

  private fun initServer() {
    val future = this.networkManager.bind(this.address)
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
      addDeserializer(Text::class.java, TextJsonDeserializer())
      addSerializer(TextJsonSerializer())
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

  override fun sendMessage(message: String) {
    this.mutablePlayers.forEach { it.sendMessage(message) }
  }

  override fun sendMessage(message: Text) {
    this.mutablePlayers.forEach { it.sendMessage(message) }
  }

  override fun sendMessageAs(message: Text, sender: MessageSender) {
    this.mutablePlayers.forEach { it.sendMessageAs(message, sender) }
  }

  override fun sendMessageAs(message: String, sender: MessageSender) {
    this.mutablePlayers.forEach { it.sendMessageAs(message, sender) }
  }
}

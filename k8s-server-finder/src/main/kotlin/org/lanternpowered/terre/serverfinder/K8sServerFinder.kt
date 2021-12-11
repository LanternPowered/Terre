/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.serverfinder

import com.google.common.reflect.TypeToken
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1Service
import io.kubernetes.client.openapi.models.V1ServiceList
import io.kubernetes.client.util.Watch
import org.lanternpowered.terre.Proxy
import org.lanternpowered.terre.Server
import org.lanternpowered.terre.ServerInfo
import org.lanternpowered.terre.config.ConfigDirectory
import org.lanternpowered.terre.event.Subscribe
import org.lanternpowered.terre.event.proxy.ProxyInitializeEvent
import org.lanternpowered.terre.event.proxy.ProxyShutdownEvent
import org.lanternpowered.terre.logger.Logger
import org.lanternpowered.terre.plugin.Plugin
import org.lanternpowered.terre.plugin.inject
import java.net.InetSocketAddress
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import io.kubernetes.client.util.Config as K8sConfig

/**
 * A plugin which watches a cluster and discovers terraria servers that are deployed
 * automatically based on defined labels. Terre must be deployed on the same cluster.
 */
@Plugin(id = "k8s-server-finder")
object K8sServerFinder {

  private val logger = inject<Logger>()
  private val configDir = inject<ConfigDirectory>()
  private var watchExecutor: ExecutorService? = null

  /**
   * All the services that are discovered and registered through the server finder.
   */
  private val services = HashMap<String, Server>()

  @Subscribe
  private suspend fun onInit(event: ProxyInitializeEvent) {
    logger.info { "Initializing K8s Server Finder plugin!" }

    if (System.getenv(K8sConfig.ENV_SERVICE_HOST) == null) {
      logger.info { "Not running in a k8s cluster, no servers will be detected." }
      return
    }

    val config = configDir.config {
      addSpec(ServerFinderConfigSpec)
    }
    config.loadOrCreate()

    initAndStartWatching(config)
  }

  @Subscribe
  private fun onShutdown(event: ProxyShutdownEvent) {
    watchExecutor?.shutdown()
  }

  private fun initAndStartWatching(config: Config) {
    val client = K8sConfig.defaultClient()
    client.httpClient = client.httpClient.newBuilder()
      .readTimeout(0, TimeUnit.SECONDS) // infinite timeout, needed for watching
      .build()
    val api = CoreV1Api(client)

    val namespace = config[ServerFinderConfigSpec.namespace]
    val labelSelector = config[ServerFinderConfigSpec.labelSelector].ifBlank { null }
    val portLabel = config[ServerFinderConfigSpec.portLabel]
    val nameLabel = config[ServerFinderConfigSpec.nameLabel]
    val allowAutoJoinLabel = config[ServerFinderConfigSpec.allowAutoJoinLabel]
    val passwordLabel = config[ServerFinderConfigSpec.passwordLabel]
    val defaultPort = config[ServerFinderConfigSpec.defaultPort]

    fun listServices(watch: Boolean) = if (namespace.isNotBlank()) {
      api.listNamespacedServiceCall(namespace, null, null, null, null, labelSelector, null, null,
        null, null, watch, null)
    } else {
      api.listServiceForAllNamespacesCall(null, null, null, labelSelector, null, null, null,
        null, null, watch, null)
    }

    val watch = Watch.createWatch<V1Service>(client, listServices(true),
      object : TypeToken<Watch.Response<V1Service>>() {}.type)

    fun updateService(type: String, service: V1Service) {
      val labels = service.metadata?.labels
      val serviceName = service.metadata?.name ?: return
      val name = labels?.get(nameLabel) ?: serviceName
      val port = labels?.get(portLabel)?.toIntOrNull() ?: defaultPort
      val allowAutoJoin = labels?.get(allowAutoJoinLabel)?.toBooleanStrictOrNull() ?: true
      val password = labels?.get(passwordLabel) ?: ""

      synchronized(services) {
        if (type == "DELETED") {
          logger.info { "Deleted old server named $name (service: $serviceName)" }
          val server = services.remove(serviceName)
          server?.unregister()
        } else {
          var server = services[serviceName]
          server?.allowAutoJoin = allowAutoJoin
          if (server != null) {
            var modified = false
            if (server.info.address.port != port)
              modified = true
            if (modified) {
              server.unregister()
              server = null
            }
          }
          if (server == null) {
            if (type == "ADDED") {
              logger.info { "Discovered a new server named $name (service: $serviceName)" }
            }
            val address = InetSocketAddress(serviceName, port)
            val info = ServerInfo(name, address, password)
            server = Proxy.servers.register(info)
            server.allowAutoJoin = allowAutoJoin
            services[serviceName] = server
          }
          Unit
        }
        // TODO: How to handle ERROR type?
      }
    }

    val watchExecutor = Executors.newSingleThreadExecutor { task ->
      Thread(task, "k8s-service-watcher")
    }
    watchExecutor.execute {
      for (response in watch)
        updateService(response.type, response.`object`)
    }
    this.watchExecutor = watchExecutor

    val services = client.execute<V1ServiceList>(listServices(false),
      object : TypeToken<V1ServiceList>() {}.type)
    for (service in services.data.items)
      updateService("ADDED", service)
  }

  object ServerFinderConfigSpec : ConfigSpec("serverFinder") {

    val namespace by optional(
      default = "",
      description = "The namespace where will be searched for servers, defaults to everywhere."
    )

    val defaultPort by optional(
      default = 7777,
      description = "The default port used by terraria servers."
    )

    val portLabel by optional(
      default = "terraria-server-port",
      description = "The label that contains the port of the server. If the label is not set, the" +
        " default port will be used instead."
    )

    val nameLabel by optional(
      default = "terraria-server-name",
      description = "The label that contains the name of the server. If the label is not set, the" +
        " name of the service will be used instead."
    )

    val passwordLabel by optional(
      default = "terraria-server-password",
      description = "The label that contains the password of the server. If the label is not set," +
        " it will assume that there is no password."
    )

    val allowAutoJoinLabel by optional(
      default = "terraria-server-allow-auto-join",
      description = "The label that contains whether to allow players to automatically connect to" +
        " this server. Defaults to true."
    )

    val labelSelector by optional(
      default = "terraria-server=true",
      description = "A selector to restrict the servers that will be detected depending on their " +
        "labels."
    )
  }
}

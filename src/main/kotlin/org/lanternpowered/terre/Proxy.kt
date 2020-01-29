/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre

import kotlinx.coroutines.CoroutineDispatcher
import org.lanternpowered.terre.impl.ProxyImpl
import org.lanternpowered.terre.plugin.PluginContainer
import org.lanternpowered.terre.text.Text
import java.net.InetSocketAddress

/**
 * Represents the proxy server.
 */
interface Proxy {

  /**
   * The coroutine dispatcher.
   */
  val dispatcher: CoroutineDispatcher

  /**
   * All the players that are currently connected to this proxy.
   */
  val players: PlayerCollection

  /**
   * The name of the server.
   */
  var name: String

  /**
   * The maximum amount of players that are allowed
   * to join the server.
   *
   * Setting a lower value doesn't kick any players,
   * but if they disconnect and try to reconnect they
   * won't be able to join.
   */
  var maxPlayers: Int

  /**
   * The message of the day.
   */
  var messageOfTheDay: Text

  /**
   * The password of the server.
   */
  var password: String

  /**
   * The address the server is bound to.
   */
  val address: InetSocketAddress

  /**
   * Triggers the server shutdown and kicks all
   * the players.
   */
  fun shutdown()

  /**
   * Triggers the server shutdown and kicks all
   * the players with the specified reason.
   */
  fun shutdown(reason: Text)

  /**
   * The singleton instance of the proxy.
   */
  companion object : Proxy by ProxyImpl
}

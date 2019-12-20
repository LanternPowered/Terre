/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.text

/**
 * Represents something that can receive messages.
 */
interface MessageReceiver {

  /**
   * Sends a text message.
   */
  fun sendMessage(message: Text)

  /**
   * Sends a plain message.
   */
  fun sendMessage(message: String)
}

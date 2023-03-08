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
 * Represents a sender of messages.
 */
interface MessageSender {

  /**
   * Represents an unknown message sender.
   */
  object Unknown : MessageSender
}

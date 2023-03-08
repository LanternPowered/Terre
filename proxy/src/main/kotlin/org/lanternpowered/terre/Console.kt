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

import org.lanternpowered.terre.command.CommandSource
import org.lanternpowered.terre.impl.ProxyImpl
import org.lanternpowered.terre.text.MessageReceiver

/**
 * Represents the console.
 */
interface Console : MessageReceiver, CommandSource {

  /**
   * The singleton instance of the console.
   */
  companion object : Console by ProxyImpl.console
}

/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.config

import org.lanternpowered.terre.impl.ProxyImpl

/**
 * Represents the root configuration directory.
 */
interface RootConfigDirectory : ConfigDirectoryBase {

  /**
   * The singleton instance of the root config directory.
   */
  companion object : RootConfigDirectory by ProxyImpl.configDirectory
}

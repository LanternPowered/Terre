/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.logger

import org.apache.logging.log4j.Logger

interface Logger : Logger {

  fun info(fn: () -> String)

  fun debug(fn: () -> String)

  fun warn(fn: () -> String)
}

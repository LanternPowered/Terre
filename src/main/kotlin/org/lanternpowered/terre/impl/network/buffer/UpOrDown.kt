/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.buffer

/**
 * Left or right. Usually used for up or down directions.
 *
 * @property isUp Whether it's up
 * @property isDown Whether it's down
 */
internal inline class UpOrDown(val isUp: Boolean) {

  val isDown: Boolean
    get() = !this.isUp
}

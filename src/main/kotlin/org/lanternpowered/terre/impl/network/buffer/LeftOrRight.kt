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
 * Left or right. Usually used for left or right directions.
 *
 * @property isRight Whether it's right
 * @property isLeft Whether it's left
 */
inline class LeftOrRight(val isRight: Boolean) {

  val isLeft: Boolean
    get() = !this.isRight
}

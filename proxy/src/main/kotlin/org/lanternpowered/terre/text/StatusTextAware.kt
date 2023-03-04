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

interface StatusTextAware {

  /**
   * Shows status text on the right side of the HUD. This will override any status text sent by
   * the server until reset.
   */
  fun showStatusText(text: Text, showShadows: Boolean = false)

  /**
   * Resets the status text.
   */
  fun resetStatusText()
}

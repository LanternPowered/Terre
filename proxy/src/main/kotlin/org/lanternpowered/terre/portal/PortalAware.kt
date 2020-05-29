/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.portal

import org.lanternpowered.terre.math.Vec2f
import org.lanternpowered.terre.util.ColorHue

/**
 * Represents something that is aware of portals.
 */
interface PortalAware {

  /**
   * Opens a new portal at the given position and color hue. When
   * the portal is no longer desired, it must be cleaned up through
   * [Portal.close].
   */
  fun openPortal(position: Vec2f, colorHue: ColorHue): Portal

  /**
   * Opens a new portal at the given position and a random color hue.
   * When the portal is no longer desired, it must be cleaned up through
   * [Portal.close].
   */
  fun openPortal(position: Vec2f): Portal
}

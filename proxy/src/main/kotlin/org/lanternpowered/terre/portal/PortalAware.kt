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

/**
 * Represents something that is aware of portals.
 */
interface PortalAware {

  /**
   * Opens a new portal at the given position. When the portal is no longer desired, it must be
   * cleaned up through [Portal.close].
   */
  fun openPortal(type: PortalType, position: Vec2f, builder: PortalBuilder.() -> Unit = {}): Portal
}

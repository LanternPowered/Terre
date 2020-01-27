/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network

import org.lanternpowered.terre.impl.util.Version

/**
 * Represents the version of a client.
 */
internal sealed class ClientVersion {

  /**
   * Represents a vanilla client version.
   */
  data class Vanilla(
      val protocol: Int
  ) : ClientVersion()

  /**
   * Represents a tModLoader client version.
   *
   * @property version The base version, e.g. 0.11.6.2
   * @property branch The branch, if not the default
   * @property beta The beta build number, if applicable
   */
  data class TModLoader(
      val version: Version,
      val branch: String?,
      val beta: Int?
  ) : ClientVersion()
}

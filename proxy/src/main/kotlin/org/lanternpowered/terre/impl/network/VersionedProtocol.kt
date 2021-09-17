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

import org.lanternpowered.terre.ProtocolVersion

/**
 * Represents a [MultistateProtocol] bound to a specific [ProtocolVersion].
 */
internal data class VersionedProtocol(
  val version: ProtocolVersion,
  val protocol: MultistateProtocol
)

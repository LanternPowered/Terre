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

/**
 * Represents an allowed translation between
 * two protocol versions.
 */
internal class ProtocolTranslation(
  val from: MultistateProtocol,
  val to: MultistateProtocol
)

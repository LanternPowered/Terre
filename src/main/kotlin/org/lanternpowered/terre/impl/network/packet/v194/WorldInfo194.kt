/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet.v194

import org.lanternpowered.terre.impl.network.packet.WorldInfoDecoder
import org.lanternpowered.terre.impl.network.packet.WorldInfoEncoder

internal val WorldInfo194Encoder = WorldInfoEncoder(194)
internal val WorldInfo194Decoder = WorldInfoDecoder(194)

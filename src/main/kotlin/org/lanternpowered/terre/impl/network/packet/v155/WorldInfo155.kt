/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.packet.v155

import org.lanternpowered.terre.impl.network.packet.newWorldInfoDecoder
import org.lanternpowered.terre.impl.network.packet.newWorldInfoEncoder

internal val WorldInfo155Encoder = newWorldInfoEncoder(version155 = true)

internal val WorldInfo155Decoder = newWorldInfoDecoder(version155 = true)

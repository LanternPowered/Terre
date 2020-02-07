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

import org.lanternpowered.terre.impl.network.packet.AddPlayerBuffDecoder
import org.lanternpowered.terre.impl.network.packet.AddPlayerBuffEncoder

internal val AddPlayerBuff155Encoder = AddPlayerBuffEncoder(155)

internal val AddPlayerBuff155Decoder = AddPlayerBuffDecoder(155)

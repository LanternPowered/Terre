/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network.pipeline

/**
 * The id of the module packets.
 */
internal const val ModulePacketId = 0x52

internal const val ModuleIdOffset = 8
internal const val ModuleIdMask = (1 shl ModuleIdOffset) - 1

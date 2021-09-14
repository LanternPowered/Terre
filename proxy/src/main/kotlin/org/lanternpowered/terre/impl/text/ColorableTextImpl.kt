/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.text

import org.lanternpowered.terre.text.ColorableText
import org.lanternpowered.terre.util.Color

internal abstract class ColorableTextImpl : TextImpl(), ColorableText {

  final override val color: Color?
    get() = optionalColor.orNull()
}

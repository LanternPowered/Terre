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

import org.lanternpowered.terre.text.Glyph
import org.lanternpowered.terre.text.GlyphText
import org.lanternpowered.terre.util.ToStringHelper

internal data class GlyphTextImpl(override val glyph: Glyph) : TextImpl(), GlyphText {

  override val isEmpty: Boolean
    get() = false

  override fun toPlain(): String = "[Glyph: ${glyph.numericId}]"

  override fun toString() = ToStringHelper(GlyphText::class)
    .add("glyph", glyph)
    .toString()
}

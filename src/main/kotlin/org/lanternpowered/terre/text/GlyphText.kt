/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.text

import org.lanternpowered.terre.impl.text.GlyphTextImpl

/**
 * Creates a new [GlyphText] from the given [Glyph].
 */
fun textOf(glyph: Glyph): GlyphText
    = GlyphTextImpl(glyph)

/**
 * Represents a glyph text component.
 */
interface GlyphText : Text {

  /**
   * The glyph of the text component.
   */
  val glyph: Glyph
}

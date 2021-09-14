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

import org.lanternpowered.terre.catalog.NumericCatalogType
import org.lanternpowered.terre.catalog.NumericCatalogTypeRegistry
import org.lanternpowered.terre.impl.text.GlyphRegistryImpl

/**
 * Represents a glyph that can be displayed in chat.
 */
interface Glyph : NumericCatalogType, TextLike {

  override fun text(): GlyphText = textOf(this)
}

/**
 * A registry of all the [Glyph]s.
 */
object GlyphRegistry : NumericCatalogTypeRegistry<Glyph> by GlyphRegistryImpl

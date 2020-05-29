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

import org.lanternpowered.terre.impl.buildNumericCatalogTypeRegistryOf
import org.lanternpowered.terre.text.Glyph

internal data class GlyphImpl(
    override val numericId: Int
) : Glyph

internal val GlyphRegistryImpl = buildNumericCatalogTypeRegistryOf<Glyph>(::GlyphImpl)

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

import org.lanternpowered.terre.impl.text.TaggedTextSerializerImpl

/**
 * Represents the [TextSerializer] who converts between tagged strings and text objects.
 *
 * Tagged strings are strings where the tags are used to define items, glyphs, achievements,
 * colors, etc.
 *
 * See [Terraria Chat Tags](https://terraria.gamepedia.com/Chat#Tags)
 */
object TaggedTextSerializer : TextSerializer by TaggedTextSerializerImpl

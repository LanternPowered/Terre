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

/**
 * Represents the [TextSerializer] who converts between
 * plain strings and text objects.
 */
object PlainTextSerializer : TextSerializer {

  override fun serialize(text: Text) = text.toPlain()

  override fun deserialize(string: String) = string.text()
}

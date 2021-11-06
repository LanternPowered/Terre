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

import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.TextSerializer
import org.lanternpowered.terre.text.text

internal object TaggedTextSerializerImpl : TextSerializer {

  override fun serialize(text: Text): String =
    (text as TextImpl).toTaggedVanillaText().toPlain()

  override fun deserialize(string: String): Text =
    (string.text() as TextImpl).fromTaggedVanillaText()
}

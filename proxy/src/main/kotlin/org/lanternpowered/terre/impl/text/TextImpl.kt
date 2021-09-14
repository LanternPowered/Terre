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

import org.lanternpowered.terre.impl.util.OptionalColor
import org.lanternpowered.terre.text.GroupedText
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.util.collection.immutableListBuilderOf
import org.lanternpowered.terre.util.collection.immutableListOf

internal abstract class TextImpl : Text {

  internal open val optionalColor: OptionalColor
    get() = OptionalColor.empty()

  override fun plus(that: Text): Text {
    if (that.isEmpty) return this
    if (this.isEmpty) return that
    // Merge if possible to reduce complexity
    if (this.optionalColor.isEmpty
      && (that as TextImpl).optionalColor.isEmpty
      && (this is GroupedText || that is GroupedText)
    ) {
      val children = immutableListBuilderOf<Text>()
      if (this is GroupedText) {
        children.addAll(this.children)
      } else {
        children.add(this)
      }
      if (that is GroupedText) {
        children.addAll(that.children)
      } else {
        children.add(that)
      }
      return GroupedTextImpl(children.build())
    }
    return GroupedTextImpl(immutableListOf(this, that))
  }
}

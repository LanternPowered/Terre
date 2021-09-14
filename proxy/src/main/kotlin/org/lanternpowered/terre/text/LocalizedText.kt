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

import org.lanternpowered.terre.impl.text.LocalizedTextImpl
import org.lanternpowered.terre.util.Color
import org.lanternpowered.terre.util.collection.toImmutableList

/**
 * Constructs a localized text component with the given substitutions.
 */
fun localizedTextOf(key: String, vararg substitutions: Any): LocalizedText =
  localizedTextOf(key, substitutions.asList())

/**
 * Constructs a localized text component with the given substitutions.
 */
fun localizedTextOf(key: String, substitutions: Iterable<Any>): LocalizedText {
  val textSubstitutions = substitutions.asSequence()
    .map { it as? Text ?: it.toString().text() }
    .toImmutableList()
  return LocalizedTextImpl(key, textSubstitutions)
}

/**
 * Represents localized text.
 */
interface LocalizedText : ColorableText {

  /**
   * The key of the localized text.
   */
  val key: String

  /**
   * The substitutions of the text.
   */
  val substitutions: List<Text>

  override fun color(color: Color?): LocalizedText
}

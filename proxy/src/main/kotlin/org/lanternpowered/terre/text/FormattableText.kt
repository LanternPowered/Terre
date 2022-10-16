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

import org.lanternpowered.terre.impl.text.FormattableTextImpl
import org.lanternpowered.terre.util.collection.toImmutableList

/**
 * Constructs a formatted text component with the given substitutions.
 */
fun formattedTextOf(format: String, vararg substitutions: Any): FormattableText =
  formattedTextOf(format, substitutions.asList())

/**
 * Constructs a formatted text component with the given substitutions.
 */
fun formattedTextOf(format: String, substitutions: Iterable<Any>): FormattableText {
  val textSubstitutions = substitutions.asSequence()
    .map { substitution ->
      if (substitution is TextLike) substitution.text()
      else substitution.toString().text()
    }
    .toImmutableList()
  return FormattableTextImpl(format, textSubstitutions)
}

interface FormattableText : ColorableText {

  /**
   * The format that will be formatted using the substitutions.
   */
  val format: String

  /**
   * The substitutions of the text.
   */
  val substitutions: List<Text>
}

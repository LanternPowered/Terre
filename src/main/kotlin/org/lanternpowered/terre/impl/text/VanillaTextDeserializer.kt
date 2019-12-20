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

import org.lanternpowered.terre.catalog.NumericId
import org.lanternpowered.terre.impl.util.OptionalColor
import org.lanternpowered.terre.text.AchievementRegistry
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.textOf
import org.lanternpowered.terre.util.Color
import org.lanternpowered.terre.util.Namespace
import org.lanternpowered.terre.util.collection.toImmutableList
import org.lanternpowered.terre.impl.util.optional
import org.lanternpowered.terre.text.GlyphRegistry
import java.util.*

/**
 * Converts the tagged vanilla text to an actual object.
 */
fun TextImpl.fromTaggedVanillaText(): Text {
  return when (this) {
    is GroupedTextImpl -> {
      val builder = TextBuilder()
      for (child in this.children) {
        builder.append((child as TextImpl).fromTaggedVanillaText())
      }
      builder.build()
    }
    is LiteralTextImpl -> {
      val builder = TextBuilder()
      fromTaggedVanillaFormat(this.literal, builder)
      builder.build()
    }
    is FormattableTextImpl -> {
      val builder = TextBuilder(this.substitutions)
      fromTaggedVanillaFormat(this.format, builder)
      builder.build()
    }
    else -> this
  }
}

private val tagRegex = "(?<!\\\\)\\[([a-zA-Z]{1,10})(?:/([^:]+))?:(.+?)(?<!\\\\)]".toRegex()

private fun fromTaggedVanillaFormat(format: String, builder: TextBuilder) {
  val matches = tagRegex.findAll(format)
  var last = 0

  // Now, new substitutions need to be handled
  for (match in matches) {
    val start = match.range.first
    val end = match.range.last + 1

    val type = match.groupValues[1]
    val options = match.groupValues[2]
    val value = match.groupValues[3]

    if (last < start) {
      builder.append(format.substring(last, start), OptionalColor.empty())
    }

    when (type) {
      "a" -> {
        val achievement = AchievementRegistry[Namespace.Terre.id(value.toLowerCase(Locale.ROOT))]
        if (achievement == null) {
          builder.append(value)
        } else {
          builder.append(AchievementTextImpl(achievement))
        }
      }
      "c" -> {
        val rgb = options.toIntOrNull(radix = 16)
        val color = if (rgb != null) Color(rgb).optional() else OptionalColor.empty()
        builder.append(value, color)
      }
      "g" -> {
        val internalId = value.toIntOrNull()
        val glyph = if (internalId != null) GlyphRegistry[NumericId(internalId)] else null
        if (glyph == null) {
          builder.append(value)
        } else {
          builder.append(GlyphTextImpl(glyph))
        }
      }
      "i" -> {
        TODO("Implement item text")
        // builder.append(ItemTextImpl(itemStack))
      }
      "n" -> builder.append('<' + value.replace("\\[", "[").replace("\\]", "]") + '>')
      else -> builder.append(value)
    }

    last = end
  }

  if (last < format.length) {
    builder.append(format.substring(last, format.length))
  }
}

private class TextBuilder(val substitutions: List<Text> = listOf()) {

  private val parts = mutableListOf<Text>()

  private val currentBuilder = StringBuilder()
  private var currentColor = OptionalColor.empty()
  private val currentSubstitutions = mutableListOf<Text>()

  private fun addRemaining() {
    if (this.currentBuilder.isEmpty()) {
      return
    }
    val content = this.currentBuilder.toString()
    if (this.currentSubstitutions.isNotEmpty()) {
      this.parts += FormattableTextImpl(content, this.currentSubstitutions.toImmutableList(), this.currentColor)
    } else {
      this.parts += LiteralTextImpl(content, this.currentColor)
    }
    this.currentBuilder.setLength(0)
    this.currentColor = OptionalColor.empty()
    this.currentSubstitutions.clear()
  }

  fun append(text: String, color: OptionalColor = OptionalColor.empty()) {
    if (this.currentColor != color) {
      addRemaining()
    }
    val matches = formatPattern.findAll(text).iterator()
    this.currentColor = color
    if (!matches.hasNext()) {
      // No matches, just append the text
      this.currentBuilder.append(text)
    } else {
      var last = 0

      // Now, new substitutions need to be handled
      for (match in matches) {
        val start = match.range.first
        val end = match.range.last + 1

        val index = match.groupValues[1].toInt()
        val substitution = if (index < this.substitutions.size) this.substitutions[index] else null

        if (last < start) {
          this.currentBuilder.append(text, last, start)
        }

        if (substitution == null) {
          this.currentBuilder.append(match.value)
        } else {
          this.currentBuilder.append('{').append(this.currentSubstitutions.size).append('}')
          this.currentSubstitutions += substitution
        }

        last = end
      }

      if (last < text.length) {
        this.currentBuilder.append(text, last, text.length)
      }
    }
  }

  fun append(text: Text) {
    addRemaining()
    this.parts += text
  }

  fun build(): Text {
    addRemaining()
    return when {
      this.parts.isEmpty() -> textOf()
      this.parts.size == 1 -> this.parts[0]
      else -> GroupedTextImpl(this.parts.toImmutableList())
    }
  }
}

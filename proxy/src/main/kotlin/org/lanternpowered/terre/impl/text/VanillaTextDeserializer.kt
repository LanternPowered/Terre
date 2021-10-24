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
import org.lanternpowered.terre.text.AchievementRegistry
import org.lanternpowered.terre.text.Text
import org.lanternpowered.terre.text.textOf
import org.lanternpowered.terre.util.Color
import org.lanternpowered.terre.util.collection.toImmutableList
import org.lanternpowered.terre.impl.util.optional
import org.lanternpowered.terre.item.ItemModifier
import org.lanternpowered.terre.item.ItemModifierRegistry
import org.lanternpowered.terre.item.ItemTypeRegistry
import org.lanternpowered.terre.item.ItemStack
import org.lanternpowered.terre.text.GlyphRegistry

/**
 * Converts the tagged vanilla text to an actual object.
 */
internal fun TextImpl.fromTaggedVanillaText(): Text {
  return when (this) {
    is GroupedTextImpl -> {
      val builder = TextBuilder()
      for (child in children)
        builder.append((child as TextImpl).fromTaggedVanillaText())
      builder.build()
    }
    is LiteralTextImpl -> {
      val builder = TextBuilder()
      fromTaggedVanillaFormat(literal, builder)
      builder.build()
    }
    is FormattableTextImpl -> {
      val builder = TextBuilder(substitutions)
      fromTaggedVanillaFormat(format, builder)
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

    if (last < start)
      builder.append(format.substring(last, start), OptionalColor.empty())

    when (type) {
      "a" -> {
        val achievement = AchievementRegistry[value.lowercase()]
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
        val glyph = if (internalId != null) GlyphRegistry[internalId] else null
        if (glyph == null) {
          builder.append(value)
        } else {
          builder.append(GlyphTextImpl(glyph))
        }
      }
      "i" -> {
        val internalId = value.toIntOrNull()
        val item = if (internalId != null) ItemTypeRegistry[internalId] else null
        val optionList = options.split(",")
          .filter { it.isNotEmpty() }
        val quantity = optionList.firstOrNull { it[0] == 's' || it[0] == 'x' }
          ?.substring(1)?.toIntOrNull() ?: 1
        if (item == null) {
          builder.append("[$value")
          if (quantity > 1)
            builder.append(" ($quantity)")
          builder.append("]")
        } else {
          val modifierId = optionList.firstOrNull { it[0] == 'p' }
            ?.toIntOrNull()?.coerceIn(0..10000) ?: 0
          val modifier = ItemModifierRegistry[modifierId] ?: ItemModifier.Default
          builder.append(ItemTextImpl(ItemStack(item, modifier, quantity)))
        }
      }
      "n" -> builder.append('<' + value
        .replace("\\[", "[")
        .replace("\\]", "]") + '>')
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
    if (currentBuilder.isEmpty())
      return
    val content = currentBuilder.toString()
    if (currentSubstitutions.isNotEmpty()) {
      parts += FormattableTextImpl(content, currentSubstitutions.toImmutableList(), currentColor)
    } else {
      parts += LiteralTextImpl(content, currentColor)
    }
    currentBuilder.setLength(0)
    currentColor = OptionalColor.empty()
    currentSubstitutions.clear()
  }

  fun append(text: String, color: OptionalColor = OptionalColor.empty()) {
    if (currentColor != color)
      addRemaining()
    val matches = formatPattern.findAll(text).iterator()
    currentColor = color
    if (!matches.hasNext()) {
      // No matches, just append the text
      currentBuilder.append(text)
    } else {
      var last = 0

      // Now, new substitutions need to be handled
      for (match in matches) {
        val start = match.range.first
        val end = match.range.last + 1

        val index = match.groupValues[1].toInt()
        val substitution = if (index < substitutions.size) substitutions[index] else null

        if (last < start)
          currentBuilder.append(text, last, start)

        if (substitution == null) {
          currentBuilder.append(match.value)
        } else {
          currentBuilder.append('{').append(currentSubstitutions.size).append('}')
          currentSubstitutions += substitution
        }

        last = end
      }

      if (last < text.length)
        currentBuilder.append(text, last, text.length)
    }
  }

  fun append(text: Text) {
    addRemaining()
    parts += text
  }

  fun build(): Text {
    addRemaining()
    return when {
      parts.isEmpty() -> textOf()
      parts.size == 1 -> parts[0]
      else -> GroupedTextImpl(parts.toImmutableList())
    }
  }
}

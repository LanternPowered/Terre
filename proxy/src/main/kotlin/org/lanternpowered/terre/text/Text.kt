/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("NOTHING_TO_INLINE")

package org.lanternpowered.terre.text

import org.lanternpowered.terre.impl.text.GroupedTextImpl
import org.lanternpowered.terre.impl.text.LiteralTextImpl
import org.lanternpowered.terre.impl.util.optional
import org.lanternpowered.terre.impl.util.optionalFromNullable
import org.lanternpowered.terre.util.Color
import org.lanternpowered.terre.util.collection.immutableListBuilderOf
import org.lanternpowered.terre.util.collection.immutableListOf
import org.lanternpowered.terre.util.collection.toImmutableList

private val Empty = textOf("")

/**
 * Converts the string into a literal text.
 */
inline fun String.text(): LiteralText
    = textOf(this)

/**
 * Gets empty text.
 */
fun textOf(): LiteralText = Empty

/**
 * Converts the string into a literal text.
 */
fun textOf(literal: String): LiteralText
    = LiteralTextImpl(literal)

/**
 * Converts the string into a literal text.
 */
fun textOf(literal: String, color: Color): LiteralText
    = LiteralTextImpl(literal, color.optional())

/**
 * Converts the string into a literal text.
 */
fun textOf(literal: String, color: Color?): LiteralText
    = LiteralTextImpl(literal, color.optionalFromNullable())

/**
 * Merges the array of text components
 * into a single one.
 */
fun textOf(array: Array<Text>): Text
    = textOf(array.toImmutableList())

/**
 * Merges the array of text components
 * into a single one.
 */
fun textOf(first: Text, second: Text, vararg more: Text): Text {
  val moreList = more.asList()
  if (moreList.isEmpty()) {
    return GroupedTextImpl(immutableListOf(first, second));
  }
  val builder = immutableListBuilderOf<Text>(expectedSize = 2 + more.size)
  builder.add(first)
  builder.add(second)
  builder.addAll(moreList)
  return GroupedTextImpl(builder.build())
}

/**
 * Merges the iterable of text components
 * into a single one.
 */
fun textOf(sequence: Sequence<Text>): Text {
  val it = sequence.iterator()
  if (!it.hasNext()) {
    return textOf()
  }
  val first = it.next()
  if (!it.hasNext()) {
    return first
  }
  val builder = immutableListBuilderOf<Text>()
  builder.add(first)
  it.forEachRemaining { text ->
    builder.add(text)
  }
  return GroupedTextImpl(builder.build())
}

/**
 * Merges the iterable of text components
 * into a single one.
 */
fun textOf(iterable: Iterable<Text>): Text {
  val collection = iterable as? Collection ?: iterable.toImmutableList()
  if (collection.isEmpty()) {
    return textOf()
  }
  if (collection.size == 1) {
    return collection.iterator().next()
  }
  return GroupedTextImpl(collection.toImmutableList())
}

/**
 * Represents a text component.
 */
interface Text {

  /**
   * Whether this text component is empty.
   */
  val isEmpty: Boolean

  /**
   * Converts the text into a plain string,
   * without any coloring or formatting.
   */
  fun toPlain(): String

  /**
   * Appends that text to this and returns the new text.
   */
  operator fun plus(that: Text): Text
}

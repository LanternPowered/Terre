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

import org.lanternpowered.terre.impl.text.GroupedTextImpl

/**
 * Creates a string from all the elements separated using separator and using the given prefix and
 * postfix if supplied. If the collection could be huge, you can specify a non-negative value of
 * limit, in which case  only the first limit elements will be appended, followed by the
 * truncated string (which defaults to "...").
 *
 * Works similar to [joinToString].
 */
fun <T> Iterable<T>.joinToText(
  separator: TextLike = ", ".text(),
  prefix: TextLike = "".text(),
  postfix: TextLike = "".text(),
  limit: Int = -1,
  truncated: TextLike = "...".text(),
  transform: ((T) -> TextLike)? = null
): Text {
  val textList = ArrayList<Text>()
  fun add(textLike: TextLike) {
    val text = textLike.text()
    if (!text.isEmpty)
      textList.add(text)
  }
  add(prefix)
  var count = 0
  for (element in this) {
    if (++count > 1)
      add(separator)
    if (limit < 0 || count <= limit) {
      val text = when {
        transform != null -> transform(element)
        element is TextLike -> element
        else -> element.toString().text()
      }
      add(text)
    } else break
  }
  if (limit in 0 until count)
    add(truncated)
  add(postfix)
  return when {
    textList.isEmpty() -> textOf()
    textList.size == 1 -> textList[0]
    else -> GroupedTextImpl(textList)
  }
}

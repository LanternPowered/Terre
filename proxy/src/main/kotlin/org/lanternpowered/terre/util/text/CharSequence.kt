/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.util.text

/**
 * Returns the index within this string of the first occurrence of the specified character, starting
 * from the specified [startIndex] and ending at the specified [endIndex].
 *
 * @param ignoreCase `true` to ignore character case when matching a character. By default, `false`.
 * @return An index of the first occurrence of [char] or -1 if none is found.
 */
fun CharSequence.indexOf(
  char: Char, startIndex: Int = 0, endIndex: Int = length, ignoreCase: Boolean = false
): Int {
  val index = indexOf(char, startIndex, ignoreCase)
  if (index >= endIndex)
    return -1
  return index
}

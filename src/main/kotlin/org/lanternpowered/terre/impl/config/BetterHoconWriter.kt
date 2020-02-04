/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.config

import com.uchuhimo.konf.source.Writer
import java.io.OutputStream
import java.io.OutputStreamWriter

/**
 * An improved hocon writer that allows a
 * custom indentation size.
 */
internal class BetterHoconWriter(
    private val original: Writer,
    private val indentSize: Int = 4
) : Writer {

  override fun toOutputStream(outputStream: OutputStream) {
    toWriter(OutputStreamWriter(outputStream))
  }

  override fun toWriter(writer: java.io.Writer) {
    writer.write(toText())
  }

  override fun toText(): String {
    val text = this.original.toText()
    if (this.indentSize == 4) // Is the same as the original indent
      return text
    return text.split("\n")
        .asSequence()
        .map { line ->
          val whitespaces = line.takeWhile { char -> char == ' ' }.count()
          // original indent is 4, decrease this to 2
          " ".repeat((whitespaces / 4) * this.indentSize) + line.substring(whitespaces)
        }
        .joinToString(separator = "\n")
  }
}

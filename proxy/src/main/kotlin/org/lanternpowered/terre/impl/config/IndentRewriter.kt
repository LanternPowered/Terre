/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("FunctionName")

package org.lanternpowered.terre.impl.config

import com.uchuhimo.konf.source.Writer
import java.io.OutputStream
import java.io.OutputStreamWriter

internal fun HoconIndentRewriter(original: Writer, indentSize: Int = 4): IndentRewriter {
  return IndentRewriter(original, 4, indentSize)
}

/**
 * A writer that allows the indentation to be rewritten.
 */
internal class IndentRewriter(
    private val original: Writer,
    private val originalIndentSize: Int,
    private val indentSize: Int
) : Writer {

  override fun toOutputStream(outputStream: OutputStream) {
    toWriter(OutputStreamWriter(outputStream))
  }

  override fun toWriter(writer: java.io.Writer) {
    writer.write(toText())
  }

  override fun toText(): String {
    val text = this.original.toText()
    if (this.indentSize == this.originalIndentSize) // Is the same as the original indent
      return text
    return text.split("\n")
        .asSequence()
        .map { line ->
          val whitespaces = line.takeWhile { char -> char == ' ' }.count()
          " ".repeat((whitespaces / this.originalIndentSize) * this.indentSize) + line.substring(whitespaces)
        }
        .joinToString(separator = "\n")
  }
}

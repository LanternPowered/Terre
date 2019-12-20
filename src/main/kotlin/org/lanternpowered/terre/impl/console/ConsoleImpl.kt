/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.console

import net.minecrell.terminalconsole.SimpleTerminalConsole
import net.minecrell.terminalconsole.TerminalConsoleAppender
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.io.IoBuilder
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.lanternpowered.terre.Console
import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.text.Text

internal class ConsoleImpl(
    val commandHandler: (command: String) -> Unit,
    val shutdownHandler: () -> Unit
) : SimpleTerminalConsole(), Console {

  @Volatile private var active = false
  private var readThread: Thread? = null
  private val lock = Object()

  init {
    LocationPatternConverter.RedirectFqcns += this::class.java.name
  }

  override fun sendMessage(message: Text) {
    sendMessage(message.toPlain())
  }

  override fun sendMessage(message: String) {
    Terre.logger.info(message)
  }

  override fun start() {
    System.setOut(IoBuilder.forLogger(Terre.logger).setLevel(Level.INFO).buildPrintStream())
    System.setErr(IoBuilder.forLogger(Terre.logger).setLevel(Level.ERROR).buildPrintStream())

    Terre.logger.info("Starting console.")
    val readThread = Thread({
      synchronized(this.lock) {
        super.start()
      }
    }, "console")
    this.readThread = readThread
    this.active = true
    readThread.isDaemon = true
    readThread.start()
  }

  fun stop() {
    synchronized(this.lock) {
      val readThread = this.readThread ?: return@synchronized

      this.active = false
      readThread.interrupt()

      val terminal = TerminalConsoleAppender.getTerminal()
      terminal?.writer()?.println()

      this.readThread = null
    }
  }

  override fun buildReader(builder: LineReaderBuilder): LineReader
      = super.buildReader(builder.appName(Terre.name))

  override fun isRunning() = this.active

  override fun runCommand(command: String) {
    this.commandHandler(command)
  }

  override fun shutdown() {
    this.shutdownHandler()
  }
}

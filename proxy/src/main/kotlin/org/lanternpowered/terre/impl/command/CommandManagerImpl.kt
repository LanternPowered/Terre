/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.command

import org.lanternpowered.terre.command.CommandManager
import org.lanternpowered.terre.command.CommandSource
import org.lanternpowered.terre.command.RawCommandExecutor
import org.lanternpowered.terre.command.SimpleCommandExecutor

internal object CommandManagerImpl : CommandManager {

  private val byName = hashMapOf<String, RawCommandExecutor>()

  init {
    register("connect", ConnectCommand)
  }

  override fun register(name: String, executor: RawCommandExecutor) {
    byName[name] = executor
  }

  override fun register(name: String, executor: SimpleCommandExecutor) {
    register(name, RawCommandExecutor { source, name1, args ->
      val argsList = args.split(" ").filter { it.isNotEmpty() }
      executor.execute(source, name1, argsList)
    })
  }

  suspend fun execute(source: CommandSource, command: String): Boolean {
    val index = command.indexOf(' ')
    val name = if (index == -1) command else command.substring(0, index)
    if (name.isBlank())
      return false
    val args = if (index == -1) "" else command.substring(index + 1)
    val executor = byName[name]
    if (executor != null) {
      executor.execute(source, name, args)
      return true
    }
    return false
  }
}

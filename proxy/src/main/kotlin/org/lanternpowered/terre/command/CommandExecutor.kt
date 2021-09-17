/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.command

fun interface RawCommandExecutor {

  /**
   * Executes the raw command.
   *
   * @param source The source which triggered the command
   * @param alias The alias of the command that was executed
   * @param args The arguments of the command
   */
  suspend fun execute(source: CommandSource, alias: String, args: String)
}

fun interface SimpleCommandExecutor {

  /**
   * Executes the raw command.
   *
   * @param source The source which triggered the command
   * @param alias The alias of the command that was executed
   * @param args The arguments of the command
   */
  suspend fun execute(source: CommandSource, alias: String, args: List<String>)
}

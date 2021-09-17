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

import org.lanternpowered.terre.impl.command.CommandManagerImpl

interface CommandManager {

  // TODO: Replace with kommando

  fun register(name: String, executor: RawCommandExecutor)

  fun register(name: String, executor: SimpleCommandExecutor)

  companion object : CommandManager by CommandManagerImpl
}

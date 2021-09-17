/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.event

import org.lanternpowered.terre.event.Event

interface UntargetedEventHandler {

  suspend fun handle(target: Any, event: Event)

  interface NoSuspend : UntargetedEventHandler {

    override suspend fun handle(target: Any, event: Event) = handleDirect(target, event)

    fun handleDirect(target: Any, event: Event)
  }
}

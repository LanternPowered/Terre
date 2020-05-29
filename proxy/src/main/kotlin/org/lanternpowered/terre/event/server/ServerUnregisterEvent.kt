/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.event.server

import org.lanternpowered.terre.Server
import org.lanternpowered.terre.event.Event

/**
 * This event is thrown when a server got unregistered.
 */
class ServerUnregisterEvent(val server: Server) : Event

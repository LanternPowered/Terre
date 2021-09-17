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
import org.lanternpowered.terre.event.proxy.ProxyInitializeEvent

/**
 * This event is thrown when a server got registered. This event is only thrown for servers that
 * got registered after the proxy was initialized. All other servers will already be available
 * when the [ProxyInitializeEvent] gets thrown.
 */
class ServerRegisterEvent(val server: Server) : Event

/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.event.connection

import org.lanternpowered.terre.InboundConnection
import org.lanternpowered.terre.event.Event

/**
 * An event that's thrown when a new connection
 * is made to the proxy.
 */
data class ConnectionHandshakeEvent(
    val inboundConnection: InboundConnection
) : Event

/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.tshock.group

import org.lanternpowered.terre.tshock.TShockMessages
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TShockMessagesTest {

  @Test
  fun authenticatedMessage() {
    val user = "User"
    var extracted = TShockMessages.findAuthenticatedUser("Authenticated as $user successfully.")
    assertEquals(user, extracted)
    extracted = TShockMessages.findAuthenticatedUser("El jugador $user ya está conectado.")
    assertEquals(user, extracted)
    extracted = TShockMessages.findAuthenticatedUser("Authenticated as $user successfully. More.")
    assertNull(extracted)
  }
}

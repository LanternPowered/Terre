/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.tshock

import org.lanternpowered.terre.text.Text

sealed interface LoginResult {

  object Success : LoginResult

  data class Denied(val reason: Text) : LoginResult

  object AlreadyLoggedIn : LoginResult
}

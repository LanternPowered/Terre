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

import com.uchuhimo.konf.ConfigSpec

object TShockUsersConfigSpec : ConfigSpec("tShockUsers") {

  object Database : ConfigSpec("database") {

    val host by optional(
      default = "localhost",
      description = "The database host."
    )

    val port by optional(
      default = 3306,
      description = "The database port."
    )

    val database by optional(
      default = "",
      description = "The database name. Defaults to user name."
    )

    val user by optional(
      default = "",
      description = "The database user."
    )

    val password by optional(
      default = "",
      description = "The database user password."
    )
  }
}

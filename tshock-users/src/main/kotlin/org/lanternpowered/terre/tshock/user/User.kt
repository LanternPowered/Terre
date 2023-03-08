/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.tshock.user

import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.UUID

data class User(
  val id: Int,
  val name: String,
  val password: String,
  val clientIdentifier: String,
  val group: String,
  val registered: LocalDateTime,
  val lastAccessed: LocalDateTime,
  val knownIPs: String,
) {

  companion object {

    /**
     * Generates a user identifier from the given [clientUniqueId].
     */
    fun generateIdentifier(clientUniqueId: UUID): String {
      val digest = MessageDigest.getInstance("SHA-512")
      digest.reset()
      digest.update(clientUniqueId.toString().toByteArray(Charsets.UTF_8))
      return digest.digest().joinToString(separator = "") {
        it.toUByte().toString(radix = 16).padStart(2, '0').uppercase()
      }
    }
  }
}

/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre

/**
 * Represents an identifier that can be used
 * to identify players.
 */
class PlayerIdentifier {

  /**
   * Create a safe clone.
   */
  private val backing: ByteArray

  /**
   * Constructs a new [PlayerIdentifier] from the given [ByteArray].
   */
  constructor(bytes: ByteArray) : this(bytes, Unit)

  /**
   * Constructs a new [PlayerIdentifier] from the given [ByteArray].
   */
  private constructor(bytes: ByteArray, unit: Unit) {
    this.backing = bytes
  }

  /**
   * The string representation of the identifier.
   */
  private val toString by lazy(::convertToString)

  private fun convertToString() = this.backing.joinToString { it.toString(radix = 16) }

  /**
   * Gets the backing bytes of the identifier.
   */
  val bytes: ByteArray
    get() = this.backing.clone()

  /**
   * Gets the string representation of the identifier.
   */
  override fun toString() = this.toString

  override fun equals(other: Any?)
      = other is PlayerIdentifier && other.backing contentEquals this.backing

  override fun hashCode()
      = this.backing.contentHashCode()

  companion object {

    /**
     * Attempts to parse the player identifier.
     *
     * @param value The value to parse
     * @return The parsed identifier
     */
    fun parse(value: String): PlayerIdentifier {
      val backing = ByteArray(value.length)
      for ((index, char) in value.withIndex()) {
        val parsed = char.toString().toIntOrNull(radix = 16) ?: error("Invalid character: $char")
        backing[index] = parsed.toByte()
      }
      return PlayerIdentifier(backing, Unit)
    }
  }
}

/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("UNCHECKED_CAST", "OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE")

package org.lanternpowered.terre.impl.util

/**
 * Represents an optional object. The actual value can be `null`.
 */
internal interface Optional<T> {

  /**
   * Whether the value is present.
   */
  val isPresent: Boolean

  /**
   * Whether no value is present.
   */
  val isEmpty: Boolean
    get() = !this.isPresent

  /**
   * Gets the value.
   *
   * @throws IllegalStateException If no value is present
   */
  val value: T

  /**
   * Unwraps this optional as a nullable object.
   */
  fun orNull(): T? =
    if (this.isPresent) this.value else null

  /**
   * Gets the value, if present, returns otherwise `that` value.
   */
  fun or(that: T): T =
    if (this.isPresent) this.value else that

  /**
   * Gets this optional, if the value is present, returns otherwise `that` optional.
   */
  fun or(that: Optional<T>): Optional<T> =
    if (this.isPresent) this else that

  /**
   * Maps the value.
   */
  fun <R> map(fn: (value: T) -> R): Optional<R> =
    if (this.isPresent) of(fn(this.value)) else empty()

  companion object {

    /**
     * Gets an empty optional.
     */
    fun <T> empty(): Optional<T> = EmptyOptional as Optional<T>

    /**
     * Wraps the value into an optional.
     */
    fun <T> of(value: T): Optional<T> = PresentOptional(value)

    /**
     * Wraps the nullable value into an optional.
     */
    fun <T : Any> ofNullable(value: T?): Optional<T> =
      if (value == null) EmptyOptional as Optional<T> else PresentOptional(value)
  }
}

/**
 * Gets the value, if present, returns otherwise
 * the value provided by the function.
 */
internal inline fun <T> Optional<T>.or(fn: () -> T): T =
  if (this.isPresent) this.value else fn()

/**
 * Wraps the value into an optional.
 */
internal inline fun <T> T.optional(): Optional<T> =
  Optional.of(this)

/**
 * Wraps the nullable value into an optional.
 */
@JvmName("optionalOfNullable")
internal inline fun <T : Any> T?.optionalFromNullable(): Optional<T> =
  Optional.ofNullable(this)

/**
 * A simple optional implementation where the value is present.
 */
private data class PresentOptional<T>(override val value: T) : Optional<T> {
  override val isPresent: Boolean
    get() = true

  override fun toString(): String = "Optional($value)"
}

/**
 * A simple optional implementation where the value is absent.
 */
private object EmptyOptional : Optional<Any?> {
  override val isPresent: Boolean
    get() = false
  override val value: Any
    get() = throw IllegalStateException("No value is present.")

  override fun toString(): String = "Optional.empty"
}

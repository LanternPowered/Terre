/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("FunctionName")

package org.lanternpowered.terre.util

import com.google.common.collect.Iterables
import java.lang.UnsupportedOperationException
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass

/**
 * Creates a new string.
 */
fun Any.toString(
  brackets: ToStringHelper.Brackets = ToStringHelper.Brackets.ROUND,
  omitNullValues: Boolean = false,
  fn: ToStringHelper.() -> Unit
): String {
  contract {
    callsInPlace(fn, InvocationKind.EXACTLY_ONCE)
  }
  return ToStringHelper(this::class.getDefaultName(), brackets, omitNullValues).also(fn).toString()
}

/**
 * Creates a new string.
 */
fun toString(
  name: String,
  brackets: ToStringHelper.Brackets = ToStringHelper.Brackets.ROUND,
  omitNullValues: Boolean = false,
  fn: ToStringHelper.() -> Unit
): String {
  contract {
    callsInPlace(fn, InvocationKind.EXACTLY_ONCE)
  }
  return ToStringHelper(name, brackets, omitNullValues).also(fn).toString()
}

/**
 * Gets the default name used for a class.
 */
private fun KClass<*>.getDefaultName(): String {
  val enclosing = nestedClasses.javaClass.enclosingClass
  return if (enclosing == null) {
    simpleName ?: java.simpleName
  } else {
    enclosing.kotlin.getDefaultName() + '.' + simpleName
  }
}

/**
 * A helper class to build [String]s for
 * classes with properties, etc.
 *
 * @param className The name of the class
 * @param brackets The brackets that should be added around the joined entries
 * @param omitNullValues Whether null values should be omitted
 * @param nameValueSeparator The separator that is used to join a key with its value
 * @param entrySeparator The separator that is used to join multiple key-value pairs
 */
class ToStringHelper(
  private val className: String = "",
  private var brackets: Brackets = Brackets.ROUND,
  private var omitNullValues: Boolean = false,
  private var nameValueSeparator: String = "=",
  private var entrySeparator: String = ", "
) {

  private var first: Entry? = null
  private var last: Entry? = null

  /**
   * Constructs a new [ToStringHelper] with the
   * simple name of the given [Class].
   */
  constructor(javaClass: Class<*>): this(javaClass.kotlin)

  /**
   * Constructs a new [ToStringHelper] with the
   * simple name of the given [KClass].
   */
  constructor(kClass: KClass<*>): this(kClass.getDefaultName())

  /**
   * Constructs a new [ToStringHelper] with the
   * simple name of the given object.
   */
  constructor(self: Any): this(self::class)

  /**
   * Applies changes to this [ToStringHelper].
   */
  operator fun invoke(function: ToStringHelper.() -> Unit) = apply { function.invoke(this) }

  /**
   * Adds a key-value pair at the first position (first is earlier).
   *
   * @param key The key to add
   * @param value The value to add
   * @return This helper, for chaining
   */
  fun addFirst(key: String, value: Any?): ToStringHelper = addFirstEntry(key, value)

  /**
   * Adds a value without a key at the first position (first is earlier).
   *
   * @param value The value to add
   * @return This helper, for chaining
   */
  fun addFirstValue(value: Any?): ToStringHelper = addFirstEntry(null, value)

  /**
   * Adds a key-value pair.
   *
   * @param key The key to add
   * @param value The value to add
   * @return This helper, for chaining
   */
  fun add(key: String, value: Any?): ToStringHelper = addEntry(key, value)

  @Suppress("DeprecatedCallableAddReplaceWith")
  @Deprecated(message = "Not allowed.", level = DeprecationLevel.ERROR)
  infix fun <A, B> A.to(@Suppress("UNUSED_PARAMETER") that: B): Nothing =
    throw UnsupportedOperationException()

  /**
   * Adds a key-value pair.
   *
   * @param value The value to add
   * @return This helper, for chaining
   */
  infix fun String.to(value: Any?) {
    add(this, value)
  }

  /**
   * Adds a value without a key.
   *
   * @param value The value to add
   * @return This helper, for chaining
   */
  fun addValue(value: Any?) = addEntry(null, value)

  /**
   * Sets whether null values should be omitted.
   *
   * @return This helper, for chaining
   */
  fun omitNullValues() = apply {
    this.omitNullValues = true
  }

  /**
   * Sets the brackets that should be added around the joined entries.
   *
   * @param brackets The brackets
   * @return This helper, for chaining
   */
  fun brackets(brackets: Brackets) = apply {
    this.brackets = brackets
  }

  /**
   * Sets the separator that is used to join multiple key-value pairs
   *
   * @param entrySeparator The entry separator
   * @return This helper, for chaining
   */
  fun entrySeparator(entrySeparator: String) = apply {
    this.entrySeparator = entrySeparator
  }

  /**
   * Sets the separator that is used to join a key with its value.
   *
   * @param nameValueSeparator The name-value separator
   * @return This helper, for chaining
   */
  fun nameValueSeparator(nameValueSeparator: String) = apply {
    this.nameValueSeparator = nameValueSeparator
  }

  private fun addFirstEntry(key: String?, value: Any?) = apply {
    val entry = createEntry(key, value)
    if (first == null) {
      first = entry
      last = entry
    } else {
      entry.next = first
      first = entry
    }
  }

  private fun addEntry(key: String?, value: Any?) = apply {
    val entry = createEntry(key, value)
    if (first == null) {
      first = entry
      last = entry
    } else {
      last!!.next = entry
      last = entry
    }
  }

  private fun createEntry(key: String?, value: Any?): Entry {
    var value1 = value
    if (value1 is Iterable<*>)
      value1 = Iterables.toString(value1)
    return Entry(key, value1)
  }

  /**
   * Builds the [String].
   *
   * @return The built string
   */
  override fun toString(): String {
    val builder = StringBuilder(className).append(brackets.open)
    var entry = first
    while (entry != null) {
      if (hasValue(entry)) {
        if (entry != first)
          builder.append(entrySeparator)
        if (entry.key != null)
          builder.append(entry.key).append(nameValueSeparator)
        var value = entry.value.toString()
        if (value.indexOfAny(quotedChars) != -1)
          value = "'$value'"
        builder.append(value)
      }
      entry = entry.next
    }
    return builder.append(brackets.close).toString()
  }

  private fun hasValue(entry: Entry?): Boolean =
    entry != null && (!omitNullValues || entry.value != null)

  /**
   * The different kind of brackets.
   */
  enum class Brackets(
    internal val open: Char,
    internal val close: Char
  ) {
    ROUND   ('(', ')'),
    CURLY   ('{', '}'),
    SQUARE  ('[', ']'),
  }

  private data class Entry(val key: String?, val value: Any?, var next: Entry? = null)

  companion object {

    private val quotedChars = charArrayOf(',', ' ')
  }
}

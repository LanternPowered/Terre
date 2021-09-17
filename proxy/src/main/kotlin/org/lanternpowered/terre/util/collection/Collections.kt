/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("NOTHING_TO_INLINE")

package org.lanternpowered.terre.util.collection

import com.google.common.collect.ImmutableCollection
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import java.util.EnumSet
import kotlin.Comparator
import kotlin.reflect.KClass

inline fun <E : Any> Array<E>.toImmutableList(): ImmutableList<E> =
  ImmutableList.copyOf(this)

fun <E : Any> Iterable<E>.toImmutableList(): ImmutableList<E> =
  this as? ImmutableList<E> ?: ImmutableList.copyOf(this)

fun <E : Any> Iterable<E>.toImmutableCollection(): ImmutableCollection<E> =
  this as? ImmutableCollection<E> ?: ImmutableList.copyOf(this)

inline fun <E : Any> immutableListOf(): ImmutableList<E> =
  ImmutableList.of()

inline fun <E : Any> immutableListOf(element: E): ImmutableList<E> =
  ImmutableList.of(element)

inline fun <E : Any> immutableListOf(e1: E, e2: E): ImmutableList<E> =
  ImmutableList.of(e1, e2)

inline fun <E : Any> immutableListOf(e1: E, e2: E, e3: E): ImmutableList<E> =
  ImmutableList.of(e1, e2, e3)

inline fun <E : Any> immutableListOf(e1: E, e2: E, e3: E, e4: E): ImmutableList<E> =
  ImmutableList.of(e1, e2, e3, e4)

inline fun <E : Any> immutableListOf(vararg elements: E): ImmutableList<E> =
  ImmutableList.copyOf(elements.asList())

inline fun <E : Any> immutableSetOf(): ImmutableSet<E> =
  ImmutableSet.of()

inline fun <E : Any> immutableSetOf(element: E): ImmutableSet<E> =
  ImmutableSet.of(element)

inline fun <E : Any> immutableSetOf(e1: E, e2: E): ImmutableSet<E> =
  ImmutableSet.of(e1, e2)

inline fun <E : Any> immutableSetOf(e1: E, e2: E, e3: E): ImmutableSet<E> =
  ImmutableSet.of(e1, e2, e3)

inline fun <E : Any> immutableSetOf(e1: E, e2: E, e3: E, e4: E): ImmutableSet<E> =
  ImmutableSet.of(e1, e2, e3, e4)

inline fun <E : Any> immutableSetOf(vararg elements: E): ImmutableSet<E> =
  ImmutableSet.copyOf(elements.asList())

inline fun <E : Any> immutableListBuilderOf(): ImmutableList.Builder<E> =
  ImmutableList.builder()

inline fun <E : Any> immutableListBuilderOf(expectedSize: Int): ImmutableList.Builder<E> =
  ImmutableList.builderWithExpectedSize(expectedSize)

inline operator fun <E : Any> ImmutableList.Builder<E>.plusAssign(element: E) {
  add(element)
}

inline fun <E : Any> Sequence<E>.toImmutableList(): ImmutableList<E> =
  ImmutableList.copyOf(asIterable())

fun <T, E : Any, B : ImmutableCollection.Builder<E>> Iterable<T>.mapTo(
  destination: B, transform: (T) -> E
): B {
  for (value in this) {
    destination.add(transform(value))
  }
  return destination
}

/**
 * Matches whether the contents of the iterables are equal. The
 * position within the iterables must also match.
 */
infix fun <E> Iterable<E>.contentEquals(that: Iterable<E>): Boolean {
  if (this is Collection<*> && that is Collection<*>)
    return contentEquals(that)
  return iterableContentEquals(that)
}

/**
 * Matches whether the contents of the iterables are equal. The
 * position within the iterables must also match.
 */
infix fun <E> Collection<E>.contentEquals(that: Collection<E>): Boolean {
  if (size != that.size)
    return false
  return iterableContentEquals(that)
}

private fun <E> Iterable<E>.iterableContentEquals(that: Iterable<E>): Boolean {
  val thatIt = that.iterator()
  for (thisValue in this) {
    if (!thatIt.hasNext() || thisValue != thatIt.next())
      return false
  }
  return true
}

/**
 * Matches whether the contents of the lists are equal.
 */
infix fun <E> List<E>.contentEquals(that: List<E>): Boolean {
  if (size != that.size)
    return false
  for (i in indices) {
    if (this[i] != that[i])
      return false
  }
  return true
}

/**
 * Gets hashcode for the contents of the [Sequence].
 */
fun <E> Sequence<E>.contentHashCode(): Int =
  iterator().contentHashCode()

/**
 * Gets hashcode for the contents of the [Iterable].
 */
fun <E> Iterable<E>.contentHashCode(): Int =
  iterator().contentHashCode()

/**
 * Gets hashcode for the contents of the [Iterator].
 */
private fun <E> Iterator<E>.contentHashCode(): Int {
  var result = 1
  for (element in this)
    result = 31 * result + (element?.hashCode() ?: 0)
  return result
}

/**
 * Constructs a new [EnumSet] for the given [type].
 */
inline fun <E : Enum<E>> enumSetOf(type: KClass<E>): EnumSet<E> =
  EnumSet.noneOf(type.java)

/**
 * Constructs a new [EnumSet] for the given type [E].
 */
inline fun <reified E : Enum<E>> enumSetOf(): EnumSet<E> =
  EnumSet.noneOf(E::class.java)

/**
 * Converts the [Iterable] into a [EnumSet].
 */
inline fun <reified E : Enum<E>> Iterable<E>.toEnumSet(): EnumSet<E> =
  toEnumSet(E::class)

/**
 * Converts the [Iterable] into a [EnumSet].
 */
fun <E : Enum<E>> Iterable<E>.toEnumSet(type: KClass<E>): EnumSet<E> =
  iterator().toEnumSet(type)

/**
 * Converts the [Sequence] into a [EnumSet].
 */
inline fun <reified E : Enum<E>> Sequence<E>.toEnumSet(): EnumSet<E> =
  toEnumSet(E::class)

/**
 * Converts the [Sequence] into a [EnumSet].
 */
fun <E : Enum<E>> Sequence<E>.toEnumSet(type: KClass<E>): EnumSet<E> =
  iterator().toEnumSet(type)

private fun <E : Enum<E>> Iterator<E>.toEnumSet(type: KClass<E>): EnumSet<E> {
  val set = EnumSet.noneOf(type.java)
  forEach(set::add)
  return set
}

inline fun <T> Sequence<T>.sortedWith(crossinline fn: (o1: T, o2: T) -> Int): Sequence<T> =
  sortedWith(Comparator { o1, o2 -> fn(o1, o2) })

inline fun <T> Iterable<T>.sortedWith(crossinline fn: (o1: T, o2: T) -> Int): List<T> =
  sortedWith(Comparator { o1, o2 -> fn(o1, o2) })

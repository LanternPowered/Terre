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
import java.util.*
import kotlin.reflect.KClass

inline fun <E : Any> Array<E>.toImmutableList(): ImmutableList<E> = ImmutableList.copyOf(this)

fun <E : Any> Iterable<E>.toImmutableList(): ImmutableList<E>
    = this as? ImmutableList<E> ?: ImmutableList.copyOf(this)

fun <E : Any> Iterable<E>.toImmutableCollection(): ImmutableCollection<E>
    = this as? ImmutableCollection<E> ?: ImmutableList.copyOf(this)

inline fun <E : Any> immutableListOf(): ImmutableList<E> = ImmutableList.of()

inline fun <E : Any> immutableListOf(element: E): ImmutableList<E> = ImmutableList.of(element)

inline fun <E : Any> immutableListOf(e1: E, e2: E): ImmutableList<E> = ImmutableList.of(e1, e2)

inline fun <E : Any> immutableListOf(e1: E, e2: E, e3: E): ImmutableList<E> = ImmutableList.of(e1, e2, e3)

inline fun <E : Any> immutableListOf(e1: E, e2: E, e3: E, e4: E): ImmutableList<E> = ImmutableList.of(e1, e2, e3, e4)

inline fun <E : Any> immutableListOf(vararg elements: E): ImmutableList<E> = ImmutableList.copyOf(elements.toList())

inline fun <E : Any> immutableListBuilderOf(): ImmutableList.Builder<E>
    = ImmutableList.builder()

inline fun <E : Any> immutableListBuilderOf(expectedSize: Int): ImmutableList.Builder<E>
    = ImmutableList.builderWithExpectedSize(expectedSize)

inline operator fun <E : Any> ImmutableList.Builder<E>.plusAssign(element: E) {
  add(element)
}

inline fun <E : Any> Sequence<E>.toImmutableList(): ImmutableList<E> = ImmutableList.copyOf(asIterable())

fun <T, E : Any, B : ImmutableCollection.Builder<E>> Iterable<T>.mapTo(destination: B, transform: (T) -> E): B {
  for (value in this) {
    destination.add(transform(value))
  }
  return destination
}

/**
 * Maps the objects of the iterable and collects them into a immutable list.
 */
fun <T, E : Any> Iterable<T>.mapToImmutableList(transform: (T) -> E): ImmutableList<E> {
  if (this is Collection) {
    if (this.size == 0) return ImmutableList.of()
    if (this.size == 1) {
      val element = if (this is List) {
        this[0]
      } else {
        iterator().next()
      }
      return ImmutableList.of(transform(element))
    }
  }
  val it = iterator()
  if (!it.hasNext()) return ImmutableList.of()
  val first = it.next()
  if (!it.hasNext()) return ImmutableList.of(transform(first))
  val builder = ImmutableList.builder<E>()
  builder.add(transform(first))
  it.forEachRemaining { builder.add(transform(it)) }
  return builder.build()
}

/**
 * Matches whether the contents of the iterables are equal. The
 * position within the iterables must also match.
 */
internal infix fun <E> Iterable<E>.contentEquals(that: Iterable<E>): Boolean {
  val thisList = this as? List<E> ?: this.toList()
  val thatList = that as? List<E> ?: that.toList()
  return thisList contentEquals thatList
}

/**
 * Matches whether the contents of the lists are equal.
 */
infix fun <E> List<E>.contentEquals(that: List<E>): Boolean {
  if (this.size != that.size) return false
  for (i in this.indices) {
    if (this[i] != that[i]) {
      return false
    }
  }
  return true
}

/**
 * Gets hashcode for the contents of the iterable.
 */
internal fun <E> Iterable<E>.contentHashCode(): Int {
  var result = 1
  for (element in this) {
    result = 31 * result + (element?.hashCode() ?: 0)
  }
  return result
}

/**
 * Constructs a new [EnumSet] for the given [type].
 */
inline fun <E : Enum<E>> enumSetOf(type: KClass<E>): EnumSet<E> = EnumSet.noneOf(type.java)

/**
 * Constructs a new [EnumSet] for the given type [E].
 */
inline fun <reified E : Enum<E>> enumSetOf(): EnumSet<E> = EnumSet.noneOf(E::class.java)

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

package org.lanternpowered.terre.impl.network

import kotlin.reflect.KClass

internal fun multistateProtocol(fn: MultistateProtocol.() -> Unit): MultistateProtocol {
  return MultistateProtocol().also(fn)
}

internal class MultistateProtocol : ProtocolBase() {

  private val array = Array(State.values().size) { Protocol() }

  operator fun get(state: State): Protocol = this.array[state.ordinal]

  override fun <P : Packet> bind(
      opcode: Int, type: KClass<P>, encoder: PacketEncoder<in P>) {
    this.array.forEach { it.bind(opcode, type, encoder) }
  }

  override fun <P : Packet> bind(
      opcode: Int, type: KClass<P>, encoder: PacketEncoder<in P>, direction: PacketDirection) {
    this.array.forEach { it.bind(opcode, type, encoder, direction) }
  }

  override fun <P : Packet> bind(
      opcode: Int, type: KClass<P>, decoder: PacketDecoder<out P>) {
    this.array.forEach { it.bind(opcode, type, decoder) }
  }

  override fun <P : Packet> bind(
      opcode: Int, type: KClass<P>, decoder: PacketDecoder<out P>, direction: PacketDirection) {
    this.array.forEach { it.bind(opcode, type, decoder, direction) }
  }

  fun init(fn: Protocol.() -> Unit) {
    get(State.ClientInit).also(fn)
  }

  fun play(fn: Protocol.() -> Unit) {
    get(State.Play).also(fn)
  }

  enum class State {
    ClientInit,
    Play,
  }
}

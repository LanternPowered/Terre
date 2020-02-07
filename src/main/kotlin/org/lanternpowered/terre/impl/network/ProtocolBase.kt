package org.lanternpowered.terre.impl.network

import kotlin.reflect.KClass

internal abstract class ProtocolBase(val version: Int) {

  inline fun <reified P : Packet> bind(
      opcode: Int, encoder: PacketEncoder<in P>, decoder: PacketDecoder<out P>) {
    bind(opcode, P::class, encoder)
    bind(opcode, P::class, decoder)
  }

  inline fun <reified P : Packet> bind(
      opcode: Int, encoder: PacketEncoder<in P>, decoder: PacketDecoder<out P>, direction: PacketDirection) {
    bind(opcode, P::class, encoder, direction)
    bind(opcode, P::class, decoder, direction)
  }

  inline fun <reified P : Packet> bind(
      opcode: Int, encoder: PacketEncoder<in P>) {
    bind(opcode, P::class, encoder)
  }

  inline fun <reified P : Packet> bind(
      opcode: Int, encoder: PacketEncoder<in P>, direction: PacketDirection) {
    bind(opcode, P::class, encoder, direction)
  }

  inline fun <reified P : Packet> bind(
      opcode: Int, decoder: PacketDecoder<out P>) {
    bind(opcode, P::class, decoder)
  }

  inline fun <reified P : Packet> bind(
      opcode: Int, decoder: PacketDecoder<out P>, direction: PacketDirection) {
    bind(opcode, P::class, decoder, direction)
  }

  abstract fun <P : Packet> bind(
      opcode: Int, type: KClass<P>, encoder: PacketEncoder<in P>)

  abstract fun <P : Packet> bind(
      opcode: Int, type: KClass<P>, encoder: PacketEncoder<in P>, direction: PacketDirection)

  abstract fun <P : Packet> bind(
      opcode: Int, type: KClass<P>, decoder: PacketDecoder<out P>)

  abstract fun <P : Packet> bind(
      opcode: Int, type: KClass<P>, decoder: PacketDecoder<out P>, direction: PacketDirection)

}

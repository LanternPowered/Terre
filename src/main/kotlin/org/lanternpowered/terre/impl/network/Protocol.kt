/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("UNCHECKED_CAST")

package org.lanternpowered.terre.impl.network

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.lanternpowered.terre.util.collection.immutableSetOf
import kotlin.reflect.KClass

/**
 * The base class for all packet protocol versions.
 */
internal abstract class Protocol(val version: Int) {

  companion object {

    private val bothDirectionsSet =
        immutableSetOf(PacketDirection.ServerToClient, PacketDirection.ClientToServer)
    private val toServerDirectionSet =
        immutableSetOf(PacketDirection.ClientToServer)
    private val toClientDirectionSet =
        immutableSetOf(PacketDirection.ServerToClient)

    fun directionSetOf(packetDirection: PacketDirection) =
        if (packetDirection == PacketDirection.ClientToServer) this.toServerDirectionSet else this.toClientDirectionSet
  }

  private val decodersByOpcode
      = Array<Int2ObjectMap<PacketDecoderRegistration<*>>>(bothDirectionsSet.size) { Int2ObjectOpenHashMap() }
  private val encodersByPacketType
      = Array<MutableMap<Class<*>, PacketEncoderRegistration<*>>>(bothDirectionsSet.size) { mutableMapOf() }

  fun getDecoder(packetDirection: PacketDirection, opcode: Int): PacketDecoderRegistration<*>?
      = this.decodersByOpcode[packetDirection.ordinal][opcode]

  fun <T : Packet> getEncoder(packetDirection: PacketDirection, packetType: Class<T>): PacketEncoderRegistration<T>?
      = this.encodersByPacketType[packetDirection.ordinal][packetType] as (PacketEncoderRegistration<T>?)

  protected inline fun <reified P : Packet> bind(
      opcode: Int, encoder: PacketEncoder<in P>, decoder: PacketDecoder<out P>) {
    bind(opcode, P::class, encoder)
    bind(opcode, P::class, decoder)
  }

  protected inline fun <reified P : Packet> bind(
      opcode: Int, encoder: PacketEncoder<in P>, decoder: PacketDecoder<out P>, direction: PacketDirection) {
    bind(opcode, P::class, encoder, direction)
    bind(opcode, P::class, decoder, direction)
  }

  protected inline fun <reified P : Packet> bind(
      opcode: Int, encoder: PacketEncoder<in P>) {
    bind(opcode, P::class, encoder)
  }

  protected inline fun <reified P : Packet> bind(
      opcode: Int, encoder: PacketEncoder<in P>, direction: PacketDirection) {
    bind(opcode, P::class, encoder, direction)
  }

  protected inline fun <reified P : Packet> bind(
      opcode: Int, decoder: PacketDecoder<out P>) {
    bind(opcode, P::class, decoder)
  }

  protected inline fun <reified P : Packet> bind(
      opcode: Int, decoder: PacketDecoder<out P>, direction: PacketDirection) {
    bind(opcode, P::class, decoder, direction)
  }

  protected fun <P : Packet> bind(
      opcode: Int, type: KClass<P>, encoder: PacketEncoder<in P>) {
    bind0(opcode, type, encoder)
  }

  protected fun <P : Packet> bind(
      opcode: Int, type: KClass<P>, encoder: PacketEncoder<in P>, direction: PacketDirection) {
    bind0(opcode, type, encoder, direction)
  }

  private fun <P : Packet> bind0(
      opcode: Int, type: KClass<P>, encoder: PacketEncoder<in P>, direction: PacketDirection? = null) {
    val directionsSet = if (direction != null) directionSetOf(direction) else bothDirectionsSet
    var registration = PacketEncoderRegistrationImpl(type.java, opcode, encoder, directionsSet)
    if (direction != null) {
      this.encodersByPacketType[direction.ordinal][type.java] = registration
    } else {
      for (map in this.encodersByPacketType) {
        map[type.java] = registration
      }
    }
    if (type.isSealed) {
      for (subclass in type.sealedSubclasses) {
        registration = PacketEncoderRegistrationImpl(
            type.java, opcode, encoder, directionsSet)
        if (direction != null) {
          val map = this.encodersByPacketType[direction.ordinal]
          if (!map.containsKey(subclass.java)) {
            map[subclass.java] = registration
          }
        } else {
          for (map in this.encodersByPacketType) {
            if (!map.containsKey(subclass.java)) {
              map[subclass.java] = registration
            }
          }
        }
      }
    }
  }

  protected fun <P : Packet> bind(
      opcode: Int, type: KClass<P>, decoder: PacketDecoder<out P>) {
    bind0(opcode, type, decoder, null)
  }

  protected fun <P : Packet> bind(
      opcode: Int, type: KClass<P>, decoder: PacketDecoder<out P>, direction: PacketDirection) {
    bind0(opcode, type, decoder, direction)
  }

  private fun <P : Packet> bind0(
      opcode: Int, type: KClass<P>, decoder: PacketDecoder<out P>, direction: PacketDirection?) {
    val directionsSet = if (direction != null) directionSetOf(direction) else bothDirectionsSet
    val registration = PacketDecoderRegistrationImpl(type.java, opcode, decoder, directionsSet)
    if (direction != null) {
      this.decodersByOpcode[direction.ordinal][opcode] = registration
    } else {
      for (map in this.decodersByOpcode) {
        map[opcode] = registration
      }
    }
  }
}

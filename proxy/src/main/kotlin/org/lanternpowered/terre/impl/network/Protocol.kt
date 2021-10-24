/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
@file:Suppress("UNCHECKED_CAST", "FunctionName")

package org.lanternpowered.terre.impl.network

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import kotlin.reflect.KClass

internal fun protocol(name: String, builder: Protocol.() -> Unit): Protocol {
  return Protocol(name).also(builder)
}

/**
 * The base class for all packet protocol versions.
 */
internal class Protocol(val name: String) : ProtocolBase() {

  companion object {

    private val bothDirectionsSet =
      setOf(PacketDirection.ServerToClient, PacketDirection.ClientToServer)
    private val toServerDirectionSet =
      setOf(PacketDirection.ClientToServer)
    private val toClientDirectionSet =
      setOf(PacketDirection.ServerToClient)

    fun directionSetOf(packetDirection: PacketDirection) =
      when (packetDirection) {
        PacketDirection.ClientToServer -> toServerDirectionSet
        PacketDirection.ServerToClient -> toClientDirectionSet
      }
  }

  private val decodersByOpcode =
    Array<Int2ObjectMap<PacketDecoderRegistration<*>>>(bothDirectionsSet.size) { Int2ObjectOpenHashMap() }
  private val encodersByPacketType =
    Array<MutableMap<Class<*>, PacketEncoderRegistration<*>>>(bothDirectionsSet.size) { HashMap() }

  fun getDecoder(packetDirection: PacketDirection, opcode: Int): PacketDecoderRegistration<*>? =
    decodersByOpcode[packetDirection.ordinal][opcode]

  fun <T : Packet> getEncoder(
    packetDirection: PacketDirection, packetType: Class<T>
  ): PacketEncoderRegistration<T>? =
    encodersByPacketType[packetDirection.ordinal][packetType] as (PacketEncoderRegistration<T>?)

  override fun <P : Packet> bind(
    opcode: Int, type: KClass<P>, encoder: PacketEncoder<in P>
  ) {
    bind0(opcode, type, encoder)
  }

  override fun <P : Packet> bind(
    opcode: Int, type: KClass<P>, encoder: PacketEncoder<in P>, direction: PacketDirection
  ) {
    bind0(opcode, type, encoder, direction)
  }

  override fun <P : Packet> bind(
    opcode: Int, type: KClass<P>, decoder: PacketDecoder<out P>
  ) {
    bind0(opcode, type, decoder, null)
  }

  override fun <P : Packet> bind(
    opcode: Int, type: KClass<P>, decoder: PacketDecoder<out P>, direction: PacketDirection
  ) {
    bind0(opcode, type, decoder, direction)
  }

  private fun <P : Packet> bind0(
    opcode: Int, type: KClass<P>, encoder: PacketEncoder<in P>, direction: PacketDirection? = null
  ) {
    val directionsSet = if (direction != null) directionSetOf(direction) else bothDirectionsSet
    var registration = PacketEncoderRegistrationImpl(type.java, opcode, encoder, directionsSet)
    if (direction != null) {
      encodersByPacketType[direction.ordinal][type.java] = registration
    } else {
      for (map in encodersByPacketType) {
        map[type.java] = registration
      }
    }
    if (type.isSealed) {
      for (subclass in type.sealedSubclasses) {
        registration = PacketEncoderRegistrationImpl(
          type.java, opcode, encoder, directionsSet)
        if (direction != null) {
          val map = encodersByPacketType[direction.ordinal]
          if (!map.containsKey(subclass.java))
            map[subclass.java] = registration
        } else {
          for (map in encodersByPacketType) {
            if (!map.containsKey(subclass.java))
              map[subclass.java] = registration
          }
        }
      }
    }
  }

  private fun <P : Packet> bind0(
    opcode: Int, type: KClass<P>, decoder: PacketDecoder<out P>, direction: PacketDirection?
  ) {
    val directionsSet = if (direction != null) directionSetOf(direction) else bothDirectionsSet
    val registration = PacketDecoderRegistrationImpl(type.java, opcode, decoder, directionsSet)
    if (direction != null) {
      decodersByOpcode[direction.ordinal][opcode] = registration
    } else {
      for (map in decodersByOpcode)
        map[opcode] = registration
    }
  }
}

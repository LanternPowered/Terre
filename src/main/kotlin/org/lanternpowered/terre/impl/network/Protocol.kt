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

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import kotlin.reflect.KClass

/**
 * The base class for all packet protocol versions.
 */
internal abstract class Protocol(val version: Int) {

  private val decodersByOpcode = Int2ObjectOpenHashMap<PacketDecoderRegistration<*>>()
  private val encodersByPacketType = mutableMapOf<Class<*>, PacketEncoderRegistration<*>>()

  fun getDecoder(opcode: Int): PacketDecoderRegistration<*>? = this.decodersByOpcode[opcode]

  fun <T : Packet> getEncoder(packetType: Class<T>): PacketEncoderRegistration<T>?
      = this.encodersByPacketType[packetType] as (PacketEncoderRegistration<T>?)

  protected inline fun <reified P : Packet> bind(
      opcode: Int, encoder: PacketEncoder<in P>, decoder: PacketDecoder<out P>) {
    bind(opcode, P::class, encoder)
    bind(opcode, P::class, decoder)
  }

  protected inline fun <reified P : Packet> bind(opcode: Int, encoder: PacketEncoder<in P>) {
    bind(opcode, P::class, encoder)
  }

  protected inline fun <reified P : Packet> bind(opcode: Int, decoder: PacketDecoder<out P>) {
    bind(opcode, P::class, decoder)
  }

  protected fun <P : Packet> bind(opcode: Int, type: KClass<P>, encoder: PacketEncoder<in P>) {
    this.encodersByPacketType[type.java] = PacketEncoderRegistrationImpl(type.java, opcode, encoder)
    if (type.isSealed) {
      for (subclass in type.sealedSubclasses) {
        if (!this.encodersByPacketType.containsKey(subclass.java)) {
          this.encodersByPacketType[subclass.java] = PacketEncoderRegistrationImpl(type.java, opcode, encoder)
        }
      }
    }
  }

  protected fun <P : Packet> bind(opcode: Int, type: KClass<P>, decoder: PacketDecoder<out P>) {
    this.decodersByOpcode[opcode] = PacketDecoderRegistrationImpl(type.java, opcode, decoder)
  }
}

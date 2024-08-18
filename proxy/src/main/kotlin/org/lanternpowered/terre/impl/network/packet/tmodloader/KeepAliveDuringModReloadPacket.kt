package org.lanternpowered.terre.impl.network.packet.tmodloader

import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.PacketDecoder
import org.lanternpowered.terre.impl.network.PacketEncoder

internal object KeepAliveDuringModReloadPacket : Packet

internal val KeepAliveDuringModReloadEncoder = PacketEncoder<KeepAliveDuringModReloadPacket> { _, _ -> }

internal val KeepAliveDuringModReloadDecoder = PacketDecoder { KeepAliveDuringModReloadPacket }

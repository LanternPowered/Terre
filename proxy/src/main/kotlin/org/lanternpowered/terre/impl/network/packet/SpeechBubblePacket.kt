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

package org.lanternpowered.terre.impl.network.packet

import org.lanternpowered.terre.impl.network.Packet
import org.lanternpowered.terre.impl.network.PacketEncoder
import org.lanternpowered.terre.impl.network.buffer.NpcId
import org.lanternpowered.terre.impl.network.buffer.PlayerId
import org.lanternpowered.terre.impl.network.buffer.ProjectileId

internal sealed class SpeechBubblePacket : Packet {

  abstract val id: Int

  /**
   * Updates or creates an emote bubble.
   */
  data class Update(
    override val id: Int,
    val emote: Int,
    val emoteMetadata: Int,
    val lifetime: Int,
    val anchor: Anchor
  ) : SpeechBubblePacket()

  /**
   * Removes an emote bubble.
   */
  data class Remove(override val id: Int) : SpeechBubblePacket()

  sealed class Anchor {

    data class Player(val id: PlayerId) : Anchor()

    data class Npc(val id: NpcId) : Anchor()

    data class Projectile(val id: ProjectileId) : Anchor()
  }
}

internal val SpeechBubbleEncoder = PacketEncoder<SpeechBubblePacket> { buf, packet ->
  buf.writeIntLE(packet.id)
  if (packet is SpeechBubblePacket.Remove) {
    buf.writeByte(255)
  } else {
    packet as SpeechBubblePacket.Update
    when (val anchor = packet.anchor) {
      is SpeechBubblePacket.Anchor.Npc -> {
        buf.writeByte(0)
        buf.writeShortLE(anchor.id.value)
      }
      is SpeechBubblePacket.Anchor.Player -> {
        buf.writeByte(1)
        buf.writeShortLE(anchor.id.value)
      }
      is SpeechBubblePacket.Anchor.Projectile -> {
        buf.writeByte(2)
        buf.writeShortLE(anchor.id.value)
      }
    }
    buf.writeShortLE(packet.lifetime)
    buf.writeByte(packet.emote)
    if (packet.emote < 0)
      buf.writeShortLE(packet.emote)
  }
}

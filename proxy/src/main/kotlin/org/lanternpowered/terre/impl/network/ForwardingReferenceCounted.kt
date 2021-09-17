/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.network

import io.netty.util.ReferenceCounted

internal abstract class ForwardingReferenceCounted(
  private val referenceCounted: ReferenceCounted
) : ReferenceCounted {

  override fun refCnt() = referenceCounted.refCnt()
  override fun release() = referenceCounted.release()
  override fun release(decrement: Int) = referenceCounted.release(decrement)
  override fun retain() = apply { referenceCounted.retain() }
  override fun retain(increment: Int) = apply { referenceCounted.retain(increment) }
  override fun touch() = apply { referenceCounted.touch() }
  override fun touch(hint: Any?) = apply { referenceCounted.touch(hint) }
}

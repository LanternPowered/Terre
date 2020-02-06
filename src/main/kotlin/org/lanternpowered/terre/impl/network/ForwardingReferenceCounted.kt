package org.lanternpowered.terre.impl.network

import io.netty.util.ReferenceCounted

abstract class ForwardingReferenceCounted(
    private val referenceCounted: ReferenceCounted
) : ReferenceCounted {

  override fun refCnt() = this.referenceCounted.refCnt()
  override fun release(): Boolean {
    //Throwable().printStackTrace()
    return this.referenceCounted.release()
  }
  override fun release(decrement: Int): Boolean {
    //Throwable().printStackTrace()
    return this.referenceCounted.release(decrement)
  }
  override fun retain() = apply { this.referenceCounted.retain() }
  override fun retain(increment: Int) = apply { this.referenceCounted.retain(increment) }
  override fun touch() = apply { this.referenceCounted.touch() }
  override fun touch(hint: Any?) = apply { this.referenceCounted.touch(hint) }
}

/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.plugin.asm

import org.lanternpowered.terre.impl.Terre
import org.objectweb.asm.AnnotationVisitor

internal abstract class WarningAnnotationVisitor(api: Int, private val className: String) : AnnotationVisitor(api) {

  internal abstract val annotation: String

  override fun visit(name: String?, value: Any) {
    Terre.logger.warn("Found unknown $annotation annotation element in $className: $name = $value")
  }

  override fun visitEnum(name: String?, desc: String, value: String) {
    Terre.logger.warn("Found unknown $annotation annotation element in $className: $name ($desc) = $value")
  }

  override fun visitAnnotation(name: String?, desc: String): AnnotationVisitor? {
    Terre.logger.warn("Found unknown $annotation annotation element in $className: $name ($desc)")
    return null
  }

  override fun visitArray(name: String?): AnnotationVisitor? {
    Terre.logger.warn("Found unknown $annotation annotation element in $className: $name")
    return null
  }
}

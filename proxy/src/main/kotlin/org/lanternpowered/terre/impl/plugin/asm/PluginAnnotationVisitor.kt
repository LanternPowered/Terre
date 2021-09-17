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

import org.lanternpowered.terre.plugin.InvalidPluginException
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes.ASM9

internal class PluginAnnotationVisitor(
  private val classVisitor: PluginClassVisitor, className: String
) : WarningAnnotationVisitor(ASM9, className) {

  override val annotation: String get() = "@Plugin"

  override fun visit(name: String?, value: Any) {
    if (name == null)
      throw InvalidPluginException("Plugin annotation attribute name is null")
    if (name == "id") {
      if (value !is String)
        throw InvalidPluginException("Plugin annotation has invalid element 'id'")
      classVisitor.pluginId = value
    } else {
      super.visit(name, value)
    }
  }

  override fun visitArray(name: String?): AnnotationVisitor? {
    if (name == null)
      throw InvalidPluginException("Plugin annotation attribute name is null")
    if (name == "use named arguments")
      return null
    return super.visitArray(name)
  }

  override fun visitEnd() {
    if (classVisitor.pluginId == null)
      throw InvalidPluginException("Plugin annotation is missing required element 'id'")
  }
}

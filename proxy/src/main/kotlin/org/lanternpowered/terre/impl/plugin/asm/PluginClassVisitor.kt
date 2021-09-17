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

import org.lanternpowered.terre.plugin.Plugin
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes.ASM9
import org.objectweb.asm.Type

internal class PluginClassVisitor : ClassVisitor(ASM9) {

  lateinit var className: String
    private set

  var pluginId: String? = null

  override fun visit(
    version: Int,
    access: Int,
    name: String,
    signature: String?,
    superName: String?,
    interfaces: Array<String>?
  ) {
    className = name
  }

  override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? =
    if (visible && desc == pluginDescriptor) {
      PluginAnnotationVisitor(this, className)
    } else null

  companion object {

    private val pluginDescriptor = Type.getDescriptor(Plugin::class.java)
  }
}

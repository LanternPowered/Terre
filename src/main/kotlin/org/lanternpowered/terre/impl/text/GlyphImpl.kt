/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.text

import org.lanternpowered.terre.catalog.numericCatalogTypeRegistry
import org.lanternpowered.terre.impl.InternalCatalogType
import org.lanternpowered.terre.text.Glyph
import org.lanternpowered.terre.util.Namespace
import org.lanternpowered.terre.util.NamespacedId

internal data class GlyphImpl(
    override val id: NamespacedId,
    override val name: String,
    override val internalId: Int
) : Glyph, InternalCatalogType

internal val GlyphRegistryImpl = numericCatalogTypeRegistry<Glyph> {
  fun register(id: String, name: String, internalId: Int) {
    register(GlyphImpl(Namespace.Terre.id(id), name, internalId))
  }
  register("button_a", "A Button", 0)
  register("button_b", "B Button", 1)
  register("button_x", "X Button", 2)
  register("button_y", "Y Button", 3)
  register("button_back", "Back Button", 4)
  register("button_start", "Start Button", 5)
  register("button_left_shoulder", "Left Shoulder Button", 6)
  register("button_right_shoulder", "Right Shoulder Button", 7)
  register("button_left_trigger", "Left Trigger Button", 8)
  register("button_right_trigger", "Right Trigger Button", 9)
  register("joystick_left", "Left Joystick", 10)
  register("joystick_right", "Right Joystick", 11)
  register("joystick_undefined", "Undefined Joystick", 12)
  register("dpad_right", "D-pad Right", 13)
  register("dpad_left", "D-pad Left", 14)
  register("dpad_down", "D-pad Down", 15)
  register("dpad_up", "D-pad Up", 16)
  register("joystick_left_left", "Left Joystick Left", 17)
  register("joystick_left_right", "Left Joystick Right", 18)
  register("joystick_left_up", "Left Joystick Up", 19)
  register("joystick_left_down", "Left Joystick Down", 20)
  register("joystick_right_left", "Right Joystick Left", 21)
  register("joystick_right_right", "Right Joystick Right", 22)
  register("joystick_right_up", "Right Joystick Up", 23)
  register("joystick_right_down", "Right Joystick Down", 24)
  register("joystick_left_wiggle_left_right", "Wiggle Left Joystick Left and Right", 25)
}

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

import org.junit.jupiter.api.Test
import org.lanternpowered.terre.text.GlyphRegistry
import org.lanternpowered.terre.text.formattedTextOf
import org.lanternpowered.terre.text.localizedTextOf
import org.lanternpowered.terre.text.text
import org.lanternpowered.terre.util.Colors
import kotlin.test.assertEquals

class VanillaTextSerializerTest {

  @Test fun `test grouped text`() {
    val text = ("Part 1".text() + " and Part 2".text()) as TextImpl
    val expected = formattedTextOf("Part 1 and Part 2")
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `test colored text`() {
    val text = "Test".text().color(Colors.Red) as TextImpl
    val expected = formattedTextOf("[c/ff0000:Test]")
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `test plain colored text`() {
    val text = "Test".text().color(Colors.Red) as TextImpl
    val expected = formattedTextOf("Test")
    assertEquals(expected, text.toPlainVanillaText())
  }

  @Test fun `test colored and grouped text`() {
    val text = ("Part 1".text().color(Colors.Red) + " and Part 2".text()) as TextImpl
    val expected = formattedTextOf("[c/ff0000:Part 1] and Part 2")
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `test plain colored and grouped text`() {
    val text = ("Part 1".text().color(Colors.Red) + " and Part 2".text()) as TextImpl
    val expected = formattedTextOf("Part 1 and Part 2")
    assertEquals(expected, text.toPlainVanillaText())
  }

  @Test fun `test glyph text`() {
    val text = GlyphRegistry.require(0).text() as TextImpl
    val expected = formattedTextOf("[g:0]")
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `test formatted text`() {
    val a = "A".text()
    val b = "B".text()
    val text = formattedTextOf("{0} and {1}", a, b) as TextImpl
    val expected = formattedTextOf("A and B")
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `test translatable text`() {
    val localized = localizedTextOf("path.to.key") as TextImpl
    val expected = formattedTextOf("{0}", localized)
    assertEquals(expected, localized.toTaggedVanillaText())
  }

  @Test fun `test translatable and grouped text`() {
    val localized = localizedTextOf("path.to.key")
    val text = (localized + " and Part 2".text()) as TextImpl
    val expected = formattedTextOf("{0} and Part 2", localized)
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `test translatable, grouped and colored text`() {
    val localized = localizedTextOf("path.to.key").color(Colors.Red)
    val text = (localized + ", Part 1 ".text().color(Colors.Lime) + " and Part 2".text()) as TextImpl
    val expected = formattedTextOf("[c/ff0000:{0}][c/ff00:, Part 1 ] and Part 2", localized)
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `test literal with square brackets text`() {
    val text = "[Test]".text().color(Colors.Red) as TextImpl
    val expected = formattedTextOf("[c/ff0000:[Test][c/ff0000:]]")
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `test literal with square brackets text 2`() {
    val text = "[Test] and another ] after".text().color(Colors.Red) as TextImpl
    val expected = formattedTextOf("[c/ff0000:[Test][c/ff0000:] and another ][c/ff0000:] after]")
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `test plain literal with square brackets text`() {
    val text = "[Test]".text().color(Colors.Red) as TextImpl
    val expected = formattedTextOf("[Test]")
    assertEquals(expected, text.toPlainVanillaText())
  }

  @Test fun `test colored literal with square brackets text 3`() {
    val text = "[c:Test]".text().color(Colors.Red) as TextImpl
    val expected = formattedTextOf("[c/ff0000:[c:Test][c/ff0000:]]")
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `test literal with square brackets text 3`() {
    val text = "[c:Test]".text() as TextImpl
    val expected = formattedTextOf("[l:[]c:Test[l:]]")
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `test plain literal with square brackets text 3`() {
    val text = "[c:Test]".text() as TextImpl
    val expected = formattedTextOf("[c:Test]")
    assertEquals(expected, text.toPlainVanillaText())
  }
}

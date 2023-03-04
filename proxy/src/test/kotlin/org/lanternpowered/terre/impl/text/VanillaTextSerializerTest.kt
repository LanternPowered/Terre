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

  @Test fun `grouped text`() {
    val text = ("Part 1".text() + " and Part 2".text()) as TextImpl
    val expected = formattedTextOf("Part 1 and Part 2")
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `colored text`() {
    val text = "Test".text().color(Colors.Red) as TextImpl
    val expected = formattedTextOf("[c/ff0000:Test]")
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `multiline text - colored`() {
    val text = "Test\nmultiline\ntext".text().color(Colors.Red) as TextImpl
    val expected = formattedTextOf("[c/ff0000:Test]\n[c/ff0000:multiline]\n[c/ff0000:text]")
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `multiline text - different colors`() {
    val text = ("Test\n".text().color(Colors.Red) + "multiline\ntext".text().color(Colors.Green)) as TextImpl
    val expected = formattedTextOf("[c/ff0000:Test]\n[c/8000:multiline]\n[c/8000:text]")
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `multiline text - colored empty lines`() {
    val text = "Test\nmultiline\n\ntext".text().color(Colors.Red) as TextImpl
    val expected = formattedTextOf("[c/ff0000:Test]\n[c/ff0000:multiline]\n\n[c/ff0000:text]")
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `plain colored text`() {
    val text = "Test".text().color(Colors.Red) as TextImpl
    val expected = formattedTextOf("Test")
    assertEquals(expected, text.toPlainVanillaText())
  }

  @Test fun `colored and grouped text`() {
    val text = ("Part 1".text().color(Colors.Red) + " and Part 2".text()) as TextImpl
    val expected = formattedTextOf("[c/ff0000:Part 1] and Part 2")
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `plain colored and grouped text`() {
    val text = ("Part 1".text().color(Colors.Red) + " and Part 2".text()) as TextImpl
    val expected = formattedTextOf("Part 1 and Part 2")
    assertEquals(expected, text.toPlainVanillaText())
  }

  @Test fun `glyph text`() {
    val text = GlyphRegistry.require(0).text() as TextImpl
    val expected = formattedTextOf("[g:0]")
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `formatted text`() {
    val a = "A".text()
    val b = "B".text()
    val text = formattedTextOf("{0} and {1}", a, b) as TextImpl
    val expected = formattedTextOf("A and B")
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `translatable text`() {
    val localized = localizedTextOf("path.to.key") as TextImpl
    val expected = formattedTextOf("{0}", localized)
    assertEquals(expected, localized.toTaggedVanillaText())
  }

  @Test fun `translatable and grouped text`() {
    val localized = localizedTextOf("path.to.key")
    val text = (localized + " and Part 2".text()) as TextImpl
    val expected = formattedTextOf("{0} and Part 2", localized)
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `translatable, grouped and colored text`() {
    val localized = localizedTextOf("path.to.key").color(Colors.Red)
    val text = (localized + ", Part 1 ".text().color(Colors.Lime) + " and Part 2".text()) as TextImpl
    val expected = formattedTextOf("[c/ff0000:{0}][c/ff00:, Part 1 ] and Part 2", localized)
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `literal with square brackets text`() {
    val text = "[Test]".text().color(Colors.Red) as TextImpl
    val expected = formattedTextOf("[c/ff0000:[Test][c/ff0000:]]")
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `literal with square brackets text 2`() {
    val text = "[Test] and another ] after".text().color(Colors.Red) as TextImpl
    val expected = formattedTextOf("[c/ff0000:[Test][c/ff0000:] and another ][c/ff0000:] after]")
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `plain literal with square brackets text`() {
    val text = "[Test]".text().color(Colors.Red) as TextImpl
    val expected = formattedTextOf("[Test]")
    assertEquals(expected, text.toPlainVanillaText())
  }

  @Test fun `colored literal with square brackets text 3`() {
    val text = "[c:Test]".text().color(Colors.Red) as TextImpl
    val expected = formattedTextOf("[c/ff0000:[c:Test][c/ff0000:]]")
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `literal with square brackets text 3`() {
    val text = "[c:Test]".text() as TextImpl
    val expected = formattedTextOf("[l:[]c:Test[l:]]")
    assertEquals(expected, text.toTaggedVanillaText())
  }

  @Test fun `plain literal with square brackets text 3`() {
    val text = "[c:Test]".text() as TextImpl
    val expected = formattedTextOf("[c:Test]")
    assertEquals(expected, text.toPlainVanillaText())
  }
}

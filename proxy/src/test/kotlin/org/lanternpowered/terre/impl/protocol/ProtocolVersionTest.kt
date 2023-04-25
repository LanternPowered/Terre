/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.protocol

import org.lanternpowered.terre.ProtocolVersion
import org.lanternpowered.terre.impl.network.ProtocolVersions
import org.lanternpowered.terre.util.Version
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProtocolVersionTest {

  @Test fun tModLoader() {
    val versionString = "tModLoader v2022.9.47.30!2022.9.47"
    val version = ProtocolVersions.parse(versionString)
    assertNotNull(version)
    assertTrue(version is ProtocolVersion.TModLoader)
    assertEquals(Version(2022, 9, 47, 30), version.version)
    assertNull(version.branch)
    assertNull(version.purpose)
    assertEquals(versionString, ProtocolVersions.toString(version))
  }

  @Test fun `tModLoader branch`() {
    val versionString = "tModLoader v2022.9.47.30 1.4!2022.9.47"
    val version = ProtocolVersions.parse(versionString)
    assertNotNull(version)
    assertTrue(version is ProtocolVersion.TModLoader)
    assertEquals(Version(2022, 9, 47, 30), version.version)
    assertEquals("1.4", version.branch)
    assertNull(version.purpose)
    assertEquals(versionString, ProtocolVersions.toString(version))
  }

  @Test fun `tModLoader dev branch`() {
    val versionString = "tModLoader v2022.9.47.30 1.4 Dev!2022.9.47"
    val version = ProtocolVersions.parse(versionString)
    assertNotNull(version)
    assertTrue(version is ProtocolVersion.TModLoader)
    assertEquals(Version(2022, 9, 47, 30), version.version)
    assertEquals("1.4", version.branch)
    assertEquals("Dev", version.purpose)
    assertEquals(versionString, ProtocolVersions.toString(version))
  }
}

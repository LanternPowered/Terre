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

import org.lanternpowered.terre.ProtocolVersion
import org.lanternpowered.terre.util.Version

internal object ProtocolVersions {

  /**
   * All the known protocol version numbers paired to their version name.
   */
  private val knownVanillaVersions = ProtocolVersion.Vanilla.Companion::class
    .members
    .asSequence()
    .filter { member -> member.returnType.classifier == ProtocolVersion.Vanilla::class }
    .map { member -> member.call(ProtocolVersion.Vanilla.Companion) as ProtocolVersion.Vanilla }
    .associateBy { it.protocol }

  private const val vanillaVersionPrefix = "Terraria"
  private val vanillaVersionRegex = "$vanillaVersionPrefix([0-9]+)".toRegex()
  private const val tModLoaderPrefix = "tModLoader"
  private val tModLoaderVersionRegex =
    "$tModLoaderPrefix v([0-9.]*)(?: ([^\\s!]+))?(?: ([^\\s!]+))?(?:!.+)?".toRegex()

  /**
   * Gets the vanilla [ProtocolVersion] for the given protocol version number.
   */
  operator fun get(protocol: Int) = knownVanillaVersions[protocol]

  /**
   * Gets the vanilla [ProtocolVersion] for the given protocol version number.
   */
  operator fun get(version: String): ProtocolVersion.Vanilla? {
    val v = Version(version)
    return knownVanillaVersions.asSequence().firstOrNull { v == it.value.version }?.value
  }

  fun parse(version: String): ProtocolVersion? {
    var result = vanillaVersionRegex.matchEntire(version)
    if (result != null) {
      val protocol = result.groupValues[1].toInt()
      return knownVanillaVersions[protocol] ?: ProtocolVersion.Vanilla(Version(0), protocol)
    }
    result = tModLoaderVersionRegex.matchEntire(version)
    if (result != null) {
      val versionPart = Version(result.groupValues[1])
      val branch = result.groups[2]?.value
      val buildType = result.groups[3]?.value
      return ProtocolVersion.TModLoader(versionPart, branch, buildType)
    }
    return null
  }

  fun toString(version: ProtocolVersion): String {
    return when (version) {
      is ProtocolVersion.Vanilla -> "$vanillaVersionPrefix${version.protocol}"
      is ProtocolVersion.TModLoader -> {
        var versionString = "$tModLoaderPrefix v${version.version}"
        if (version.branch != null)
          versionString += " ${version.branch}"
        if (version.purpose != null)
          versionString += " ${version.purpose}"
        var protocolVersion = version.version
        val protocolVersionValues = protocolVersion.values
        if (protocolVersionValues.size > 3) {
          protocolVersion = Version(protocolVersionValues.copyOf(3))
        }
        "$versionString!$protocolVersion"
      }
    }
  }
}

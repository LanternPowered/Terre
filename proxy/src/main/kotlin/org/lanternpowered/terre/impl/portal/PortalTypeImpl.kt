/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.portal

import org.lanternpowered.terre.catalog.NamedCatalogType
import org.lanternpowered.terre.impl.ProjectileType
import org.lanternpowered.terre.impl.ProjectileTypes
import org.lanternpowered.terre.impl.buildNamedCatalogTypeRegistryOf
import org.lanternpowered.terre.math.Vec2f
import org.lanternpowered.terre.portal.PortalType

/**
 * @property fallback In case the portal type isn't supported by the latest version
 */
internal data class PortalTypeImpl(
  override val name: String,
  val projectileType: ProjectileType?,
  val size: Vec2f,
  val lifetime: Int,
  val fallback: PortalType? = null
) : NamedCatalogType, PortalType {

  companion object {
    val Lunar = PortalTypeImpl("lunar", ProjectileTypes.LunarPortal, Vec2f(32f, 32f), 7200)
    val Nebula = PortalTypeImpl("nebula", ProjectileTypes.NebulaArcanum, Vec2f(176f, 176f), 3600)
    val Magnetosphere = PortalTypeImpl("magnetosphere", ProjectileTypes.MagnetSphereBall, Vec2f(38f, 38f), 660)
    val Electrosphere = PortalTypeImpl("electrosphere", ProjectileTypes.ElectroSphereMissile, Vec2f(80f, 80f), 3600)
    val Fireball = PortalTypeImpl("fireball", ProjectileTypes.Fireball, Vec2f(40f, 40f), 3600)
    val Shadowball = PortalTypeImpl("shadowball", ProjectileTypes.ShadowFireball, Vec2f(40f, 40f), 3600)
    val Void = PortalTypeImpl("void", ProjectileTypes.VoidBag, Vec2f(30f, 70f), 3600, Shadowball) // TODO: Check these
    val Invisible = PortalTypeImpl("invisible", null, Vec2f(30f, 70f), -1)
  }
}

internal val PortalTypeRegistryImpl = buildNamedCatalogTypeRegistryOf<PortalType>({ null }) {
  register(PortalTypeImpl.Lunar)
  register(PortalTypeImpl.Nebula)
  register(PortalTypeImpl.Magnetosphere)
  register(PortalTypeImpl.Electrosphere)
  register(PortalTypeImpl.Fireball)
  register(PortalTypeImpl.Shadowball)
  register(PortalTypeImpl.Void)
}

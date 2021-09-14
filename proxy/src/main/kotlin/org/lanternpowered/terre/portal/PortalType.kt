/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.portal

import org.lanternpowered.terre.catalog.NamedCatalogType
import org.lanternpowered.terre.catalog.NamedCatalogTypeRegistry
import org.lanternpowered.terre.impl.portal.PortalTypeImpl
import org.lanternpowered.terre.impl.portal.PortalTypeRegistryImpl

/**
 * Represents a type of portal.
 */
interface PortalType : NamedCatalogType

/**
 * All the supported portal types.
 */
object PortalTypes {
  val Lunar: PortalType = PortalTypeImpl.Lunar
  val Nebula: PortalType = PortalTypeImpl.Nebula
  val Magnetosphere: PortalType = PortalTypeImpl.Magnetosphere
  val Electrosphere: PortalType = PortalTypeImpl.Electrosphere
  val Fireball: PortalType = PortalTypeImpl.Fireball
  val Shadowball: PortalType = PortalTypeImpl.Shadowball
  val Void: PortalType = PortalTypeImpl.Void
  val Invisible: PortalType = PortalTypeImpl.Invisible
}

object PortalTypeRegistry : NamedCatalogTypeRegistry<PortalType> by PortalTypeRegistryImpl

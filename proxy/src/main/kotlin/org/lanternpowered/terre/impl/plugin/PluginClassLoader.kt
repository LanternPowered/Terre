/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl.plugin

import java.net.URL
import java.net.URLClassLoader

internal class PluginClassLoader(
  urls: Array<out URL> = emptyArray(),
  parent: ClassLoader? = Thread.currentThread().contextClassLoader
) : URLClassLoader(urls, parent) {

  public override fun addURL(url: URL) {
    super.addURL(url)
  }
}

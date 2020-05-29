/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl

import java.io.File
import java.net.MalformedURLException
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Paths
import java.security.CodeSource

private object Context

internal fun main(args: Array<String>) {
  System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager")

  val source: CodeSource? = Context::class.java.protectionDomain.codeSource
  val location = source?.location

  val urls = mutableListOf<URL>()

  val classPath = System.getProperty("java.class.path")
  val libraries = classPath.split(File.pathSeparator).toTypedArray()
  for (library in libraries) {
    try {
      val url: URL = Paths.get(library).toUri().toURL()
      if (!library.endsWith(".jar") || url == location)
        urls.add(url)
    } catch (ignored: MalformedURLException) {
      println("Invalid library found in the class path: $library")
    }
  }

  val bootstrapClassLoader = Context::class.java.classLoader

  // Construct a URL class loader
  val classLoader = URLClassLoader(urls.toTypedArray(), bootstrapClassLoader)
  Thread.currentThread().contextClassLoader = classLoader

  // Load the proxy using the new class loader
  Class.forName("org.lanternpowered.terre.impl.ProxyImpl", true, classLoader)

  // Initialize the proxy
  ProxyImpl.init()
}

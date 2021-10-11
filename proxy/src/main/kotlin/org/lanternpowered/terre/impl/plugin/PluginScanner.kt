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

import org.lanternpowered.terre.impl.Terre
import org.lanternpowered.terre.impl.plugin.asm.PluginClassVisitor
import org.lanternpowered.terre.plugin.InvalidPluginException
import org.objectweb.asm.ClassReader
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.nio.file.DirectoryStream
import java.nio.file.FileVisitOption
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.jar.JarInputStream

internal class PluginScanner {

  private val mutablePlugins = mutableMapOf<String, PluginCandidate>()
  private val pluginClasses = mutableSetOf<String>()

  private var classFileVisitor = object : SimpleFileVisitor<Path>() {
    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
      visitClasspathFile(file)
      return FileVisitResult.CONTINUE
    }
  }

  val plugins: Collection<PluginCandidate>
    get() = mutablePlugins.values

  fun scanClassPath(urls: Iterable<URL>) {
    val sources = mutableSetOf<URI>()
    for (url in urls) {
      if (url.protocol != "file") {
        Terre.logger.warn("Skipping unsupported classpath source: $url")
        continue
      }
      if (url.path.startsWith(JAVA_HOME)) {
        Terre.logger.trace("Skipping JRE classpath entry: $url")
        continue
      }
      val source: URI = try {
        url.toURI()
      } catch (e: URISyntaxException) {
        Terre.logger.error("Failed to search for classpath plugins in $url")
        continue
      }
      if (sources.add(source)) {
        val path = Paths.get(source)
        if (Files.exists(path)) {
          if (Files.isDirectory(path)) {
            scanClasspathDirectory(path)
          } else if (JAR_FILE.matches(path)) {
            scanJar(path, true)
          }
        }
      }
    }
  }

  private fun scanClasspathDirectory(dir: Path) {
    Terre.logger.trace("Scanning $dir for plugins")
    try {
      Files.walkFileTree(dir, setOf(FileVisitOption.FOLLOW_LINKS), Int.MAX_VALUE, classFileVisitor)
    } catch (e: IOException) {
      Terre.logger.error("Failed to search for plugins in $dir", e)
    }
  }

  private fun visitClasspathFile(path: Path) {
    if (CLASS_FILE.matches(path)) {
      try {
        Files.newInputStream(path).use { inputStream ->
          val candidate: PluginCandidate? = scanClassFile(inputStream, null)
          if (candidate != null)
            addCandidate(candidate)
        }
      } catch (e: IOException) {
        Terre.logger.error("Failed to search for plugins in $path", e)
      }
    }
  }

  fun scanDirectory(path: Path) {
    try {
      Files.newDirectoryStream(path, JAR_FILTER).use { dir ->
        for (jar in dir)
          scanJar(jar, false)
      }
    } catch (e: IOException) {
      Terre.logger.error("Failed to search for plugins in $path", e)
    }
  }

  private fun scanJar(path: Path, classpath: Boolean) {
    Terre.logger.trace("Scanning $path for plugins")
    val candidates = mutableListOf<PluginCandidate>()

    // Open the zip file so we can scan it for plugins
    try {
      JarInputStream(BufferedInputStream(Files.newInputStream(path))).use { jar ->
        while (true) {
          val entry = jar.nextEntry ?: break
          if (entry.isDirectory || !entry.name.endsWith(CLASS_EXTENSION))
            continue
          val candidate = scanClassFile(jar, path)
          if (candidate != null)
            candidates.add(candidate)
        }
      }
    } catch (e: IOException) {
      Terre.logger.error("Failed to scan plugin JAR: {}", path, e)
      return
    }
    if (candidates.isNotEmpty()) {
      var success = false
      for (candidate in candidates)
        success = success or addCandidate(candidate)
    } else if (!classpath) {
      Terre.logger.error("No valid plugins found in $path. Is the file actually a plugin JAR?")
    }
  }

  private fun addCandidate(candidate: PluginCandidate): Boolean {
    val className = candidate.className
    val id = candidate.id
    if (!ID_REGEX.matches(id)) {
      Terre.logger.error("Skipping plugin with invalid plugin ID '$id' from ${candidate.source}. $ID_WARNING")
      return false
    }
    if (mutablePlugins.containsKey(id)) {
      Terre.logger.error("Skipping plugin with duplicate plugin ID '$id' from ${candidate.source}")
      return false
    }
    if (!pluginClasses.add(className)) {
      Terre.logger.error("Skipping duplicate plugin class $className from ${candidate.source}")
      return false
    }
    mutablePlugins[id] = candidate
    return true
  }

  private fun scanClassFile(inputStream: InputStream, source: Path?): PluginCandidate? {
    val reader = ClassReader(inputStream)
    val visitor = PluginClassVisitor()
    try {
      reader.accept(visitor, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
      val pluginId = visitor.pluginId
        ?: return null
      return PluginCandidate(visitor.className.replace('/', '.'), pluginId, source)
    } catch (e: InvalidPluginException) {
      Terre.logger.error("Skipping invalid plugin ${visitor.className} from $source", e)
    }
    return null
  }

  companion object {

    private val ID_REGEX = "^[a-z][a-z0-9-_]{1,63}$".toRegex()
    private const val ID_WARNING = "Plugin IDs should be lowercase, and only contain characters " +
      "from a-z, dashes or underscores, start with a lowercase letter, and not exceed 64 characters."

    private const val CLASS_EXTENSION = ".class"
    private const val JAR_EXTENSION = ".jar"
    private val CLASS_FILE = PathMatcher { path: Path -> path.toString().endsWith(CLASS_EXTENSION) }
    private val JAR_FILE = PathMatcher { path: Path -> path.toString().endsWith(JAR_EXTENSION) }
    private val JAR_FILTER = DirectoryStream.Filter { path: Path -> path.toString().endsWith(JAR_EXTENSION) }
    private val JAVA_HOME = System.getProperty("java.home")
  }
}

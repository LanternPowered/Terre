/*
 * Terre
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) contributors
 *
 * This work is licensed under the terms of the MIT License (MIT). For
 * a copy, see 'LICENSE.txt' or <https://opensource.org/licenses/MIT>.
 */
package org.lanternpowered.terre.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Map;

/**
 * The main class of the standalone Terre application. Terre and its libraries are located in a
 * libs directory inside the standalone jar, they are first extracted and then loaded.
 */
public final class StandaloneMain {

  /**
   * The entrypoint.
   *
   * @param args The application arguments
   */
  public static void main(final String... args) {
    final var source = StandaloneMain.class.getProtectionDomain().getCodeSource();
    final var location = source.getLocation();

    if (!location.toString().endsWith(".jar"))
      throw new IllegalStateException("Not executed from a jar.");

    final URI jarUri;
    try {
      jarUri = new URI("jar", location.toURI().toString(), null);
    } catch (final URISyntaxException ex) {
      throw new IllegalStateException(ex);
    }
    final var env = Map.of("create", "true");
    final var urls = new ArrayList<URL>();
    try (final var fileSystem = FileSystems.newFileSystem(jarUri, env)) {
      final var libsPath = fileSystem.getPath("libs");
      final var outputLibsPath = Paths.get("libs");
      Files.createDirectories(outputLibsPath);
      try (final var libs = Files.list(libsPath)) {
        libs.forEach(lib -> {
          final var outputPath = outputLibsPath.resolve(lib.getFileName().toString());
          try {
            Files.copy(lib, outputPath, StandardCopyOption.REPLACE_EXISTING);
            urls.add(outputPath.toUri().toURL());
          } catch (final IOException ex) {
            throw new IllegalStateException(ex);
          }
        });
      }
    } catch (final IOException ex) {
      throw new IllegalStateException(ex);
    }

    // Construct a URL class loader
    final var classLoader = new URLClassLoader(urls.toArray(URL[]::new));
    Thread.currentThread().setContextClassLoader(classLoader);

    try {
      // Load the proxy using the new class loader
      final var mainClass = Class.forName(
        "org.lanternpowered.terre.impl.TerreMainKt", true, classLoader);
      mainClass.getMethod("main", String[].class)
        .invoke(null, new Object[] { args });
    } catch (final ReflectiveOperationException ex) {
      throw new IllegalStateException(ex);
    }
  }
}

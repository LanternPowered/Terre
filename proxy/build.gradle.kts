plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
}

dependencies {
  // Kotlin
  api(kotlin("stdlib-jdk8"))
  api(kotlin("reflect"))

  val coroutinesVersion = "1.5.2"
  api(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = coroutinesVersion)
  api(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-jdk8", version = coroutinesVersion)
  api(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json-jvm", version = "1.3.0-RC")

  // General utilities
  api(group = "com.google.guava", name = "guava", version = "30.1.1-jre")

  // Json
  implementation(group = "com.google.code.gson", name = "gson", version = "2.8.8")

  // Primitive collections
  implementation(group = "it.unimi.dsi", name = "fastutil", version = "8.5.6")

  // Configuration
  api(group = "com.uchuhimo", name = "konf-yaml", version = "0.22.1")

  // Launch Options
  implementation(group = "net.sf.jopt-simple", name = "jopt-simple", version = "5.0.4")

  // Networking
  implementation(group = "io.netty", name = "netty-all", version = "4.1.68.Final")

  // Cache
  implementation(group = "com.github.ben-manes.caffeine", name = "caffeine", version = "3.0.4")

  // Lambda generation
  implementation(group = "org.lanternpowered", name = "lmbda", version = "2.0.0")

  // ASM
  implementation(group = "org.ow2.asm", name = "asm", version = "9.2")

  // Plugins
  implementation(group = "org.spongepowered", name = "plugin-spi", version = "0.3.0")

  // Logging
  val log4jVersion = "2.14.1"
  implementation(group = "org.apache.logging.log4j", name = "log4j-core", version = log4jVersion)
  implementation(group = "org.apache.logging.log4j", name = "log4j-jul", version = log4jVersion)
  api(group = "org.apache.logging.log4j", name = "log4j-api", version = log4jVersion)
  implementation(group = "org.apache.logging.log4j", name = "log4j-iostreams", version = log4jVersion)
  implementation(group = "org.apache.logging.log4j", name = "log4j-slf4j-impl", version = log4jVersion)
  implementation(group = "com.lmax", name = "disruptor", version = "3.4.4")

  // Console
  implementation(group = "net.minecrell", name = "terminalconsoleappender", version = "1.3.0")
  implementation(group = "org.jline", name = "jline-terminal-jansi", version = "3.20.0")

  // Testing
  testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.8.0")
  testImplementation(kotlin(module = "test", version = "1.5.21"))
}

tasks {
  jar {
    exclude("**/*.java") // For module-info.java

    exclude("log4j2.xml")
    rename("log4j2_prod.xml", "log4j2.xml")
    // Only enable async logging outside dev mode, using async in combination
    // with code location logging is disabled by default to avoid performance
    // issues, but in dev we want to see the locations, so no async here
    // See https://logging.apache.org/log4j/2.x/manual/async.html @ Location, location, location...
    rename("log4j2_prod.component.properties", "log4j2.component.properties")
  }
}

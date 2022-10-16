plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
}

dependencies {
  // Kotlin
  api(kotlin("stdlib-jdk8"))
  api(kotlin("reflect"))

  val coroutinesVersion = "1.6.4"
  api(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = coroutinesVersion)
  api(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-jdk8", version = coroutinesVersion)
  api(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json-jvm", version = "1.4.0")

  // General utilities
  api(group = "com.google.guava", name = "guava", version = "31.1-jre")

  // Json
  implementation(group = "com.google.code.gson", name = "gson", version = "2.9.0")

  // Primitive collections
  implementation(group = "it.unimi.dsi", name = "fastutil-core", version = "8.5.9")

  // Configuration
  api(group = "com.uchuhimo", name = "konf-yaml", version = "1.1.2")

  // Launch Options
  implementation(group = "net.sf.jopt-simple", name = "jopt-simple", version = "5.0.4")

  // Networking
  implementation(group = "io.netty", name = "netty-all", version = "4.1.82.Final") {
    exclude(group = "io.netty", module = "netty-codec-haproxy")
    exclude(group = "io.netty", module = "netty-codec-http2")
    exclude(group = "io.netty", module = "netty-codec-http")
    exclude(group = "io.netty", module = "netty-codec-memcache")
    exclude(group = "io.netty", module = "netty-codec-mqtt")
    exclude(group = "io.netty", module = "netty-codec-redis")
    exclude(group = "io.netty", module = "netty-codec-smtp")
    exclude(group = "io.netty", module = "netty-codec-socks")
    exclude(group = "io.netty", module = "netty-codec-stomp")
    exclude(group = "io.netty", module = "netty-codec-xml")
    exclude(group = "io.netty", module = "netty-handler-proxy")
    exclude(group = "io.netty", module = "netty-transport-rxtx")
    exclude(group = "io.netty", module = "netty-transport-sctp")
    exclude(group = "io.netty", module = "netty-transport-udt")
  }

  // Cache
  implementation(group = "com.github.ben-manes.caffeine", name = "caffeine", version = "3.1.1")

  // Lambda generation
  implementation(group = "org.lanternpowered", name = "lmbda", version = "2.0.0")

  // ASM
  implementation(group = "org.ow2.asm", name = "asm", version = "9.4")

  // Plugins
  implementation(group = "org.spongepowered", name = "plugin-spi", version = "0.3.0")

  // Logging
  val log4jVersion = "2.19.0"
  implementation(group = "org.apache.logging.log4j", name = "log4j-core", version = log4jVersion)
  implementation(group = "org.apache.logging.log4j", name = "log4j-jul", version = log4jVersion)
  api(group = "org.apache.logging.log4j", name = "log4j-api", version = log4jVersion)
  implementation(group = "org.apache.logging.log4j", name = "log4j-iostreams", version = log4jVersion)
  implementation(group = "org.apache.logging.log4j", name = "log4j-slf4j-impl", version = log4jVersion)
  implementation(group = "com.lmax", name = "disruptor", version = "3.4.4")

  // Console
  implementation(group = "net.minecrell", name = "terminalconsoleappender", version = "1.3.0")
  implementation(group = "org.jline", name = "jline-terminal-jansi", version = "3.21.0")

  // Testing
  testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.9.0")
  testImplementation(kotlin(module = "test"))
}

tasks.jar {
  val name = "Terre"
  val version = rootProject.version
  val vendor = "LanternPowered"

  manifest {
    attributes(
      "Main-Class" to "org.lanternpowered.terre.impl.TerreMainKt",
      "Specification-Name" to name,
      "Specification-Version" to version,
      "Specification-Vendor" to vendor,
      "Implementation-Name" to name,
      "Implementation-Version" to version,
      "Implementation-Vendor" to vendor,
    )
  }
  exclude("**/*.java") // For module-info.java

  exclude("log4j2.xml")
  rename("log4j2_prod.xml", "log4j2.xml")
  // Only enable async logging outside dev mode, using async in combination
  // with code location logging is disabled by default to avoid performance
  // issues, but in dev we want to see the locations, so no async here
  // See https://logging.apache.org/log4j/2.x/manual/async.html @ Location, location, location...
  rename("log4j2_prod.component.properties", "log4j2.component.properties")
}

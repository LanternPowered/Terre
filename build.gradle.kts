plugins {
  java
  eclipse
  idea
  kotlin("jvm") version "1.3.61"
  id("net.minecrell.licenser") version "0.4.1"
}

defaultTasks("licenseFormat", "build")

group = "org.lanternpowered"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
  maven("https://repo.spongepowered.org/maven/")
  maven("https://jitpack.io")
  maven("https://kotlin.bintray.com/kotlinx")
  maven("https://oss.sonatype.org/content/groups/public")
}

dependencies {
  // Kotlin
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))

  val coroutinesVersion = "1.3.0-RC2"
  implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = coroutinesVersion)
  implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-jdk8", version = coroutinesVersion)
  implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-runtime", version = "0.13.0")

  // General utilities
  implementation(group = "com.google.guava", name = "guava", version = "28.0-jre")

  // Json
  implementation(group = "com.google.code.gson", name = "gson", version = "2.8.6")

  // Primitive collections
  implementation(group = "it.unimi.dsi", name = "fastutil", version = "8.3.0")

  // Configuration
  implementation(group = "com.uchuhimo", name = "konf-toml", version = "0.21.0")

  // Launch Options
  implementation(group = "net.sf.jopt-simple", name = "jopt-simple", version = "5.0.4")

  // Networking
  implementation(group = "io.netty", name = "netty-all", version = "4.1.38.Final")

  // Cache
  implementation(group = "com.github.ben-manes.caffeine", name = "caffeine", version = "2.8.1")

  // Lambda generation
  implementation(group = "org.lanternpowered", name = "lmbda", version = "2.0.0-SNAPSHOT")

  // Logging
  val log4jVersion = "2.12.1"
  runtime(group = "org.apache.logging.log4j", name = "log4j-slf4j-impl", version = log4jVersion)
  runtime(group = "org.apache.logging.log4j", name = "log4j-core", version = log4jVersion)
  implementation(group = "org.apache.logging.log4j", name = "log4j-api", version = log4jVersion)
  implementation(group = "org.apache.logging.log4j", name = "log4j-iostreams", version = log4jVersion)
  runtime(group = "com.lmax", name = "disruptor", version = "3.4.2")

  // Console
  implementation(group = "net.minecrell", name = "terminalconsoleappender", version = "1.2.0")
  implementation(group = "org.jline", name = "jline-terminal-jansi", version = "3.12.1")

  // Testing
  testCompile(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.2.0")
  testCompile(kotlin(module = "test", version = "1.3.41"))
}

tasks {
  val baseName = "terre"

  jar {
    archiveBaseName.set(baseName)

    exclude("**/*.java") // For module-info.java

    exclude("log4j2.xml")
    rename("log4j2_prod.xml", "log4j2.xml")
    // Only enable async logging outside dev mode, using async in combination
    // with code location logging is disabled by default to avoid performance
    // issues, but in dev we want to see the locations, so no async here
    // See https://logging.apache.org/log4j/2.x/manual/async.html @ Location, location, location...
    rename("log4j2_prod.component.properties", "log4j2.component.properties")
  }

  val javadocJar = create<Jar>("javadocJar") {
    archiveBaseName.set(baseName)
    archiveClassifier.set("javadoc")
    from(javadoc)
  }

  val sourceJar = create<Jar>("sourceJar") {
    archiveBaseName.set(baseName)
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
    exclude("**/*.class") // For module-info.class
  }

  assemble {
    dependsOn(sourceJar)
    dependsOn(javadocJar)
  }

  artifacts {
    archives(jar.get())
    archives(sourceJar)
    archives(javadocJar)
  }

  listOf(jar.get(), sourceJar, javadocJar).forEach {
    it.from(project.file("LICENSE.txt"))
  }

  test {
    useJUnitPlatform()
  }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().forEach {
    it.kotlinOptions.apply {
      jvmTarget = "1.8"
      languageVersion = "1.3"

      val args = mutableListOf<String>()
      args += "-Xjvm-default=enable"

      fun useExperimentalAnnotation(name: String) {
        args += "-Xuse-experimental=$name"
      }

      fun enableLanguageFeature(name: String) {
        args += "-XXLanguage:+$name"
      }

      enableLanguageFeature("InlineClasses")
      enableLanguageFeature("NewInference")
      enableLanguageFeature("NonParenthesizedAnnotationsOnFunctionalTypes")

      useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
      useExperimentalAnnotation("kotlin.contracts.ExperimentalContracts")
      useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
      useExperimentalAnnotation("kotlin.experimental.ExperimentalTypeInference")

      freeCompilerArgs = args
    }
  }
}

license {
  header = rootProject.file("HEADER.txt")
  newLine = false
  ignoreFailures = false
  sourceSets = project.sourceSets

  include("**/*.java")
  include("**/*.kt")

  ext {
    set("name", project.name)
    set("url", "https://www.lanternpowered.org")
    set("organization", "LanternPowered")
  }
}

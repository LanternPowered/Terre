rootProject.name = "Terre"

pluginManagement {
  repositories {
    jcenter()
    mavenLocal()
    mavenCentral()
    maven("https://dl.bintray.com/lanternpowered/maven/")
    gradlePluginPortal()
  }

  resolutionStrategy {
    eachPlugin {
      if (requested.id.id.startsWith("org.lanternpowered.")) {
        val version = requested.version ?: "1.0.2"
        useModule("org.lanternpowered:lanterngradle:$version")
      }
    }
  }
}

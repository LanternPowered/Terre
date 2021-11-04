plugins {
  java
}

dependencies {
  implementation(project(":terre-proxy"))
}

tasks.jar {
  manifest {
    val common = project(":terre-proxy").tasks.jar.get().manifest.attributes
    val overrides = mapOf(
      "Main-Class" to "org.lanternpowered.terre.impl.StandaloneMain"
    )
    attributes(common + overrides)
  }

  for (lib in configurations.runtimeClasspath.get().resolvedConfiguration.resolvedArtifacts) {
    val dependency = dependencies.create(lib.moduleVersion.id.toString())
    from (lib.file) {
      rename {
        "libs/${dependency.name}-${dependency.version}.jar"
      }
    }
  }
}

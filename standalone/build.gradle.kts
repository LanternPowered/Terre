plugins {
  java
  `maven-publish`
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

publishing {
  val repoUsername: String? by project
  val repoPassword: String? by project
  val repoUrl: String? by project
  val projectUrl: String? by project

  if (repoUrl == null || repoUsername == null || repoPassword == null)
    return@publishing

  repositories {
    maven {
      url = uri(repoUrl!!)
      credentials {
        username = repoUsername
        password = repoPassword
      }
    }
  }

  publications {
    create<MavenPublication>("maven") {
      groupId = project.group.toString()
      artifactId = "terre"
      version = project.version.toString()
      from(components["java"])
      pom {
        url.set(projectUrl ?: "https://github.com/LanternPowered/Terre")
        licenses {
          license {
            name.set("The MIT License")
            url.set("https://opensource.org/licenses/MIT")
          }
        }
        developers {
          developer {
            id.set("Cybermaxke")
            name.set("Seppe Volkaerts")
            email.set("contact@seppevolkaerts.be")
          }
        }
        withXml {
          val node = asNode()
          node.remove(node.get("dependencies") as groovy.util.Node)
        }
      }
    }
  }
}

plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
  `maven-publish`
}

dependencies {
  api(project(":terre-proxy"))
}

System.setProperty("org.gradle.internal.publish.checksums.insecure", "true")

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

  val projectSha: String? by project
  var projectVersion: String = version.toString()
  if (projectSha != null)
    projectVersion = projectVersion.replace("SNAPSHOT", projectSha!!)

  publications {
    create<MavenPublication>("maven") {
      groupId = project.group.toString()
      artifactId = "portals"
      version = projectVersion
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
          // Delete dependencies section
          val root = asNode()
          val nodes = root["dependencies"] as groovy.util.NodeList
          if (nodes.isNotEmpty())
            root.remove(nodes.first() as groovy.util.Node)
        }
      }
    }
  }
}

tasks.withType<GenerateModuleMetadata> {
  enabled = false
}

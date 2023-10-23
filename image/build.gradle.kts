plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
  id("com.google.cloud.tools.jib") version "3.4.0"
}

dependencies {
  api(project(":terre-proxy"))
  api(project(":terre-portals"))
  api(project(":terre-k8s-server-finder"))
}

jib {
  val dockerUsername: String? by project
  val dockerPassword: String? by project
  from {
    image = "azul/zulu-openjdk-alpine:17-jre"
    if (!dockerUsername.isNullOrBlank() && !dockerPassword.isNullOrBlank()) {
      platforms {
        listOf("amd64", "arm64").forEach { arch ->
          platform {
            os = "linux"
            architecture = arch
          }
        }
      }
    }
  }
  to {
    image = "cybermaxke/terre"
    tags = setOf("latest")
    if (!dockerUsername.isNullOrBlank() && !dockerPassword.isNullOrBlank()) {
      auth {
        username = dockerUsername
        password = dockerPassword
      }
    }
  }
  container {
    mainClass = "org.lanternpowered.terre.impl.TerreMainKt"
    ports = listOf("7777")
    creationTime.set("USE_CURRENT_TIMESTAMP")
  }
}

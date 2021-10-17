plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
  id("com.google.cloud.tools.jib") version "3.1.4"
}

dependencies {
  api(project(":terre-proxy"))
  api(project(":terre-portals"))
  api(project(":terre-k8s-server-finder"))
}

jib {
  from {
    image = "azul/zulu-openjdk-alpine:16-jre"
  }
  to {
    image = "cybermaxke/terre"
    tags = setOf("latest")
    val dockerUsername: String? by project
    val dockerPassword: String? by project
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
    creationTime = "USE_CURRENT_TIMESTAMP"
  }
}

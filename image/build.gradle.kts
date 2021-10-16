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
    image = "azul/zulu-openjdk:16-jre"
  }
  to {
    image = "cybermaxke/terre"
    tags = setOf("latest")
  }
  container {
    mainClass = "org.lanternpowered.terre.impl.TerreMainKt"
    ports = listOf("7777")
  }
}

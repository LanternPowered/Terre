plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
}

dependencies {
  api(project(":terre-proxy"))
  implementation(group = "io.kubernetes", name = "client-java", version = "17.0.0")
}

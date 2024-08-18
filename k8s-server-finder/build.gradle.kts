plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
}

dependencies {
  api(project(":terre-proxy"))
  implementation(group = "io.kubernetes", name = "client-java", version = "21.0.0")
  // update json-smart version pulled by the kubernetes lib for vulnerabilities
  implementation(group = "net.minidev", name = "json-smart", version = "2.5.1")
}

plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
}

dependencies {
  implementation(project(":terre-proxy"))
  implementation(project(":terre-portals"))
  implementation(project(":terre-characters"))
  // implementation(project(":terre-tshock-users"))
}

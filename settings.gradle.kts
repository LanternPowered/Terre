rootProject.name = "Terre"

listOf("proxy", "image", "standalone", "k8s-server-finder", "portals", "tshock-users", "test").forEach {
  include(it)
  project(":$it").name = "terre-$it"
}

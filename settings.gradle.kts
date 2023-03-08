rootProject.name = "Terre"

listOf(
  "proxy",
  "image",
  "standalone",
  "k8s-server-finder",
  "portals",
  "characters",
  "test",
  "tshock-users",
).forEach {
  include(it)
  project(":$it").name = "terre-$it"
}

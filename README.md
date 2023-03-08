# Terre [![Discord](https://img.shields.io/badge/chat-on%20discord-6E85CF.svg)](https://discord.gg/ArSrsuU)

Terre is a Terraria proxy which aims to bring support for multi world and cross-platform play 
between mobile and desktop clients. It is possible to customize the proxy to your liking by 
installing plugins or even develop your own.

* [Source]
* [Issues]
* [Discussions]
* [Wiki]
* [Docker Images]
* [Helm Charts]

**The proxy is still is a work in progress, bugs are to be expected. There is a getting started 
page on the [wiki] and downloads are available as
[packages](https://github.com/orgs/LanternPowered/packages?repo_name=Terre).**
 
The following list gives an overview of each subproject in this repository.

* **proxy** - The actual proxy server.
* **image** - A docker image build containing the proxy and all the official plugins.
* **standalone** - Packs terre and all its dependencies into a single jar.
* **test** - A test environment which can be used in the IDE, which will include plugins from the 
  repository.
* **portals** - A plugin that makes it possible to create portals to teleport between servers 
  (worlds).
* **characters** - A plugin that allows configuration of character storages. As alternative to 
  client side characters. **WIP, is currently only used to test server side character 
  compatibility, no data is currently saved.**
* **k8s-server-finder** A plugin that discovers Terraria servers on a kubernetes cluster and 
  automatically registers them.
* **tshock-users** A plugin that hooks into the tShock user system to provide permission support
  on the proxy. Also improves server side character support when sharing characters between backing 
  servers. Requires that the tShock backing servers and this plugin are connected to the same
  MySQL database. The plugin only reads data from this database. **WIP, can be unstable, not all 
  login scenarios are tested.**

If you are looking for some more information, or you like to help out, feel free to hop on our 
discord server. You will find the button at the top.

## Prerequisites
* [Java 17] or newer, JDK 17 for development
* [Docker Desktop] when building and testing the docker images locally

## Clone
The following steps will ensure the project is cloned properly.
1. `git clone https://github.com/LanternPowered/Terre.git`
2. `cd Terre`

## Building
__Note:__ If you do not have [Gradle] installed then use `./gradlew` for Unix systems or Git Bash and 
`gradlew.bat` for Windows systems in place of any `gradle` command.

In order to build Terre you can use the following `gradle` commands.

### Standalone
Build the standalone jar, which can be found in `./standalone/build/libs` after running the 
following command. The jar is labeled similarly to `terre-standalone-x.x.x-SNAPSHOT.jar`.
```
gradle build
```
This also builds portals and the other plugins, the jar of these plugins can for example be 
found in `./portals/build/libs` and are labeled similarly to `terre-portals-x.x.x-SNAPSHOT.jar`. 
These plugin jars can be put into the plugins directory of Terre to enable extra functionality.

### Docker image
Build the docker image and publish it to the local docker daemon with the 
`cybermaxke/terre:latest` tag. The docker images contains all the official plugins by default, 
which can be disabled through the config.
```
gradle jibDockerBuild
```

[Source]: https://github.com/LanternPowered/Terre
[Issues]: https://github.com/LanternPowered/Terre/issues
[Discussions]: https://github.com/LanternPowered/Terre/discussions
[Wiki]: https://github.com/LanternPowered/Terre/wiki
[Docker Images]: https://hub.docker.com/r/cybermaxke/terre
[Java 17]: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
[Docker Desktop]: https://www.docker.com/products/docker-desktop
[Helm Charts]: https://github.com/Cybermaxke/terraria-helm-charts

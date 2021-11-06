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

**The proxy is still is a work in progress, bugs are to be expected. There are currently no official 
builds, so for now you will need to build the project yourself.**
 
The following list gives an overview of each sub-project in this repository.

* **proxy** - The actual proxy server.
* **image** - A docker image build containing the proxy and all the official plugins.
* **standalone** - Packs terre and all its dependencies into a single jar.
* **test** - A test environment which can be used in the IDE, which will include plugins from the 
repository.
* **portals** - A plugin which allows you to create portals to teleport between servers (worlds).
* **tshock-users** - A plugin which hooks into the tShock user and permission system, so they can 
work together. (Placeholder)
* **k8s-server-finder** A plugin that discovers Terraria servers on a kubernetes cluster and 
  automatically registers them.

If you are looking for some more information, or you like to help out, feel free to hop on our 
discord server. You will find the button at the top.

## Prerequisites
* [Java 16] or newer, JDK 16 for development
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
[Java 16]: https://www.oracle.com/java/technologies/javase/jdk16-archive-downloads.html
[Docker Desktop]: https://www.docker.com/products/docker-desktop
[Helm Charts]: https://github.com/Cybermaxke/terraria-helm-charts

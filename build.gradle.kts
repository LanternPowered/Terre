plugins {
  kotlin("jvm") version "1.3.72"
  kotlin("plugin.serialization") version "1.3.72"
  id("net.minecrell.licenser") version "0.4.1"
}

allprojects {
  apply(plugin = "java")
  apply(plugin = "idea")
  apply(plugin = "eclipse")

  defaultTasks("licenseFormat", "build")

  group = "org.lanternpowered"
  version = "1.0-SNAPSHOT"

  repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://kotlin.bintray.com/kotlinx")
    maven("https://oss.sonatype.org/content/groups/public")
  }
}

subprojects {
  afterEvaluate {
    apply(plugin = "net.minecrell.licenser")

    dependencies {
      implementation(kotlin("stdlib-jdk8"))
    }

    tasks {
      val javadocJar = create<Jar>("javadocJar") {
        archiveBaseName.set(project.name)
        archiveClassifier.set("javadoc")
        from(javadoc)
      }

      val sourceJar = create<Jar>("sourceJar") {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
        exclude("**/*.class") // For module-info.class
      }

      assemble {
        dependsOn(sourceJar)
        dependsOn(javadocJar)
      }

      artifacts {
        archives(jar.get())
        archives(sourceJar)
        archives(javadocJar)
      }

      listOf(jar.get(), sourceJar, javadocJar).forEach {
        it.from(project.file("LICENSE.txt"))
      }

      test {
        useJUnitPlatform()
      }

      withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().forEach {
        it.kotlinOptions.apply {
          jvmTarget = "1.8"
          languageVersion = "1.3"

          val args = mutableListOf<String>()
          args += "-Xjvm-default=enable"
          args += "-Xallow-result-return-type"

          fun useExperimentalAnnotation(name: String) {
            args += "-Xuse-experimental=$name"
          }

          fun enableLanguageFeature(name: String) {
            args += "-XXLanguage:+$name"
          }

          enableLanguageFeature("InlineClasses")
          enableLanguageFeature("NewInference")
          enableLanguageFeature("NonParenthesizedAnnotationsOnFunctionalTypes")

          useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
          useExperimentalAnnotation("kotlin.contracts.ExperimentalContracts")
          useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
          useExperimentalAnnotation("kotlin.experimental.ExperimentalTypeInference")
          useExperimentalAnnotation("kotlin.time.ExperimentalTime")
          useExperimentalAnnotation("kotlinx.serialization.UnstableDefault")

          freeCompilerArgs = args
        }
      }
    }

    license {
      header = rootProject.file("HEADER.txt")
      newLine = false
      ignoreFailures = false
      sourceSets = project.sourceSets

      include("**/*.java")
      include("**/*.kt")

      ext {
        set("name", rootProject.name)
        set("url", "https://www.lanternpowered.org")
        set("organization", "LanternPowered")
      }
    }
  }
}

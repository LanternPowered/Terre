plugins {
  kotlin("jvm") version "2.0.0"
  kotlin("plugin.serialization") version "2.0.0"
  id("org.cadixdev.licenser") version "0.6.1"
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
    maven("https://oss.sonatype.org/content/groups/public")
  }
}

subprojects {
  afterEvaluate {
    apply(plugin = "org.cadixdev.licenser")

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
          jvmTarget = "17"
          languageVersion = "1.9"

          val args = mutableListOf<String>()
          args += "-Xjvm-default=all"
          args += "-Xallow-result-return-type"

          fun optIn(name: String) {
            args += "-opt-in=$name"
          }

          fun enableLanguageFeature(name: String) {
            args += "-XXLanguage:+$name"
          }

          enableLanguageFeature("InlineClasses")
          enableLanguageFeature("NewInference")
          enableLanguageFeature("NonParenthesizedAnnotationsOnFunctionalTypes")

          optIn("kotlin.ExperimentalUnsignedTypes")
          optIn("kotlin.contracts.ExperimentalContracts")
          optIn("kotlin.ExperimentalStdlibApi")
          optIn("kotlin.experimental.ExperimentalTypeInference")
          optIn("kotlin.time.ExperimentalTime")
          optIn("kotlinx.coroutines.InternalCoroutinesApi")
          optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")

          freeCompilerArgs = args
        }
      }
    }

    license {
      header(rootProject.file("HEADER.txt"))
      newLine(false)
      ignoreFailures(false)

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

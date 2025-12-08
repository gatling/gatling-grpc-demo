plugins {
  idea
  kotlin("jvm")               version "2.2.21"
  kotlin("plugin.allopen")    version "2.2.21"

  id("com.google.protobuf")   version "0.9.5"
  id("com.diffplug.spotless") version "8.1.0"
  id("io.gatling.gradle")     version "3.14.9"
}

tasks.withType(JavaCompile::class) {
  options.release.set(21)
}

kotlin {
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
  }
}

repositories {
  mavenCentral()
}

dependencies {
  gatlingApi("com.google.protobuf:protobuf-kotlin:4.33.2")
  gatlingImplementation("io.gatling:gatling-grpc-java:3.14.9.1")
}

gatling {
  // Enterprise Cloud (https://cloud.gatling.io/) configuration reference: https://docs.gatling.io/reference/integrations/build-tools/gradle-plugin/#running-your-simulations-on-gatling-enterprise-cloud
}

var generatedSources = arrayOf(
  file("${protobuf.generatedFilesBaseDir}/gatling/java"),
  file("${protobuf.generatedFilesBaseDir}/gatling/kotlin"),
  file("${protobuf.generatedFilesBaseDir}/gatling/grpc")
)

idea {
  module {
    generatedSourceDirs.plusAssign(generatedSources)
  }
}

sourceSets.getByName("gatling") {
  java.srcDirs(generatedSources)
  kotlin.srcDirs(generatedSources)
}

protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:4.33.2"
  }
  plugins {
    create("grpc") {
      artifact = "io.grpc:protoc-gen-grpc-java:1.77.0"
    }
  }
  generateProtoTasks {
    ofSourceSet("gatling").forEach { task ->
      // A plugin somewhere doesn't handle task dependencies correctly on custom source sets
      tasks.getByName("compileGatlingKotlin").dependsOn(task)
      task.builtins {
        maybeCreate("java") // Used by kotlin and already defined by default
        create("kotlin")
      }
      task.plugins {
        create("grpc")
      }
    }
  }
}

spotless {
  kotlin {
    ktfmt()
      .googleStyle()
    toggleOffOn()
  }
}

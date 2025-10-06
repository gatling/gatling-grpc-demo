plugins {
  idea
  kotlin("jvm")               version "2.2.20"
  kotlin("plugin.allopen")    version "2.2.20"

  id("com.google.protobuf")   version "0.9.5"
  id("com.diffplug.spotless") version "8.0.0"
  id("io.gatling.gradle")     version "3.14.5.1"
}

repositories {
  mavenCentral()
}

dependencies {
  gatlingApi("com.google.protobuf:protobuf-kotlin:4.32.1")
  gatlingImplementation("io.gatling:gatling-grpc-java:3.14.5")
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
    artifact = "com.google.protobuf:protoc:4.32.1"
  }
  plugins {
    create("grpc") {
      artifact = "io.grpc:protoc-gen-grpc-java:1.75.0"
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

plugins {
  idea
  kotlin("jvm")               version "2.0.20"
  kotlin("plugin.allopen")    version "2.0.20"

  id("com.google.protobuf")   version "0.9.4"
  id("com.diffplug.spotless") version "6.25.0"
  id("io.gatling.gradle")     version "3.12.0.1"
}

repositories {
  mavenCentral()
}

dependencies {
  gatlingApi("com.google.protobuf:protobuf-kotlin:3.25.5")
  gatlingImplementation("io.gatling:gatling-grpc-java:3.12.0.1")
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
    artifact = "com.google.protobuf:protoc:3.25.5"
  }
  plugins {
    create("grpc") {
      artifact = "io.grpc:protoc-gen-grpc-java:1.68.0"
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

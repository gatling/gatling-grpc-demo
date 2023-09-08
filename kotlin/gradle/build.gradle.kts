plugins {
  idea
  kotlin("jvm")
  kotlin("plugin.allopen")

  id("com.google.protobuf")   version "0.9.4"
  id("com.diffplug.spotless") version "6.21.0"
  id("io.gatling.gradle")     version "3.9.5.5"
}

repositories {
  mavenCentral()
}

dependencies {
  gatlingApi("com.google.protobuf:protobuf-kotlin:3.24.3")
  gatlingImplementation("io.gatling:gatling-grpc-java:0.1.0-SNAPSHOT")
}

gatling {
  enterprise.closureOf<Any> {
    // Enterprise Cloud (https://cloud.gatling.io/) configuration reference: https://gatling.io/docs/gatling/reference/current/extensions/gradle_plugin/#working-with-gatling-enterprise-cloud
    // Enterprise Self-Hosted configuration reference: https://gatling.io/docs/gatling/reference/current/extensions/gradle_plugin/#working-with-gatling-enterprise-self-hosted
  }
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
    artifact = "com.google.protobuf:protoc:3.24.2"
  }
  plugins {
    create("grpc") {
      artifact = "io.grpc:protoc-gen-grpc-java:1.57.2"
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
  }
}

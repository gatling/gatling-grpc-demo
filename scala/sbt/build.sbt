enablePlugins(GatlingPlugin)

name := "gatling-grpc-demo-sbt-scala"

scalaVersion := "2.13.13"
scalacOptions := Seq(
  "-encoding", "UTF-8", "-release:8", "-deprecation",
  "-feature", "-unchecked", "-language:implicitConversions", "-language:postfixOps")

// Use Protobuf.javaSettings instead if you would prefer to use POJOs instead of objects generated by scalapb
Protobuf.scalaSettings

val gatlingVersion = "3.10.4"
val gatlingGrpcVersion = "3.10.5"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion     % "test,it"
libraryDependencies += "io.gatling"            % "gatling-grpc"              % gatlingGrpcVersion % "test,it"
libraryDependencies += "io.gatling"            % "gatling-test-framework"    % gatlingVersion     % "test,it"

// Enterprise Cloud (https://cloud.gatling.io/) configuration reference: https://gatling.io/docs/gatling/reference/current/extensions/sbt_plugin/#working-with-gatling-enterprise-cloud
// Enterprise Self-Hosted configuration reference: https://gatling.io/docs/gatling/reference/current/extensions/sbt_plugin/#working-with-gatling-enterprise-self-hosted

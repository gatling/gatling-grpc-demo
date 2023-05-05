import sbt.Keys._
import sbt._
import sbtprotoc.ProtocPlugin.autoImport._

object Protobuf {

  private val commons = Seq(
    PB.protocVersion := "3.22.3"
  )

  val javaSettings: Seq[Def.Setting[_]] = commons ++ Seq(
    Test / PB.targets := Seq(
      PB.gens.java -> (Test / sourceManaged).value,
      PB.gens.plugin("grpc-java") -> (Test / sourceManaged).value
    ),
    libraryDependencies ++= Seq(
      ("io.grpc" % "protoc-gen-grpc-java" % scalapb.compiler.Version.grpcJavaVersion).asProtocPlugin()
    )
  )

  val scalaSettings: Seq[Def.Setting[_]] = commons ++ Seq(
    Test / PB.targets := Seq(
      scalapb.gen() -> (Test / sourceManaged).value
    ),
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime"      % scalapb.compiler.Version.scalapbVersion % "protobuf",
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion % "test"
    )
  )
}

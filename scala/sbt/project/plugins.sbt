addSbtPlugin("io.gatling"         % "gatling-sbt"           % "4.19.0")
addSbtPlugin("com.thesamet"       % "sbt-protoc"            % "1.1.0-RC2")

libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "compilerplugin" % "1.0.0-alpha.6"
)

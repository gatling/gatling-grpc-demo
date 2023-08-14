addSbtPlugin("io.gatling"         % "gatling-sbt"           % "4.5.0")
addSbtPlugin("com.thesamet"       % "sbt-protoc"            % "1.0.6")

libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "compilerplugin" % "0.11.13"
)

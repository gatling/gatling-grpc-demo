addSbtPlugin("io.gatling"         % "gatling-sbt"           % "4.11.0")
addSbtPlugin("com.thesamet"       % "sbt-protoc"            % "1.0.7")

libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "compilerplugin" % "0.11.17"
)

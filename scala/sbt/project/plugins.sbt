addSbtPlugin("io.gatling"         % "gatling-sbt"           % "4.17.0")
addSbtPlugin("com.thesamet"       % "sbt-protoc"            % "1.0.8")

libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "compilerplugin" % "0.11.19"
)

# Gatling gRPC - Scala with SBT demo project

This folder contain a SBT project that shows how to use the Gatling gRPC DSL with Scala.

It is possible to use Java generated objects instead of Scala ones by modifying the build directly, check the
[build.sbt](https://github.com/gatling/gatling-grpc-demo/blob/main/scala/sbt/build.sbt#L11) file for more details.

If you want to run the scenarios over a working server, make sure to check the [server demo project](../../server)
beforehand.

## Usage

Both scenarios use a system property called `grpc.scenario` as a gRPC method switch. A value of `unary` is used to run
the scenario which implements a gRPC scenario using a unary method, and so on.

### Greeting simulation

The system property `grpc.scenario` can take the following values:

- unary
- deadlines

To run the Greeting simulation, use the `Gatling / testOnly` SBT task:

```console
sbt -Dgrpc.scenario=unary "Gatling / testOnly io.gatling.grpc.demo.GreetingSimulation"
```

### Calculator simulation

The system property `grpc.scenario` can take the following values:

- unary
- serverStreaming
- clientStreaming
- bidirectionalStreaming
- deadlines

To run the Calculator simulation, use the `Gatling / testOnly` SBT task:

```console
sbt -Dgrpc.scenario=unary "Gatling / testOnly io.gatling.grpc.demo.CalculatorSimulation"
```

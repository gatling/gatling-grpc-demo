# Gatling gRPC - Java with Gradle demo project

This folder contain a Gradle project that shows how to use the Gatling gRPC DSL with Java.

If you want to run the scenarios over a working server, make sure to check the [server demo project](../../server)
beforehand.

## Usage

Both scenarios use a system property called `grpc.scenario` as a gRPC method switch. A value of `unary` is used to run
the scenario which implements a gRPC scenario using a unary method, and so on.

### Greeting simulation

The system property `grpc.scenario` can take the following values:

- unary
- deadlines

To run the Greeting simulation, use the `gatlingRun` Gradle task:

```console
./gradlew -Dgrpc.scenario=unary gatlingRun --simulation io.gatling.grpc.demo.GreetingSimulation
```

### Calculator simulation

The system property `grpc.scenario` can take the following values:

- unary
- serverStreaming
- clientStreaming
- bidirectionalStreaming
- deadlines

To run the Calculator simulation, use the `gatlingRun` Gradle task:

```console
./gradlew -Dgrpc.scenario=unary gatlingRun --simulation io.gatling.grpc.demo.CalculatorSimulation
```

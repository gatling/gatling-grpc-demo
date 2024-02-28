# Server demo project

This a Gradle project contains a demo server meant to be used along the Gatling gRPC demo projects.

## Usage

The Gradle application plugin is used to configure the project, which can then be run with the Gradle task `run`

Running the task with no project property will run the Greeting server by default:

```console
./gradlew run
```

### Greeting server

To run the Greeting demo server:

```console
./gradlew -PmainClass=io.gatling.grpc.demo.server.greeting.GreetingServer run
```

It will run the service on the port `50051`.

### Calculator server

To run the Calculator demo server:

```console
./gradlew -PmainClass=io.gatling.grpc.demo.server.calculator.CalculatorServer run
```

It will run the service on the port `50052`.

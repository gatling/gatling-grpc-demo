import { atOnceUsers, exec, getParameter, repeat, simulation, scenario } from "@gatling.io/core";
import { grpc, response, statusCode } from "@gatling.io/grpc";

export default simulation((setUp) => {
  const baseGrpcProtocol = grpc.forAddress("localhost", 50052);

  const unary = scenario("Calculator Unary").exec(
    grpc("Sum")
      .unary("calculator.CalculatorService/Sum")
      .send({ first_number: 1, second_number: 2 })
      .check(statusCode().is("OK"), response((response) => response.sum_result).is(3))
  );

  const serverStream = grpc("Prime Number Decomposition")
    .serverStream("calculator.CalculatorService/PrimeNumberDecomposition")
    .check(
      statusCode().is("OK"),
      response((response) => response.prime_factor)
        .transform((p) => p === 2 || p === 5 || p === 17 || p === 97 || p === 6669961)
        .is(true)
    );

  const serverStreaming = scenario("Calculator Server Streaming").exec(
    serverStream.send({ number: 109987656890 }),
    serverStream.awaitStreamEnd()
  );

  const clientStream = grpc("Compute Average")
    .clientStream("calculator.CalculatorService/ComputeAverage")
    .check(statusCode().is("OK"), response((response) => response.average).saveAs("average"));

  const clientStreaming = scenario("Calculator Client Streaming").exec(
    clientStream.start(),
    repeat(10).on(
      clientStream.send((_) => {
        const number = Math.floor(Math.random() * 1000);
        return { number };
      })
    ),
    clientStream.halfClose(),
    clientStream.awaitStreamEnd((main, forked) => {
      const average = forked.get("average");
      return main.set("average", average);
    }),
    exec((session) => {
      const average = session.get("average");
      console.log(`average: ${average}`);
      return session;
    })
  );

  const bidirectionalStream = grpc("Find Maximum")
    .bidiStream("calculator.CalculatorService/FindMaximum")
    .check(statusCode().is("OK"), response((response) => response.maximum).saveAs("maximum"));

  const bidirectionalStreaming = scenario("Calculator Bidirectional Streaming").exec(
    bidirectionalStream.start(),
    repeat(10).on(
      bidirectionalStream.send((_) => {
        const number = Math.floor(Math.random() * 1000);
        return { number };
      })
    ),
    bidirectionalStream.halfClose(),
    bidirectionalStream.awaitStreamEnd((main, forked) => {
      const latestMaximum = forked.get("maximum");
      return main.set("maximum", latestMaximum);
    }),
    exec((session) => {
      const maximum = session.get("maximum");
      console.log(`maximum: ${maximum}`);
      return session;
    })
  );

  const deadlines = scenario("Calculator w/ Deadlines").exec(
    grpc("Square Root")
      .unary("calculator.CalculatorService/SquareRoot")
      .send({ number: -2 })
      .check(statusCode().is("INVALID_ARGUMENT"))
  );

  // npx gatling run --simulation=calculator grpc.scenario=unary
  // npx gatling run --simulation=calculator grpc.scenario=serverStreaming
  // npx gatling run --simulation=calculator grpc.scenario=clientStreaming
  // npx gatling run --simulation=calculator grpc.scenario=bidirectionalStreaming
  // npx gatling run --simulation=calculator grpc.scenario=deadlines

  const name = getParameter("grpc.scenario");
  let scn;
  if (name === "serverStreaming") {
    scn = serverStreaming;
  } else if (name === "clientStreaming") {
    scn = clientStreaming;
  } else if (name === "bidirectionalStreaming") {
    scn = bidirectionalStreaming;
  } else if (name === "deadlines") {
    scn = deadlines;
  } else {
    scn = unary;
  }

  setUp(scn.injectOpen(atOnceUsers(1))).protocols(baseGrpcProtocol);
});

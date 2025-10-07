import { Session, simulation, scenario, atOnceUsers, getParameter } from "@gatling.io/core";
import { grpc, response, statusCode } from "@gatling.io/grpc";

import { channelCredentials, randomNames } from "./feeders";

export default simulation((setUp) => {
  const baseGrpcProtocol = grpc
    .forAddress("localhost", 50051)
    .channelCredentials("#{channelCredentials}")
    .overrideAuthority("gatling-grpc-demo-test-server");

  const greeting = (session: Session) => {
    const firstName = session.get<string>("firstName");
    const lastName = session.get<string>("lastName");
    return {
      first_name: firstName,
      last_name: lastName
    };
  };

  const unary = scenario("Greet Unary")
    .feed(channelCredentials.circular())
    .feed(randomNames.circular())
    .exec((session) => session.setAll({ firstName: "hello", lastName: "warudo" }))
    .exec(
      grpc("Greet")
        .unary("greeting.GreetingService/Greet")
        .send((session: Session) => ({
          greeting: greeting(session)
        }))
        .check(
          statusCode().is("OK"),
          response((response) => response.result).isEL("Hello #{firstName} #{lastName}")
        )
    );

  const deadlines = scenario("Greet w/ Deadlines")
    .feed(channelCredentials.circular())
    .feed(randomNames.circular())
    .exec(
      grpc("Greet w/ Deadlines")
        .unary("greeting.GreetingService/GreetWithDeadline")
        .send((session: Session) => ({
          greeting: greeting(session)
        }))
        .deadlineAfter({ amount: 100, unit: "milliseconds" })
        .check(statusCode().is("DEADLINE_EXCEEDED"))
    );

  // npx gatling run --simulation=greeting grpc.scenario=unary
  // npx gatling run --simulation=greeting grpc.scenario=deadlines

  const name = getParameter("grpc.scenario");
  let scn;
  if (name === "deadlines") {
    scn = deadlines;
  } else {
    scn = unary;
  }

  setUp(scn.injectOpen(atOnceUsers(5))).protocols(baseGrpcProtocol);
});

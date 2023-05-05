package io.gatling.grpc.demo.server.greeting;

import io.gatling.grpc.demo.greeting.*;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

public class GreetingServiceImpl extends GreetingServiceGrpc.GreetingServiceImplBase {

    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        System.out.println("greet.request: " + request);

        // Extract the fields we need
        Greeting greeting = request.getGreeting();
        String firstName = greeting.getFirstName();
        String lastName = greeting.getLastName();

        // Create the response
        String result = "Hello " + firstName + " " + lastName;
        GreetResponse response = GreetResponse.newBuilder().setResult(result).build();

        // Send the response
        responseObserver.onNext(response);

        // Complete the RPC call
        responseObserver.onCompleted();
    }

    @Override
    public void greetWithDeadline(
            GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        Context context = Context.current();
        try {
            for (int i = 0; i < 3; i++) {
                if (!context.isCancelled()) {
                    Thread.sleep(100);
                } else {
                    System.out.println("Cancelled");
                    return;
                }
            }
            System.out.println("Send response");
            responseObserver.onNext(
                    GreetResponse.newBuilder()
                            .setResult("Hello " + request.getGreeting().getFirstName())
                            .build());
            responseObserver.onCompleted();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

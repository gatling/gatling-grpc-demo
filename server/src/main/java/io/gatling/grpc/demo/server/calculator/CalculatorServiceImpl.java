package io.gatling.grpc.demo.server.calculator;

import io.gatling.grpc.demo.calculator.*;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {
        int sum = request.getFirstNumber() + request.getSecondNumber();
        SumResponse response = SumResponse.newBuilder().setSumResult(sum).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void primeNumberDecomposition(
            PrimeNumberDecompositionRequest request,
            StreamObserver<PrimeNumberDecompositionResponse> responseObserver) {
        try {
            long factor = 2;
            long number = request.getNumber();
            while (number > 1) {
                if (number % factor == 0) {
                    number = number / factor;
                    PrimeNumberDecompositionResponse response = PrimeNumberDecompositionResponse.newBuilder()
                            .setPrimeFactor(factor)
                            .build();
                    responseObserver.onNext(response);
                    Thread.sleep(1000L);
                } else {
                    factor += 1;
                }
            }
        } catch (InterruptedException e) {
            responseObserver.onError(e);
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<ComputeAverageRequest> computeAverage(
            StreamObserver<ComputeAverageResponse> responseObserver) {
        return new StreamObserver<>() {
            int sum = 0;
            int count = 0;

            @Override
            public void onNext(ComputeAverageRequest value) {
                int number = value.getNumber();
                sum += number;
                count += 1;
            }

            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                double average = (double) sum / count;
                responseObserver.onNext(
                        ComputeAverageResponse.newBuilder().setAverage(average).build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<FindMaximumRequest> findMaximum(StreamObserver<FindMaximumResponse> responseObserver) {
        return new StreamObserver<>() {
            int maximum = Integer.MIN_VALUE;

            @Override
            public void onNext(FindMaximumRequest value) {
                if (value.getNumber() > maximum) {
                    maximum = value.getNumber();
                    responseObserver.onNext(
                            FindMaximumResponse.newBuilder().setMaximum(maximum).build());
                }
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void squareRoot(SquareRootRequest request, StreamObserver<SquareRootResponse> responseObserver) {
        int number = request.getNumber();
        if (number >= 0) {
            double numberRoot = Math.sqrt(number);
            responseObserver.onNext(
                    SquareRootResponse.newBuilder().setNumberRoot(numberRoot).build());
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("The number being sent is not positive")
                    .augmentDescription("Number sent: " + number)
                    .asRuntimeException());
        }
    }
}

package io.gatling.grpc.demo.server.echo;

import io.gatling.grpc.demo.echo.EchoServiceGrpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

public class EchoServiceImpl extends EchoServiceGrpc.EchoServiceImplBase {

    @Override
    public void echo(Empty request, StreamObserver<Empty> responseObserver) {
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}

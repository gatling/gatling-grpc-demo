# busybox's chmod doesn't support the X flag
FROM ubuntu:latest AS builder

COPY gatling-grpc-demo-server.tar .

RUN mkdir /app &&\
    tar -xvf /gatling-grpc-demo-server.tar -C /app --strip-components=1 &&\
    chmod -R a=rX /app

FROM gatlingcorp/openjdk-base:latest-jre-headless

ARG LABEL=gatling-grpc-demo-server
LABEL gatling="$LABEL"

COPY --from=builder --chown=1001:0 /app /app

WORKDIR /app
ENV HOME=/app
EXPOSE 50051 50052 50053
USER 1001
ENTRYPOINT ["/app/bin/gatling-grpc-demo-server"]
CMD []

#!/usr/bin/env bash

set -e
[[ "$DEBUG" ]] && set -x

CA_CN=gatling-grpc-demo-test-ca

CLIENT_CN_PREFIX=gatling-grpc-demo-test-client
SERVER_CN=gatling-grpc-demo-test-server

certs_dir=.certs
mkdir -p $certs_dir
rm -rf "${certs_dir:?}/*"

server_dir=server/src/main
client_dirs=(
  java/gradle/src/gatling
  java/maven/src/test
  javascript
  kotlin/gradle/src/gatling
  kotlin/maven/src/test
  scala/sbt/src/test
  typescript
)

## Certificate Authority

# Generate CA private key
openssl genrsa -passout pass:1111 -des3 -out $certs_dir/ca.key 4096

# Generate CA certificate
openssl req \
  -new -x509 \
  -key $certs_dir/ca.key -out $certs_dir/ca.crt \
  -days 3650 -passin pass:1111 \
  -subj "/C=FR/ST=Ile-de-France/L=Paris/O=Gatling gRPC Demo/OU=IT/CN=$CA_CN"

## Clients certificates

for i in {1..3}; do
  # Generate the client private key
  openssl genrsa -out $certs_dir/client$i.key 4096

  # Generate a Certificate Signing Request for the client
  openssl req -new \
    -key $certs_dir/client$i.key \
    -out $certs_dir/client$i.csr \
    -subj "/C=FR/ST=Ile-de-France/L=Paris/O=Gatling gRPC Demo/OU=IT/CN=${CLIENT_CN_PREFIX}$i"
  #openssl req -text -noout -verify -in $certs_dir/client$i.csr

  # Sign the request with the CA
  openssl x509 -req \
    -CA $certs_dir/ca.crt -CAkey $certs_dir/ca.key \
    -in $certs_dir/client$i.csr -out $certs_dir/client$i.crt \
    -days 3650 -passin pass:1111
  openssl x509 -in $certs_dir/client$i.crt -noout -serial
  openssl verify -verbose -CAfile $certs_dir/ca.crt $certs_dir/client$i.crt
done

## Server certificate

# Generate the server private key
openssl genrsa -out $certs_dir/server.key 4096

# Generate a Certificate Signing Request for the server
openssl req -new \
  -key $certs_dir/server.key -out $certs_dir/server.csr \
  -subj "/C=FR/ST=Ile-de-France/L=Paris/O=Gatling gRPC Demo/OU=IT/CN=$SERVER_CN"
#openssl req -text -noout -verify -in $certs_dir/server.csr

# Sign the request with the CA
openssl x509 -req \
  -CA $certs_dir/ca.crt -CAkey $certs_dir/ca.key \
  -in $certs_dir/server.csr -out $certs_dir/server.crt \
  -days 3650 -passin pass:1111
openssl x509 -in $certs_dir/server.crt -noout -serial
openssl verify -verbose -CAfile $certs_dir/ca.crt $certs_dir/server.crt

# Copy certificate and private key into the different projects

cp $certs_dir/ca.key $certs_dir/ca.crt $certs_dir/server.key $certs_dir/server.crt $server_dir/resources/certs

for client_dir in "${client_dirs[@]}"; do
  rm $client_dir/resources/certs/*
  cp $certs_dir/ca.crt $certs_dir/client*.key $certs_dir/client*.crt $client_dir/resources/certs
done

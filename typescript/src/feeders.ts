import { arrayFeeder } from "@gatling.io/core";
import { ChannelCredentials } from "@gatling.io/grpc";

const availableChannelCredentials: ChannelCredentials[] = [];
for (let i = 1; i <= 3; i++) {
  availableChannelCredentials.push({
    rootCerts: "certs/ca.crt",
    certChain: `certs/client${i}.crt`,
    privateKey: `certs/client${i}.key`
  });
}

export const channelCredentials = arrayFeeder(
  availableChannelCredentials.map((channelCredentials) => ({
    channelCredentials
  }))
);

const random = (alphabet: string, n: number): string => {
  let s = "";
  for (let i = 0; i < n; i++) {
    const index = Math.floor(Math.random() * alphabet.length);
    s += alphabet.charAt(index);
  }
  return s;
};

const randomString = (n: number): string =>
  random("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", n);

const randomNamesRecords = [];
for (let i = 0; i < 5; i++) {
  randomNamesRecords.push({
    firstName: randomString(20),
    lastName: randomString(20)
  });
}

export const randomNames = arrayFeeder(randomNamesRecords);

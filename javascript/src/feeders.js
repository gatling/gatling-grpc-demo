import { arrayFeeder } from "@gatling.io/core";

const availableChannelCredentials = [];
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

const random = (alphabet, n) => {
  let s = "";
  for (let i = 0; i < n; i++) {
    const index = Math.floor(Math.random() * alphabet.length);
    s += alphabet.charAt(index);
  }
  return s;
};

const randomString = (n) => random("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", n);

const randomNamesRecords = [];
for (let i = 0; i < 5; i++) {
  randomNamesRecords.push({
    firstName: randomString(20),
    lastName: randomString(20)
  });
}

export const randomNames = arrayFeeder(randomNamesRecords);

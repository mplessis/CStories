import { readFileSync, writeFileSync } from "node:fs";

const gradleProperties = readFileSync(new URL("../../gradle.properties", import.meta.url), "utf8");
const version = gradleProperties.match(/^cstoriesVersion=([0-9A-Za-z.+-]+)$/m)?.[1];

if (!version) {
  throw new Error("A valid cstoriesVersion is required in gradle.properties");
}

writeFileSync(
  new URL("../../website-home/version.js", import.meta.url),
  `window.CSTORIES_VERSION = "${version}";\n`,
);

console.log(`Updated website-home version to ${version}`);

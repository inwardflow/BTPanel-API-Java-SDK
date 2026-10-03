import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const openapiDir = path.join(root, "docs", "openapi");
const files = [
  "btpanel-developer-api.openapi-3.1.json",
  "btpanel-developer-api.strict.openapi-3.1.json",
  "btpanel-developer-api.observed.openapi-3.1.json",
];

function normalizeJsonFile(filePath) {
  const raw = fs.readFileSync(filePath, "utf8");
  const parsed = JSON.parse(raw);
  fs.writeFileSync(filePath, `${JSON.stringify(parsed, null, 2)}\n`, "utf8");
}

function main() {
  for (const file of files) {
    const fullPath = path.join(openapiDir, file);
    if (!fs.existsSync(fullPath)) {
      throw new Error(`Missing OpenAPI artifact: ${fullPath}`);
    }
    normalizeJsonFile(fullPath);
    console.log(`Validated ${path.relative(root, fullPath)}`);
  }
  console.log("OpenAPI artifacts are valid JSON and normalized.");
}

main();

import fs from "node:fs";
import path from "node:path";

const projectRoot = process.cwd();
const apiRoot = path.join(projectRoot, "src", "main", "java", "net", "heimeng", "sdk", "btapi", "api");
const developerSpecPath = path.join(
  projectRoot,
  "docs",
  "openapi",
  "btpanel-developer-api.openapi-3.1.json",
);

function listJavaFiles(dir) {
  const result = [];
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      result.push(...listJavaFiles(fullPath));
    } else if (entry.isFile() && entry.name.endsWith(".java")) {
      result.push(fullPath);
    }
  }
  return result;
}

function normalizeEndpoint(endpoint) {
  return endpoint.startsWith("/") ? endpoint : `/${endpoint}`;
}

function extractEffectiveEndpoint(filePath) {
  const source = fs.readFileSync(filePath, "utf8");
  const endpointMatch = source.match(/ENDPOINT\s*=\s*"([^"]+)"/);
  if (!endpointMatch) {
    return null;
  }

  const endpoint = endpointMatch[1];
  const actionLiteralMatch = source.match(/addParam\(\s*"action"\s*,\s*"([^"]+)"\s*\)/);

  if (endpoint.includes("action=") || !actionLiteralMatch) {
    return normalizeEndpoint(endpoint);
  }

  const separator = endpoint.includes("?") ? "&" : "?";
  return normalizeEndpoint(`${endpoint}${separator}action=${actionLiteralMatch[1]}`);
}

function collectSdkEndpoints() {
  const endpoints = new Map();
  for (const filePath of listJavaFiles(apiRoot)) {
    const endpoint = extractEffectiveEndpoint(filePath);
    if (!endpoint) {
      continue;
    }
    const fileName = path.basename(filePath);
    if (!endpoints.has(endpoint)) {
      endpoints.set(endpoint, []);
    }
    endpoints.get(endpoint).push(fileName);
  }
  return endpoints;
}

function collectOpenApiEndpoints(specPath) {
  const spec = JSON.parse(fs.readFileSync(specPath, "utf8"));
  const endpoints = new Map();

  for (const pathItem of Object.values(spec.paths ?? {})) {
    const operation = pathItem.post;
    if (!operation) {
      continue;
    }
    const endpoint = operation["x-bt-sdk-endpoint"];
    if (!endpoint) {
      continue;
    }
    const normalized = normalizeEndpoint(endpoint);
    if (!endpoints.has(normalized)) {
      endpoints.set(normalized, []);
    }
    endpoints.get(normalized).push(operation.operationId);
  }

  return endpoints;
}

function diffEndpoints(sourceMap, docMap) {
  const missingInDoc = [];
  const missingInSource = [];

  for (const [endpoint, owners] of sourceMap) {
    if (!docMap.has(endpoint)) {
      missingInDoc.push({ endpoint, owners });
    }
  }

  for (const [endpoint, operationIds] of docMap) {
    if (!sourceMap.has(endpoint)) {
      missingInSource.push({ endpoint, operationIds });
    }
  }

  return { missingInDoc, missingInSource };
}

const sdkEndpoints = collectSdkEndpoints();
const docEndpoints = collectOpenApiEndpoints(developerSpecPath);
const { missingInDoc, missingInSource } = diffEndpoints(sdkEndpoints, docEndpoints);

if (missingInDoc.length === 0 && missingInSource.length === 0) {
  console.log("OpenAPI sync check passed.");
  console.log(`SDK endpoints: ${sdkEndpoints.size}`);
  console.log(`Documented endpoints: ${docEndpoints.size}`);
  process.exit(0);
}

if (missingInDoc.length > 0) {
  console.error("Missing in OpenAPI:");
  for (const item of missingInDoc) {
    console.error(`- ${item.endpoint} <= ${item.owners.join(", ")}`);
  }
}

if (missingInSource.length > 0) {
  console.error("Missing in SDK source:");
  for (const item of missingInSource) {
    console.error(`- ${item.endpoint} <= ${item.operationIds.join(", ")}`);
  }
}

process.exit(1);

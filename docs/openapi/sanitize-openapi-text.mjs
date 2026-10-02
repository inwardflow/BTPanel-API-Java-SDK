import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const openapiDir = path.join(root, "docs", "openapi");

const jsonFiles = [
  "btpanel-developer-api.openapi-3.1.json",
  "btpanel-developer-api.strict.openapi-3.1.json",
  "btpanel-developer-api.observed.openapi-3.1.json",
  "live-validation-report.json",
  "ssl-live-validation-2026-03-29.json",
  "ssl-devtools-capture-validation-2026-03-29.json",
];

const summaryByUrl = new Map([
  ["/system?action=GetSystemTotal", "Get system overview"],
  ["/system?action=GetDiskInfo", "Get disk usage list"],
  ["/system?action=GetNetWork", "Get network metrics"],
  ["/ajax?action=UpdatePanel", "Check panel update"],
  ["/ajax?action=GetTaskCount", "Get pending task count"],
  ["/site?action=get_site_types", "List site categories"],
  ["/data?action=getData&table=sites", "List websites (legacy data endpoint)"],
  ["/datalist/data/get_data_list?table=sites", "List websites"],
  ["/site?action=AddSite", "Create website"],
  ["/site?action=DeleteSite", "Delete website"],
  ["/database?action=CheckDatabaseStatus", "Check database service status"],
  ["/datalist/data/get_data_list?table=databases", "List databases"],
  ["/database", "Create database"],
  ["/database?action=AddDatabase", "Create database"],
  ["/database?action=DeleteDatabase", "Delete database"],
  ["/database?action=ChangeDBPassword", "Change database password"],
  ["/ftp?action=getData", "List FTP accounts (legacy endpoint)"],
  ["/datalist/data/get_data_list?table=ftps", "List FTP accounts"],
  ["/ftp?action=AddFtp", "Create FTP account (legacy endpoint)"],
  ["/ftp?action=AddUser", "Create FTP account"],
  ["/ftp?action=SetUser", "Update FTP account"],
  ["/ftp?action=ChangeFtpPassword", "Change FTP password"],
  ["/ftp?action=DeleteFtp", "Delete FTP account (legacy endpoint)"],
  ["/ftp?action=DeleteUser", "Delete FTP account"],
  ["/files?action=GetDirNew", "List directory entries"],
  ["/files?action=CreateDir", "Create directory"],
  ["/files?action=AddFolder", "Create directory (legacy endpoint)"],
  ["/files?action=DeleteDir", "Delete directory"],
  ["/files?action=DeleteFile", "Delete file or directory"],
  ["/files?action=GetFileBody", "Read file contents"],
  ["/files?action=SaveFileBody", "Save file contents"],
  ["/files?action=RenameFile", "Rename file or directory"],
  ["/files?action=MoveFile", "Move file or directory"],
  ["/files?action=Compress", "Compress files"],
  ["/files?action=UnCompress", "Extract archive"],
  ["/ssl?action=get_cert_list", "List saved SSL certificates"],
  ["/ssl?action=getData", "List saved SSL certificates (legacy endpoint)"],
  ["/site?action=GetSSL", "Get site SSL status"],
  ["/ssl?action=get_order_list", "List SSL orders for a site"],
  ["/ssl?action=GetSiteDomain", "Resolve deployable sites for certificate"],
  ["/ssl?action=SetBatchCertToSite", "Deploy saved certificate to site"],
  ["/site?action=GetSSLCertList", "List site SSL certificates (legacy endpoint)"],
  ["/site?action=CloseSSL", "Disable site SSL (legacy endpoint)"],
  ["/site?action=SetSSL", "Install site SSL from PEM (legacy endpoint)"],
  ["/ssl?action=SaveSSL", "Save SSL certificate (legacy endpoint)"],
  ["/ssl/cert/get_cert_list", "List saved SSL certificates (invalid captured route)"],
]);

const validationNoteByUrl = new Map([
  [
    "/ftp?action=AddFtp",
    'Live validation on 2026-03-28 returned `{"status":false,"msg":"指定参数无效!"}`. Use `AddUser` instead.',
  ],
  [
    "/ftp?action=ChangeFtpPassword",
    "The historical route still exists in documentation, but the current panel flow prefers `SetUser`. Parameter contract remains unverified.",
  ],
  [
    "/ftp?action=DeleteFtp",
    "The historical route is not the preferred current-panel delete flow. Use `DeleteUser` instead.",
  ],
  [
    "/database?action=ChangeDBPassword",
    'Live validation on 2026-03-28 returned `{"status":false,"msg":"指定参数无效!"}`. Action name or parameters likely changed.',
  ],
  [
    "/ssl?action=get_cert_list",
    "Passed live probe on 2026-03-29 and returned a JSON array instead of a `{status,data}` wrapper.",
  ],
  [
    "/site?action=GetSSL",
    "Passed DevTools capture replay and signed live probe on 2026-03-29. `status=false` means the site currently has no deployed certificate.",
  ],
  [
    "/ssl?action=get_order_list",
    "Passed DevTools capture replay and signed live probe on 2026-03-29.",
  ],
  [
    "/ssl?action=GetSiteDomain",
    "Passed DevTools capture replay and signed live probe on 2026-03-29.",
  ],
  [
    "/ssl?action=SetBatchCertToSite",
    "Passed DevTools capture replay and signed live probe on 2026-03-29 with a temporary site created and deleted during validation.",
  ],
  [
    "/ssl?action=getData",
    'Live validation on 2026-03-29 returned `{"status":false,"msg":"指定参数无效!"}`.',
  ],
  [
    "/site?action=GetSSLCertList",
    'Live validation on 2026-03-29 returned `{"status":false,"msg":"指定参数无效!"}` for both `id` and `siteName` payloads.',
  ],
  [
    "/site?action=CloseSSL",
    'Live validation on 2026-03-29 returned `{"status":false,"msg":"指定参数无效!"}`.',
  ],
  [
    "/site?action=SetSSL",
    "Live validation on 2026-03-29 returned HTTP 404 for both historical payload shapes.",
  ],
  [
    "/ssl?action=SaveSSL",
    "Live validation on 2026-03-29 returned HTTP 404.",
  ],
  [
    "/ssl/cert/get_cert_list",
    'Live validation on 2026-03-29 returned `{"status":false,"msg":"没有在模型中找到指定模块"}`.',
  ],
]);

const propertyDescriptionByName = new Map([
  ["p", "Page number."],
  ["limit", "Page size."],
  ["search", "Search keyword."],
  ["order", "Sort field or sort direction."],
  ["table", "Logical resource table name."],
  ["id", "Resource identifier."],
  ["name", "Resource name."],
  ["username", "Username."],
  ["password", "Password."],
  ["new_password", "New password."],
  ["ftp_username", "FTP username."],
  ["ftp_password", "FTP password."],
  ["path", "Absolute panel path."],
  ["ps", "Remark or display note."],
  ["siteName", "Website domain name."],
  ["webname", "Website definition payload."],
  ["cert_list", "JSON array string of certificate names."],
  ["BatchInfo", "JSON array string describing certificate deployment items."],
  ["request_time", "Unix timestamp in seconds."],
  ["request_token", "Developer API request signature."],
  ["action", "Action name."],
]);

const replacements = new Map([
  ["鎸囧畾鍙傛暟鏃犳晥!", "指定参数无效!"],
  ["娌℃湁鍦ㄦā鍨嬩腑鎵惧埌鎸囧畾妯″潡", "没有在模型中找到指定模块"],
  ["娣诲姞鎴愬姛", "添加成功"],
  ["淇敼鎴愬姛", "修改成功"],
  ["鍒犻櫎鎴愬姛", "删除成功"],
  ["绔欑偣鍒犻櫎鎴愬姛!", "站点删除成功!"],
  ["鐩綍鍒涘缓鎴愬姛!", "目录创建成功!"],
  ["鍒犻櫎鐩綍鎴愬姛!", "删除目录成功!"],
  ["236澶?", "236天"],
  ["娴嬭瘯鏁版嵁搴撳娉?", "测试数据库备注"],
  ["root鐎靛棛鐖渀", "root 密码"],
]);

function stripBom(value) {
  return typeof value === "string" ? value.replace(/^\uFEFF/u, "") : value;
}

function stripReplacementArtifacts(value) {
  if (typeof value !== "string" || value.length === 0) {
    return value;
  }

  return value
    .replace(/�\?/gu, "")
    .replace(/\uFFFD/gu, "")
    .replace(/[ \t]+$/gmu, "");
}

function sanitizeInlineRawPayloadLines(raw) {
  return raw
    .split(/\r?\n/u)
    .map((line) => {
      const match = line.match(/^(\s*)"responseRaw":\s*".*("?,?)$/u);
      if (!match) {
        return line;
      }

      const [, indent, suffix] = match;
      const trailingComma = suffix?.trim().endsWith(",") ? "," : "";
      return `${indent}"responseRaw": "<sanitized raw response omitted>"${trailingComma}`;
    })
    .join("\n");
}

function isSuspiciousText(value) {
  if (typeof value !== "string" || value.length === 0) {
    return false;
  }
  const suspiciousTokens = [
    "锟",
    "�?",
    "????",
    "瀹濆",
    "闈㈡澘",
    "寮€鍙",
    "鏂囨。",
    "鎸囧畾",
    "鏃犳晥",
    "娌℃湁",
    "妯″潡",
    "娣诲姞",
    "淇敼",
    "鍒犻櫎",
    "鐩綍",
    "缁熶竴",
    "璺緞",
    "鐎规繂顢",
  ];
  return suspiciousTokens.some((token) => value.includes(token));
}

function replaceKnownText(value) {
  if (typeof value !== "string") {
    return value;
  }
  let current = value;
  for (const [source, target] of replacements) {
    current = current.split(source).join(target);
  }
  return stripReplacementArtifacts(current);
}

function humanizeAction(action) {
  if (!action) {
    return "BTPanel operation";
  }
  return action
    .replace(/([a-z0-9])([A-Z])/g, "$1 $2")
    .replace(/_/g, " ")
    .replace(/\s+/g, " ")
    .trim();
}

function deriveSummary(pathKey, operation) {
  const realUrl = operation["x-bt-real-url"] ?? pathKey;
  if (summaryByUrl.has(realUrl)) {
    return summaryByUrl.get(realUrl);
  }

  const table = operation["x-bt-table"];
  if (table === "sites") {
    return "List websites";
  }
  if (table === "databases") {
    return "List databases";
  }
  if (table === "ftps") {
    return "List FTP accounts";
  }

  return humanizeAction(operation["x-bt-action"]);
}

function deriveSourceNote(operation) {
  const source = operation["x-bt-source"];
  if (!source) {
    return null;
  }
  if (source === "developer-stable") {
    return "Auth mode: developer API signature (`request_time` + `request_token`).";
  }
  if (source === "legacy-compatible") {
    return "Auth mode: developer API signature (`request_time` + `request_token`). This route is kept for compatibility.";
  }
  if (source.includes("devtools-capture")) {
    return "Source: captured in BTPanel UI and then revalidated with developer API signature.";
  }
  if (source === "panel-observed") {
    return "Source: observed in BTPanel UI. Any signed revalidation is recorded in `x-bt-auth-verified`.";
  }
  return `Source: ${source}.`;
}

function deriveValidationNote(realUrl, operation) {
  if (validationNoteByUrl.has(realUrl)) {
    return validationNoteByUrl.get(realUrl);
  }
  return operation["x-bt-validation-note"] ?? null;
}

function buildDescription(pathKey, operation) {
  const realUrl = operation["x-bt-real-url"] ?? pathKey;
  const summary = deriveSummary(pathKey, operation);
  const lines = [`${summary}.`, `Real request: \`POST ${realUrl}\`.`];
  const sdkEndpoint = operation["x-bt-sdk-endpoint"];

  if (sdkEndpoint && sdkEndpoint !== realUrl.replace(/^\//u, "")) {
    lines.push(`SDK endpoint: \`${sdkEndpoint}\`.`);
  }

  const sourceNote = deriveSourceNote(operation);
  if (sourceNote) {
    lines.push(sourceNote);
  }

  const validationNote = deriveValidationNote(realUrl, operation);
  if (validationNote) {
    lines.push(`Validation: ${replaceKnownText(validationNote)}`);
  }

  if (operation.deprecated) {
    lines.push("Status: deprecated compatibility route. Do not prefer this endpoint on current panel versions.");
  }

  return lines.join("\n\n");
}

function cleanSchemaProperties(schema) {
  if (!schema || typeof schema !== "object") {
    return;
  }

  if (schema.properties && typeof schema.properties === "object") {
    for (const [name, property] of Object.entries(schema.properties)) {
      if (!property || typeof property !== "object") {
        continue;
      }
      if (!property.description || isSuspiciousText(property.description)) {
        if (propertyDescriptionByName.has(name)) {
          property.description = propertyDescriptionByName.get(name);
        }
      } else {
        property.description = replaceKnownText(property.description);
      }
      cleanSchemaProperties(property);
    }
  }

  if (schema.items) {
    cleanSchemaProperties(schema.items);
  }
  if (schema.oneOf) {
    schema.oneOf.forEach(cleanSchemaProperties);
  }
  if (schema.anyOf) {
    schema.anyOf.forEach(cleanSchemaProperties);
  }
  if (schema.allOf) {
    schema.allOf.forEach(cleanSchemaProperties);
  }
  if (schema.contentSchema) {
    cleanSchemaProperties(schema.contentSchema);
  }
}

function cleanOperation(pathKey, operation) {
  operation.summary = deriveSummary(pathKey, operation);
  operation.description = buildDescription(pathKey, operation);

  if (operation.requestBody) {
    if (!operation.requestBody.description || isSuspiciousText(operation.requestBody.description)) {
      operation.requestBody.description =
        "Submit parameters as `application/x-www-form-urlencoded` unless the endpoint description states otherwise.";
    } else {
      operation.requestBody.description = replaceKnownText(operation.requestBody.description);
    }

    for (const mediaType of Object.values(operation.requestBody.content ?? {})) {
      cleanSchemaProperties(mediaType.schema);
    }
  }

  for (const response of Object.values(operation.responses ?? {})) {
    if (!response.description || isSuspiciousText(response.description)) {
      response.description = "Panel response.";
    } else {
      response.description = replaceKnownText(response.description);
    }

    for (const mediaType of Object.values(response.content ?? {})) {
      cleanSchemaProperties(mediaType.schema);
    }
  }
}

function deepReplaceKnownText(value) {
  if (typeof value === "string") {
    return replaceKnownText(value);
  }
  if (Array.isArray(value)) {
    return value.map(deepReplaceKnownText);
  }
  if (value && typeof value === "object") {
    for (const key of Object.keys(value)) {
      value[key] = deepReplaceKnownText(value[key]);
    }
  }
  return value;
}

function repairBrokenQuotedLines(raw) {
  return raw
    .split(/\r?\n/u)
    .map((line) => {
      const quoteCount = (line.match(/"/g) ?? []).length;
      const looksLikeQuotedProperty = /^\s*"[^"]+"\s*:\s*"/u.test(line);
      if (!looksLikeQuotedProperty || quoteCount % 2 === 0) {
        return line;
      }

      if (line.endsWith(",")) {
        return `${line.slice(0, -1)}",`;
      }
      return `${line}"`;
    })
    .join("\n");
}

function cleanOpenApiDocument(document, mode) {
  document.info = {
    ...document.info,
    title:
      mode === "strict"
        ? "BTPanel Developer API (Strict OpenAPI 3.1)"
        : mode === "observed"
          ? "BTPanel Developer API (Observed Draft)"
          : "BTPanel Developer API",
    description:
      mode === "strict"
        ? "Strict OpenAPI 3.1 alias document for BTPanel developer APIs. Use `x-bt-real-url` as the actual request target during debugging."
        : mode === "observed"
          ? "Observed BTPanel request catalog collected from UI behavior. This draft keeps captured routes together with any later developer-signature validation notes."
          : "Primary OpenAPI document for BTPanel developer APIs validated against the current SDK and live panel probes.",
  };

  if (Array.isArray(document.servers)) {
    document.servers = document.servers.map((server) => ({
      ...server,
      description:
        "Root BTPanel base URL. Do not append the panel entry path used for browser login pages.",
    }));
  }

  if (document.externalDocs) {
    document.externalDocs.description = "Historical official BTPanel PDF reference.";
  }

  const tagDescriptions = {
    System: "System status, disk, network, panel update, and task-count endpoints.",
    Site: "Website creation, listing, deletion, PHP, and SSL-related endpoints.",
    Database: "Database listing and write operations.",
    Files: "File and directory browsing plus read/write operations.",
    FTP: "FTP account listing and management endpoints.",
    SSL: "Saved certificate listing and deployment-related endpoints.",
  };

  if (Array.isArray(document.tags)) {
    document.tags = document.tags.map((tag) => ({
      ...tag,
      description: tagDescriptions[tag.name] ?? tag.description,
    }));
  }

  if (document.components?.securitySchemes) {
    const securitySchemes = document.components.securitySchemes;
    if (securitySchemes.BtRequestTime) {
      securitySchemes.BtRequestTime.description = "Unix timestamp in seconds.";
    }
    if (securitySchemes.BtRequestToken) {
      securitySchemes.BtRequestToken.description =
        "Developer API signature computed as `md5(String(request_time) + md5(apiKey))`.";
    }
    if (securitySchemes.BtSessionToken) {
      securitySchemes.BtSessionToken.description =
        "Browser session token observed in panel UI traffic. Not required for pure developer-signature routes.";
    }
  }

  const schemaDescriptions = {
    LooseObject: "Loose object payload used for BTPanel responses with unstable field sets.",
    LooseArray: "Loose array payload used for BTPanel responses with unstable item shapes.",
    BtMessageResponse: "Common BTPanel boolean/message wrapper.",
    BtPageResponse: "Common BTPanel paginated list response.",
    SystemTotalResponse: "Typical fields returned by the system overview endpoint.",
    CreateSiteResponse: "Typical fields returned by the website creation endpoint.",
  };

  if (document.components?.schemas) {
    for (const [name, schema] of Object.entries(document.components.schemas)) {
      if (schemaDescriptions[name]) {
        schema.description = schemaDescriptions[name];
      } else if (schema.description) {
        schema.description = replaceKnownText(schema.description);
      }
      cleanSchemaProperties(schema);
    }
  }

  for (const [pathKey, pathItem] of Object.entries(document.paths ?? {})) {
    if (pathItem.post) {
      cleanOperation(pathKey, pathItem.post);
    }
  }

  return deepReplaceKnownText(document);
}

function readJson(fileName) {
  const raw = sanitizeInlineRawPayloadLines(
    stripBom(fs.readFileSync(path.join(openapiDir, fileName), "utf8")),
  );
  try {
    return JSON.parse(raw);
  } catch (error) {
    const repaired = repairBrokenQuotedLines(raw);
    return JSON.parse(repaired);
  }
}

function writeJson(fileName, value) {
  fs.writeFileSync(path.join(openapiDir, fileName), `${JSON.stringify(value, null, 2)}\n`, "utf8");
}

function main() {
  const mainDoc = cleanOpenApiDocument(readJson("btpanel-developer-api.openapi-3.1.json"), "main");
  const strictDoc = cleanOpenApiDocument(
    readJson("btpanel-developer-api.strict.openapi-3.1.json"),
    "strict",
  );
  const observedDoc = cleanOpenApiDocument(
    readJson("btpanel-developer-api.observed.openapi-3.1.json"),
    "observed",
  );

  writeJson("btpanel-developer-api.openapi-3.1.json", mainDoc);
  writeJson("btpanel-developer-api.strict.openapi-3.1.json", strictDoc);
  writeJson("btpanel-developer-api.observed.openapi-3.1.json", observedDoc);

  for (const fileName of jsonFiles.slice(3)) {
    writeJson(fileName, deepReplaceKnownText(readJson(fileName)));
  }

  console.log("Sanitized OpenAPI and validation JSON text.");
}

main();

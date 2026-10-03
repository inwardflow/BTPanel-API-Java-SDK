import crypto from "node:crypto";
import fs from "node:fs";
import path from "node:path";

process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0";

const projectRoot = process.cwd();
const propsPath = path.join(projectRoot, "src", "test", "resources", "application-test.properties");
const outputPath = path.join(projectRoot, "docs", "openapi", "live-validation-report.json");

function parseProperties(text) {
  const props = {};
  for (const rawLine of text.split(/\r?\n/)) {
    const line = rawLine.trim();
    if (!line || line.startsWith("#")) {
      continue;
    }
    const separatorIndex = line.indexOf("=");
    if (separatorIndex < 0) {
      continue;
    }
    const key = line.slice(0, separatorIndex).trim();
    const value = line.slice(separatorIndex + 1).trim();
    props[key] = value;
  }
  return props;
}

function loadConfig() {
  const propsText = fs.readFileSync(propsPath, "utf8");
  const props = parseProperties(propsText);
  const baseUrl = process.env.BT_PANEL_BASE_URL ?? props.baseUrl;
  const apiKey = process.env.BT_PANEL_API_KEY ?? props.apiKey;
  if (!baseUrl || !apiKey) {
    throw new Error("Missing BT panel config: baseUrl/apiKey");
  }
  return {
    baseUrl: baseUrl.replace(/\/+$/, ""),
    apiKey,
    props,
  };
}

/**
 * Random throwaway password for resources the validator creates and deletes.
 * Generated per run so no credential-like literal lives in the repository.
 */
function generateTestPassword() {
  // The panel requires mixed character classes; the suffix guarantees them.
  return `${crypto.randomBytes(12).toString("base64url")}Aa1!`;
}

function createSignedParams(apiKey, businessParams = {}) {
  const requestTime = Math.floor(Date.now() / 1000).toString();
  const inner = crypto.createHash("md5").update(apiKey).digest("hex");
  const requestToken = crypto
    .createHash("md5")
    .update(requestTime + inner)
    .digest("hex");
  return {
    ...businessParams,
    request_time: requestTime,
    request_token: requestToken,
  };
}

function createRedactionContext(config, runtimeValues) {
  const replacements = new Map();
  const addReplacement = (value, placeholder) => {
    if (typeof value === "string" && value) {
      replacements.set(value, placeholder);
    }
  };

  addReplacement(config.baseUrl, "<panel-base-url>");
  addReplacement(runtimeValues.ftpUsername, "<ftp-username>");
  addReplacement(runtimeValues.ftpPassword, "<ftp-password>");
  addReplacement(runtimeValues.ftpPassword2, "<new-ftp-password>");
  addReplacement(runtimeValues.legacyFtpUsername, "<legacy-ftp-username>");
  addReplacement(runtimeValues.legacyFtpPassword, "<legacy-ftp-password>");
  addReplacement(runtimeValues.legacyFtpPassword2, "<new-legacy-ftp-password>");
  addReplacement(runtimeValues.ftpBasePath, "<ftp-base-path>");
  addReplacement(runtimeValues.ftpHomePath, "<ftp-home-path>");
  addReplacement(runtimeValues.legacyFtpPath, "<legacy-ftp-path>");
  addReplacement(runtimeValues.dbName, "<database-name>");
  addReplacement(runtimeValues.dbUser, "<database-user>");
  addReplacement(runtimeValues.dbPassword, "<database-password>");
  addReplacement(runtimeValues.dbPassword2, "<new-database-password>");
  addReplacement(runtimeValues.siteDomain, "<site-domain>");
  addReplacement(runtimeValues.siteWebroot, "<site-webroot>");

  return {
    replacements: [...replacements.entries()].sort((left, right) => right[0].length - left[0].length),
  };
}

function sanitizeString(value, context) {
  let sanitized = value;
  for (const [rawValue, placeholder] of context.replacements) {
    sanitized = sanitized.split(rawValue).join(placeholder);
  }
  return sanitized;
}

function sanitizeValue(key, value, context) {
  if (value === null || value === undefined) {
    return value;
  }
  if (Array.isArray(value)) {
    return value.map((item) => sanitizeValue("", item, context));
  }
  if (typeof value === "object") {
    return Object.fromEntries(
      Object.entries(value).map(([entryKey, entryValue]) => [
        entryKey,
        sanitizeValue(entryKey, entryValue, context),
      ]),
    );
  }
  if (typeof value !== "string") {
    return value;
  }

  if (/^(api[_-]?key|request_token|password|ftp_password|new_password)$/i.test(key)) {
    return `<${key || "secret"}>`;
  }
  if (/^(path|basePath|homePath|webroot)$/i.test(key)) {
    return sanitizeString(value, context);
  }
  if (key === "baseUrl") {
    return "<panel-base-url>";
  }
  return sanitizeString(value, context);
}

async function callPanel({ baseUrl, apiKey }, endpoint, businessParams = {}) {
  const allParams = createSignedParams(apiKey, businessParams);
  const query = new URLSearchParams(allParams).toString();
  const body = new URLSearchParams(allParams).toString();
  const url = `${baseUrl}${endpoint}${endpoint.includes("?") ? "&" : "?"}${query}`;

  const response = await fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
      "User-Agent": "btpanel-api-java-sdk/live-validator",
    },
    body,
  });

  const raw = await response.text();
  let json;
  try {
    json = JSON.parse(raw);
  } catch {
    json = null;
  }

  return {
    ok: response.ok,
    status: response.status,
    endpoint,
    params: businessParams,
    json,
    raw,
  };
}

function summarize(result) {
  const panelStatus = typeof result.json?.status === "boolean" ? result.json.status : null;
  return {
    endpoint: result.endpoint,
    statusCode: result.status,
    transportOk: result.ok,
    panelStatus,
    message: result.json?.msg ?? null,
  };
}

async function main() {
  const config = loadConfig();
  const timestamp = Date.now();
  const suffix = timestamp.toString().slice(-8);
  const ftpUsername = `sdkit${timestamp.toString().slice(-8)}`;
  const ftpPassword = generateTestPassword();
  const ftpPassword2 = generateTestPassword();
  const legacyFtpUsername = `lgit${timestamp.toString().slice(-8)}`;
  const legacyFtpPassword = generateTestPassword();
  const legacyFtpPassword2 = generateTestPassword();
  const ftpBasePath = `/www/wwwroot/.sdk-it-${timestamp}`;
  const ftpHomePath = `${ftpBasePath}/${ftpUsername}`;
  const legacyFtpPath = `${ftpBasePath}/${legacyFtpUsername}`;
  const dbName = `itdb${suffix}`;
  const dbUser = `itusr${suffix}`;
  const dbPassword = generateTestPassword();
  const dbPassword2 = generateTestPassword();
  const domainSuffixRaw = (config.props["test.domain"] ?? "test.example.com").trim();
  const domainSuffix = domainSuffixRaw.replace(/^\.+/, "");
  const siteDomain = `itsite-${suffix}.${domainSuffix}`;
  const siteWebroot = `/www/wwwroot/${siteDomain}`;
  const redactionContext = createRedactionContext(config, {
    ftpUsername,
    ftpPassword,
    ftpPassword2,
    legacyFtpUsername,
    legacyFtpPassword,
    legacyFtpPassword2,
    ftpBasePath,
    ftpHomePath,
    legacyFtpPath,
    dbName,
    dbUser,
    dbPassword,
    dbPassword2,
    siteDomain,
    siteWebroot,
  });

  const report = {
    generatedAt: new Date().toISOString(),
    baseUrl: sanitizeValue("baseUrl", config.baseUrl, redactionContext),
    checks: [],
    ftpFlow: sanitizeValue("", {
      username: ftpUsername,
      basePath: ftpBasePath,
      homePath: ftpHomePath,
    }, redactionContext),
    databaseFlow: sanitizeValue("", {
      name: dbName,
      user: dbUser,
    }, redactionContext),
    siteFlow: sanitizeValue("", {
      domain: siteDomain,
      webroot: siteWebroot,
    }, redactionContext),
  };

  const run = async (name, endpoint, params = {}) => {
    const result = await callPanel(config, endpoint, params);
    report.checks.push({
      name,
      ...summarize(result),
      requestParams: sanitizeValue("", params, redactionContext),
      responseJson: sanitizeValue("", result.json, redactionContext),
      responseRaw: sanitizeString(result.raw.slice(0, 1200), redactionContext),
    });
    return result;
  };

  await run("system.total", "/system?action=GetSystemTotal");
  await run("ftp.list.before", "/datalist/data/get_data_list", {
    table: "ftps",
    p: 1,
    limit: 100,
    search: "",
  });

  await run("file.mkdir.base", "/files?action=CreateDir", { path: ftpBasePath });
  await run("file.mkdir.home", "/files?action=CreateDir", { path: ftpHomePath });
  await run("file.mkdir.legacyHome", "/files?action=CreateDir", { path: legacyFtpPath });

  await run("ftp.addUser", "/ftp?action=AddUser", {
    ftp_username: ftpUsername,
    ftp_password: ftpPassword,
    path: ftpHomePath,
    ps: ftpUsername,
  });

  const listAfterCreate = await run("ftp.list.afterCreate", "/datalist/data/get_data_list", {
    table: "ftps",
    p: 1,
    limit: 200,
    search: ftpUsername,
  });

  const account = (listAfterCreate.json?.data ?? []).find((item) => item?.name === ftpUsername);
  const ftpId = Number(account?.id ?? 0);
  report.ftpFlow.detectedId = ftpId > 0 ? ftpId : null;

  if (ftpId > 0) {
    await run("ftp.setUser", "/ftp?action=SetUser", {
      id: ftpId,
      ftp_username: ftpUsername,
      new_password: ftpPassword2,
      path: ftpHomePath,
    });

    await run("ftp.deleteUser", "/ftp?action=DeleteUser", {
      id: ftpId,
      username: ftpUsername,
    });
  } else {
    report.checks.push({
      name: "ftp.setUser",
      skipped: true,
      reason: "Unable to detect temporary FTP account id after creation",
    });
    report.checks.push({
      name: "ftp.deleteUser",
      skipped: true,
      reason: "Unable to detect temporary FTP account id after creation",
    });
  }

  const legacyAddResult = await run("ftp.legacy.addFtp", "/ftp?action=AddFtp", {
    name: legacyFtpUsername,
    password: legacyFtpPassword,
    path: legacyFtpPath,
    size: 0,
    ps: 0,
  });

  const legacyCreated = legacyAddResult.json?.status === true;
  if (legacyCreated) {
    await run("ftp.legacy.changePassword", "/ftp?action=ChangeFtpPassword", {
      name: legacyFtpUsername,
      password: legacyFtpPassword2,
    });
    await run("ftp.legacy.deleteFtp", "/ftp?action=DeleteFtp", {
      name: legacyFtpUsername,
    });
  } else {
    report.checks.push({
      name: "ftp.legacy.changePassword",
      skipped: true,
      reason: "Legacy AddFtp did not succeed",
    });
    report.checks.push({
      name: "ftp.legacy.deleteFtp",
      skipped: true,
      reason: "Legacy AddFtp did not succeed",
    });
  }

  await run("file.delete.home", "/files?action=DeleteDir", { path: ftpHomePath });
  await run("file.delete.legacyHome", "/files?action=DeleteDir", { path: legacyFtpPath });
  await run("file.delete.base", "/files?action=DeleteDir", { path: ftpBasePath });

  await run("ssl.getData.legacy", "/ssl?action=getData", {
    table: "ssl",
    p: 1,
    limit: 100,
    search: "",
  });

  await run("ssl.list.observed", "/datalist/data/get_data_list", {
    table: "ssl",
    p: 1,
    limit: 100,
    search: "",
  });

  await run("database.list.before", "/datalist/data/get_data_list", {
    table: "databases",
    p: 1,
    limit: 200,
    search: "",
    order: "",
  });

  await run("database.add", "/database", {
    action: "AddDatabase",
    name: dbName,
    db_user: dbUser,
    password: dbPassword,
    codeing: "utf8mb4",
    dataAccess: "127.0.0.1",
    address: "127.0.0.1",
    dtype: "MySQL",
    ps: dbName,
    sid: 0,
    listen_ip: "0.0.0.0/0",
    host: "%",
  });

  const dbListAfterCreate = await run("database.list.afterCreate", "/datalist/data/get_data_list", {
    table: "databases",
    p: 1,
    limit: 200,
    search: dbName,
    order: "",
  });

  const dbItem = (dbListAfterCreate.json?.data ?? []).find((item) => item?.name === dbName);
  const dbId = Number(dbItem?.id ?? 0);
  report.databaseFlow.detectedId = dbId > 0 ? dbId : null;

  if (dbId > 0) {
    await run("database.changePassword", "/database?action=ChangeDBPassword", {
      name: dbName,
      username: dbUser,
      password: dbPassword2,
    });

    await run("database.delete", "/database?action=DeleteDatabase", {
      name: dbName,
      id: dbId,
    });
  } else {
    report.checks.push({
      name: "database.changePassword",
      skipped: true,
      reason: "Unable to detect temporary database id after creation",
    });
    report.checks.push({
      name: "database.delete",
      skipped: true,
      reason: "Unable to detect temporary database id after creation",
    });
  }

  const webname = JSON.stringify({
    domain: siteDomain,
    domainlist: [],
    count: 0,
  });

  await run("site.create", "/site?action=AddSite", {
    webname,
    path: siteWebroot,
    type_id: 0,
    type: "PHP",
    version: "81",
    port: 80,
    ps: siteDomain,
    ftp: 0,
    sql: 0,
  });

  const siteListAfterCreate = await run("site.list.afterCreate", "/datalist/data/get_data_list", {
    table: "sites",
    p: 1,
    limit: 200,
    search: siteDomain,
    type: -1,
    order: "",
  });

  const siteItem = (siteListAfterCreate.json?.data ?? []).find((item) => item?.name === siteDomain);
  const siteId = Number(siteItem?.id ?? 0);
  report.siteFlow.detectedId = siteId > 0 ? siteId : null;

  if (siteId > 0) {
    await run("site.delete", "/site?action=DeleteSite", {
      id: siteId,
      webname: siteDomain,
      ftp: 0,
      database: 0,
      path: 1,
    });
  } else {
    report.checks.push({
      name: "site.delete",
      skipped: true,
      reason: "Unable to detect temporary site id after creation",
    });
  }

  fs.writeFileSync(outputPath, `${JSON.stringify(report, null, 2)}\n`, "utf8");
  console.log(`Wrote ${path.relative(projectRoot, outputPath)}`);
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});

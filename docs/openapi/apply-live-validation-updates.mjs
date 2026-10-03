import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const mainPath = path.join(root, "docs", "openapi", "btpanel-developer-api.openapi-3.1.json");
const strictPath = path.join(
  root,
  "docs",
  "openapi",
  "btpanel-developer-api.strict.openapi-3.1.json",
);
const observedPath = path.join(
  root,
  "docs",
  "openapi",
  "btpanel-developer-api.observed.openapi-3.1.json",
);

function readJson(file) {
  return JSON.parse(fs.readFileSync(file, "utf8"));
}

function writeJson(file, data) {
  fs.writeFileSync(file, `${JSON.stringify(data, null, 2)}\n`, "utf8");
}

function markLegacyDeprecated(operation) {
  if (!operation) {
    return;
  }
  operation.deprecated = true;
  operation["x-bt-source"] = "legacy-compatible";
  operation.description = `${operation.description ?? ""}\n\nLive validation (2026-03-28): panel 9.0.0 returns "invalid parameter" for this legacy action.`;
}

function appendValidationNote(operation, note) {
  if (!operation) {
    return;
  }
  operation.description = `${operation.description ?? ""}\n\n${note}`.trim();
}

function buildAddUserOperation() {
  return {
    post: {
      tags: ["FTP"],
      summary: "Create FTP account (validated)",
      operationId: "ftpAddUser",
      description:
        "Validated on 2026-03-28 against panel 9.0.0 using developer API signature.\n\nReal request: `POST /ftp?action=AddUser`.",
      requestBody: {
        required: true,
        content: {
          "application/x-www-form-urlencoded": {
            schema: {
              type: "object",
              additionalProperties: false,
              required: ["ftp_username", "ftp_password", "path", "ps"],
              properties: {
                ftp_username: { type: "string", example: "demoftp" },
                ftp_password: { type: "string", example: "<password>" },
                path: { type: "string", example: "/www/wwwroot/demo.example.com" },
                ps: { type: "string", example: "demoftp" },
              },
            },
          },
        },
      },
      responses: {
        "200": {
          description: "Panel response",
          content: {
            "application/json": {
              schema: { $ref: "#/components/schemas/BtMessageResponse" },
              examples: {
                success: { value: { status: true, msg: "添加成功" } },
              },
            },
          },
        },
      },
      "x-bt-real-path": "/ftp",
      "x-bt-real-url": "/ftp?action=AddUser",
      "x-bt-source": "developer-stable",
      "x-bt-auth-mode": "developer-signature",
      "x-bt-action": "AddUser",
      "x-bt-sdk-endpoint": "ftp?action=AddUser",
    },
  };
}

function buildSetUserOperation() {
  return {
    post: {
      tags: ["FTP"],
      summary: "Update FTP account (validated)",
      operationId: "ftpSetUser",
      description:
        "Validated on 2026-03-28 against panel 9.0.0 using developer API signature.\n\nReal request: `POST /ftp?action=SetUser`.",
      requestBody: {
        required: true,
        content: {
          "application/x-www-form-urlencoded": {
            schema: {
              type: "object",
              additionalProperties: false,
              required: ["id", "ftp_username", "new_password", "path"],
              properties: {
                id: { type: "integer", example: 1 },
                ftp_username: { type: "string", example: "demoftp" },
                new_password: { type: "string", example: "New<password>" },
                path: { type: "string", example: "/www/wwwroot/demo.example.com" },
              },
            },
          },
        },
      },
      responses: {
        "200": {
          description: "Panel response",
          content: {
            "application/json": {
              schema: { $ref: "#/components/schemas/BtMessageResponse" },
              examples: {
                success: { value: { status: true, msg: "修改成功" } },
              },
            },
          },
        },
      },
      "x-bt-real-path": "/ftp",
      "x-bt-real-url": "/ftp?action=SetUser",
      "x-bt-source": "developer-stable",
      "x-bt-auth-mode": "developer-signature",
      "x-bt-action": "SetUser",
      "x-bt-sdk-endpoint": "ftp?action=SetUser",
    },
  };
}

function buildDeleteUserOperation() {
  return {
    post: {
      tags: ["FTP"],
      summary: "Delete FTP account (validated)",
      operationId: "ftpDeleteUser",
      description:
        "Validated on 2026-03-28 against panel 9.0.0 using developer API signature.\n\nReal request: `POST /ftp?action=DeleteUser`.",
      requestBody: {
        required: true,
        content: {
          "application/x-www-form-urlencoded": {
            schema: {
              type: "object",
              additionalProperties: false,
              required: ["id", "username"],
              properties: {
                id: { type: "integer", example: 1 },
                username: { type: "string", example: "demoftp" },
              },
            },
          },
        },
      },
      responses: {
        "200": {
          description: "Panel response",
          content: {
            "application/json": {
              schema: { $ref: "#/components/schemas/BtMessageResponse" },
              examples: {
                success: { value: { status: true, msg: "删除成功" } },
              },
            },
          },
        },
      },
      "x-bt-real-path": "/ftp",
      "x-bt-real-url": "/ftp?action=DeleteUser",
      "x-bt-source": "developer-stable",
      "x-bt-auth-mode": "developer-signature",
      "x-bt-action": "DeleteUser",
      "x-bt-sdk-endpoint": "ftp?action=DeleteUser",
    },
  };
}

function main() {
  const main = readJson(mainPath);
  const strict = readJson(strictPath);
  const observed = readJson(observedPath);

  markLegacyDeprecated(main.paths["/ftp?action=AddFtp"]?.post);
  markLegacyDeprecated(main.paths["/ftp?action=DeleteFtp"]?.post);
  markLegacyDeprecated(main.paths["/ftp?action=ChangeFtpPassword"]?.post);

  main.paths["/ftp?action=AddUser"] = buildAddUserOperation();
  main.paths["/ftp?action=SetUser"] = buildSetUserOperation();
  main.paths["/ftp?action=DeleteUser"] = buildDeleteUserOperation();

  strict.paths["/bt/ftp/AddUser"] = buildAddUserOperation();
  strict.paths["/bt/ftp/SetUser"] = buildSetUserOperation();
  strict.paths["/bt/ftp/DeleteUser"] = buildDeleteUserOperation();

  observed.paths["/ftp?action=SetUser"] = buildSetUserOperation();

  appendValidationNote(
    main.paths["/database?action=AddDatabase"]?.post,
    "Live validation (2026-03-28): works with developer API signature on panel 9.0.0.",
  );
  appendValidationNote(
    main.paths["/database?action=DeleteDatabase"]?.post,
    "Live validation (2026-03-28): works with developer API signature on panel 9.0.0.",
  );
  appendValidationNote(
    main.paths["/database?action=ChangeDBPassword"]?.post,
    'Live validation (2026-03-28): returned `{"status":false,"msg":"指定参数无效!"}` under developer signature; action or parameters likely changed or require page-session context.',
  );
  appendValidationNote(
    main.paths["/site?action=AddSite"]?.post,
    "Live validation (2026-03-28): works with developer API signature on panel 9.0.0.",
  );
  appendValidationNote(
    main.paths["/site?action=DeleteSite"]?.post,
    "Live validation (2026-03-28): works with developer API signature on panel 9.0.0.",
  );

  main.info.version = "2026-03-28";
  strict.info.version = "2026-03-28";
  observed.info.version = "2026-03-28";

  writeJson(mainPath, main);
  writeJson(strictPath, strict);
  writeJson(observedPath, observed);
  console.log("Applied live validation updates to OpenAPI files.");
}

main();

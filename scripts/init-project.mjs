#!/usr/bin/env node
// 템플릿으로 만든 새 저장소에서 한 번 실행한다.
//   node scripts/init-project.mjs --name my-shop --package com.acme.shop --owner my-github-id
//
// 하는 일
//   - 자바 패키지 com.example.template -> --package (폴더 이동 + 코드 치환)
//   - build.gradle.kts group -> 패키지의 앞부분 (com.acme.shop -> com.acme)
//   - README 제목, compose 프로젝트 이름 -> --name
//   - CODEOWNERS, LICENSE 소유자 -> --owner
// 끝나면 git diff로 확인하고 ./gradlew build 로 검증한다.
import { execFileSync } from "node:child_process";
import fs from "node:fs";
import path from "node:path";
import { parseArgs } from "node:util";

const OLD_PACKAGE = "com.example.template";
const OLD_GROUP = "com.example";
const OLD_NAME = "hexagonal-spring-next-template";

const { values } = parseArgs({
  options: {
    name: { type: "string" },
    package: { type: "string" },
    owner: { type: "string" },
    "dry-run": { type: "boolean", default: false },
  },
});

const usage = "사용법: node scripts/init-project.mjs --name <프로젝트명> --package <자바 패키지> --owner <GitHub ID>";
if (!values.name || !values.package || !values.owner) {
  console.error(usage);
  process.exit(1);
}
if (!/^[a-z][a-z0-9-]*$/.test(values.name)) {
  console.error("--name 은 소문자, 숫자, - 만 쓸 수 있습니다. 예) my-shop");
  process.exit(1);
}
if (!/^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$/.test(values.package)) {
  console.error("--package 는 소문자 자바 패키지 형식이어야 합니다. 예) com.acme.shop");
  process.exit(1);
}
if (!/^[A-Za-z0-9-]+$/.test(values.owner)) {
  console.error("--owner 는 GitHub 사용자/조직 ID 입니다. 예) my-github-id");
  process.exit(1);
}

const root = path.resolve(path.dirname(new URL(import.meta.url).pathname.replace(/^\/([A-Za-z]:)/, "$1")), "..");
const newPackage = values.package;
const newGroup = newPackage.split(".").slice(0, -1).join(".");
const dryRun = values["dry-run"];
const changed = [];

function trackedFiles() {
  return execFileSync("git", ["ls-files"], { cwd: root, encoding: "utf8" })
    .split("\n")
    .filter(Boolean);
}

const TEXT = /\.(java|kts|ya?ml|md|json|mjs|ts|tsx|properties|sql|alloy|xml)$|^(CODEOWNERS|LICENSE|Dockerfile)$/;
const SKIP = [/package-lock\.json$/, /schema\.d\.ts$/, /^openapi\//, /^scripts\/init-project\.mjs$/];

function rewrite(file, replacements) {
  const p = path.join(root, file);
  const before = fs.readFileSync(p, "utf8");
  let after = before;
  for (const [from, to] of replacements) after = after.split(from).join(to);
  if (after !== before) {
    changed.push(file);
    if (!dryRun) fs.writeFileSync(p, after);
  }
}

// 1. 내용 치환
for (const file of trackedFiles()) {
  const base = path.basename(file);
  if (!TEXT.test(base) || SKIP.some((re) => re.test(file))) continue;
  if (!fs.existsSync(path.join(root, file))) continue;

  const replacements = [
    [OLD_PACKAGE, newPackage],
    [OLD_PACKAGE.replaceAll(".", "/"), newPackage.replaceAll(".", "/")],
    [OLD_NAME, values.name],
    ["name: template-app", `name: ${values.name}`],
    ["name: template-observability", `name: ${values.name}-observability`],
  ];
  if (file === "backend/build.gradle.kts") replacements.push([`group = "${OLD_GROUP}"`, `group = "${newGroup}"`]);
  if (file === ".github/CODEOWNERS") replacements.push(["@klmin", `@${values.owner}`]);
  if (file === "LICENSE") replacements.push(["Copyright (c) 2026 klmin", `Copyright (c) ${new Date().getFullYear()} ${values.owner}`]);
  rewrite(file, replacements);
}

// 2. 패키지 폴더 이동
for (const sourceSet of ["main", "test"]) {
  const javaDir = path.join(root, "backend", "src", sourceSet, "java");
  const from = path.join(javaDir, ...OLD_PACKAGE.split("."));
  const to = path.join(javaDir, ...newPackage.split("."));
  if (!fs.existsSync(from)) continue;
  console.log(`move ${path.relative(root, from)} -> ${path.relative(root, to)}`);
  if (dryRun) continue;
  fs.mkdirSync(path.dirname(to), { recursive: true });
  fs.renameSync(from, to);
  // 비게 된 상위 폴더 정리 (com/example)
  let dir = path.dirname(from);
  while (dir !== javaDir && fs.readdirSync(dir).length === 0) {
    fs.rmdirSync(dir);
    dir = path.dirname(dir);
  }
}

console.log(`\n${dryRun ? "[dry-run] " : ""}${changed.length}개 파일 수정`);
for (const f of changed) console.log(`  ${f}`);
console.log(`
다음 단계
  1. git diff 로 변경 확인
  2. cd backend && ./gradlew build   (openapi.json 은 패키지와 무관해서 그대로)
  3. 샘플 item 기능은 참고 후 삭제하거나 실제 도메인으로 교체
  4. 이 스크립트(scripts/init-project.mjs)는 지워도 된다`);

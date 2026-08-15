# BlockDynamics_Mod (blockd)

实体冻结器（Entity Freezer）与繁殖限制（Breeding Limiter）模组，基于 **NeoForge**。

- **实体冻结器方块**：红石通电时冻结周围生物（NoAI + 完全静止，含重力/水流/击退免疫），断电自动解冻。
  - 状态存入方块 BlockState（`POWERED`），多台冻结器互不干扰，拆方块也会解冻。
  - 支持弱信号（红石粉）。
- **繁殖限制**：父代周围 17×17×17 范围内 Mob 数量超过配置上限（`maxMobsInChunk`，默认 10）时阻止产仔。

## 支持版本

一套源码编译多个版本（[Stonecutter](https://stonecutter.kikugie.dev/) + ModDevGradle 2.0.141）：

| Minecraft | NeoForge | Java | Parchment |
|---|---|---|---|
| 1.21.1 | 21.1.248 | 21 | 2024.11.17 |
| 1.21.11 | 21.11.42 | 21 | 2025.12.20 |
| 26.1 | 26.1.0.19-beta | 25 | - |

版本表在 `build.neoforge.gradle.kts` 顶部的 `versionInfo` 中，新增版本只需加一行。

## 构建

```bash
# 一次构建全部版本，产物收集到 build/libs/<mc版本>/
./gradlew buildAndCollect

# 只构建某个版本
./gradlew :1.21.1-neoforge:build

# 跑 gametest（三个版本各有 5 个行为测试）
./gradlew :1.21.1-neoforge:runGameTestServer
./gradlew :1.21.11-neoforge:runGameTestServer
./gradlew :26.1-neoforge:runGameTestServer
```

> 26.1 需要 JDK 25：本地可用 `org.gradle.java.installations.paths`（写入 `~/.gradle/gradle.properties`）或 `-Porg.gradle.java.installations.fromEnv=...` 指定；CI 里已通过 setup-java 安装。

### 版本差异与条件注释

不同版本间的 API 差异用 Stonecutter 的 `//? if` 条件注释写在源码里（例如 `neighborChanged` 的 6/7 参数签名、NBT 读写 API、`isClientSide` 字段/方法、`new ItemStack(this)` 等）。**仓库里的源码始终处于 `stonecutter active` 版本（1.21.1）的状态**：1.21.1 分支的代码保持普通代码，其他版本分支用块注释包住；切版本/编译其他版本时由 Stonecutter 自动重写。

规则：
- 修改源码时保持"当前 active 版本分支为普通代码、其余分支为 `/* ... */` 块注释"的格式，否则 active 版本编译会失败；
- 编译非 active 版本时，Stonecutter 会处理根目录 `src/main` 并生成 `versions/<id>/build/generated/stonecutter` 供该版本编译使用。
- 某些资源（如配方 JSON 的 ingredient 格式在 1.21.11 变了）需要按版本区分时，把覆盖文件放进 `versions/<mc>/src/main/resources/...`，Stonecutter 会把它合并进该版本的构建（active 版本用根目录文件）。
- **注意：被 `//? if` 门控包裹的源码区域里不能出现块注释（`/* */`，javadoc 也算），否则 stitcher 解析会报 Unclosed scope。**
- gametest 分两套：`BlockDynamicsGameTests`（1.21.1 旧框架，`<1.21.11` 门控）和 `BlockDynamicsGameTestsModern` + `GameTestRegistrarModern`（1.21.11+ 新 registry 框架，`>=1.21.11` 门控）；测试内容一致。

## 分支策略（旧版废弃）

旧仓库按"一个版本一个分支"维护（`1.20.1` / `1.21.1` / `1.21.11` / `26.1` / `master`），现已改为 **Stonecutter 多版本单分支**（`multiversion`，功能基线 = 原 `1.21.1` 分支）。旧分支保留在远端供参考，不再维护。

## 自动发布

- `.github/workflows/build.yml`：push/PR 全版本构建。
- `.github/workflows/alpha.yml`：push 到 `multiversion` 时以 `<mod_version>-alpha.<commit7>` 发布全部版本的 alpha 到 [Modrinth](https://modrinth.com/mod/blockdynamics_mod)。

## 配置

`blockd-common.toml`（首次启动生成）：
- `maxMobsInChunk`：区块内生物数量上限（繁殖限制），默认 10。
- `givetagBlockRange`：冻结器作用半径，默认 8。
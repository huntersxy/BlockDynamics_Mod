# 任务接手：BlockDynamics_Mod 多版本化迁移（Stonecutter）—— 进度更新

> 本文件是迁移任务的交接文档。上方为原始任务描述与背景；下方为本轮接手后的进展记录。

## 原始任务
把 D:\CODE\minecraft_mod\BlockDynamics_Mod 从"一个版本一个分支"改成**一套源码编译多个 NeoForge 版本**：
- 目标版本：**1.21.1 / 1.21.11 / 26.1**（放弃 1.20.1 的 Forge 分支）
- 功能基线：**origin/1.21.1 分支**
- 工具：**Stonecutter 0.9.7 + ModDevGradle 2.0.141 + Kotlin DSL**

## 核心问题已解决（原阻塞点）

**"active 版本编译根目录未处理 src、报 ValueOutput/Orientation 找不到符号"的根因**：
Stonecutter 0.9.7 的机制是——
- **active 版本子项目直接编译根目录 `src/main` 的原始文件**（不经过 stonecutterGenerate 输出）；
- **非 active 版本子项目编译 `versions/<id>/build/generated/stonecutter`**（由 stonecutterPrepare → stonecutterGenerate 产出，builtBy 自动接线）。

因此**仓库里的根 src 必须始终处于 active 版本（1.21.1）的状态**：1.21.1 分支代码保持普通代码，其余版本分支用 `/* ... */` 块注释包住（`//? if` 标记行本身保留）。上一手把 1.21.11/26.1 分支也写成了普通代码，导致 active 编译看到全部分支的并集而报错。

修复方式：把 `stonecutterGenerate` 对 active 版本生成的（正确的 1.21.1 状态）产物拷回根 src（`Givetagblock.java`、`MixinLivingEntity.java`），其余文件本就是同一状态。

**验证结论**：`:1.21.1-neoforge:compileJava` ✅ BUILD SUCCESSFUL。

## 1.21.11/26.1 的 API 差异（已确认并修复）

| 差异 | 1.21.1 | 1.21.11+ |
|---|---|---|
| `Block.onRemove` | 有（`Level`） | **删除**，改为 `affectNeighborsAfterRemoval(BlockState, ServerLevel, BlockPos, boolean)`（仅在方块被其他方块替换时触发，语义等价旧 onRemove + !newState.is(this)） |
| `Cow`/`Chicken` 包 | `world.entity.animal` | `world.entity.animal.cow.Cow` / `world.entity.animal.chicken.Chicken` |
| `EventBusSubscriber.bus()` | 有（21.1.x 必需，248 起 deprecated） | **已删除**，不能写 bus= |
| gametest 旧 API（`@GameTest`/`@GameTestHolder`/`@PrefixGameTestTemplate`/`GameTestHelper.spawn`+sequence） | 可用 | **整个框架被 Mojang 重写**（registry 化：`Registries.TEST_FUNCTION` + `GameTestInstance`/`TestData`/`TestEnvironmentDefinition`） |
| `ResourceLocation` | 有 | 改名 `Identifier`（本项目代码未直接使用） |

**已采用的策略**：
- `Givetagblock.java`：`onRemove` 与 `affectNeighborsAfterRemoval` 按版本条件编译（`<1.21.11` 用旧、`>=1.21.11` 用新）。
- `GameTestRegistrar.java` 与 `BlockDynamicsGameTests.java`：整体 `//? if <1.21.11 {` 门控，**gametest 只在 1.21.1 编译/运行**；1.21.11/26.1 的新框架移植列为后续工作（工作量较大，且 26.1 可能又有差异）。
- 其余（Config/blockdClient 无 bus 参数、NBT 读写、travel、isClientSide、ItemStack(this)）沿用上一手的条件注释，编译验证中。

## 环境补充
- 26.1 需要 JDK 25：foojay 自动下载失败（其 redirect 端点连不上），已手动下载 Temurin 25.0.4 解压到 `C:/Users/xiey/.jdks/jdk-25.0.4+7`，并在 `~/.gradle/gradle.properties` 写 `org.gradle.java.installations.paths`（含 21 与 25 两个 JDK）。**该文件在用户目录，不入库**。
- CI（`.github/workflows/build.yml`/`alpha.yml`）已按多版本布局重写：JDK 21 + JDK 25 双 setup，`ORG_GRADLE_PROJECT_org.gradle.java.installations.fromEnv=JDK21,JDK25` 传入工具链路径；`buildAndCollect` 收集 `build/libs/<mc版本>/blockd-<ver>+<mc>.jar`；alpha 工作流对每个游戏版本各发一个 Modrinth 版本（game_versions 按 jar 区分）。触发分支为 `multiversion`（改名需同步）。
- `build.neoforge.gradle.kts`：`version = mod_version + "+" + mcVersion`（jar 名带版本后缀，三版本可区分）；新增 `buildAndCollect` Copy 任务。

## ✅ 新增版本支持：26.1.2 / 26.2

- 版本：26.1.2 = NeoForge 26.1.2.95（正式版）、26.2 = NeoForge 26.2.0.59（正式版），
  版本号取自官方 MDK（NeoForgeMDKs/MDK-26.1.2-ModDevGradle、MDK-26.2-ModDevGradle）。
- API 差异：
  - 26.1.2+ 移除了 DeferredRegister.Blocks 三参 registerBlock(String, Function, Properties)
    重载 → 改用 Supplier<Properties> 版本（\`//? if <26.1.2\` 条件）。
  - 26.2 把实体类型常量从 EntityType.COW 移到新类 EntityTypes.COW（Block/Blocks 同风格）
    → gametest 改用 BuiltInRegistries.ENTITY_TYPE 按 id 查询（所有 21.11+ 通用，避免嵌套标记）。
  - 注意：版本门控区内不能嵌套 \`//?\` 标记（外层门控内再放 if/else 会 Unclosed scope），
    跨版本差异要么拆独立文件、要么用运行时查询/raw type 桥接。
- 修正 pack_format：1.21.11=75、26.1/26.1.2=84、26.2=88（此前 1.21.11=15、26.1=34 是错的，
  不匹配只打日志不影响加载，但已修正）；processResources 的 expand 值不参与增量检查，
  已显式 inputs.property 声明，改版本表会自动重跑。
- 配方覆盖：versions/26.1.2-neoforge、versions/26.2-neoforge 各加字符串格式配方。
- 验证：5 版本编译全过；gametest 1.21.1 5/5，1.21.11/26.1/26.1.2/26.2 各 6/6。
- alpha workflow 的 VERSIONS 列表已加 26.1.2/26.2（发布 5 个 alpha，旧 alpha 自动清理）。

## ✅ 完成状态（本接手轮次全部完成）

- 三个版本全部编译通过 + jar 打包（`buildAndCollect` → `build/libs/<mc>/blockd-<ver>+<mc>.jar`）
- 1.21.1 gametest 5/5 通过（`runGameTestServer`）
- 1.21.11 / 26.1 服务器启动验证：mod 正常加载、mixin 全部应用成功、正常关服
- **运行时发现的额外修复**（编译期无法发现）：
  - 1.21.11+ 方块注册必须用 `BLOCKS.registerBlock(...)`（旧 `register(Supplier)` 不调用
    `BlockBehaviour.Properties.setId`，运行时抛 "Block id not set" / "Trying to access unbound value"）
  - 该问题在本地 21.11/26.1 服务器启动时复现并修复
- CI：push 到 `multiversion` 后 Build workflow 通过（1m54s），Alpha workflow 已向 Modrinth
  发布 3 个 alpha（1.0.8-alpha.17d7b72+1.21.1 / +1.21.11 / +26.1，game_versions 各自对应）
- 已提交 commit 17d7b72 并推送到 origin/multiversion

## ✅ gametest 已移植（后续轮次完成）

1.21.11/26.1 的新框架移植完成并全部通过：
- 新框架机制：测试函数（Consumer<GameTestHelper>）注册进 TEST_FUNCTION 内置注册表
  （RegisterEvent 阶段）；测试实例（FunctionGameTestInstance + TestData）注册进 TEST_INSTANCE
  数据包注册表（RegisterGameTestsEvent 阶段，环境自建 blockd:default 空环境）。
- 新增 `BlockDynamicsGameTestsModern.java` 与 `GameTestRegistrarModern.java`（`//? if >=1.21.11` 门控）。
- 21.11 与 26.1 的差异（TestEnvironmentDefinition 泛型化）用 raw type 桥接，无版本标记。
- 踩坑：门控区域内不能有块注释（javadoc 也算），否则 stitcher 报 "Unclosed scope"；
  `//?` 门控区注释里的 `/* */` 文本同样会触发。
- 顺手修复：recipe JSON 的 ingredient 格式 1.21.11+ 改为字符串形式（`"minecraft:egg"`），
  用 versions/<mc>/src/main/resources 覆盖文件实现（1.21.1 保持 {"item":...}）。
- 验证：三个版本 runGameTestServer 全部通过（1.21.1 5/5；1.21.11、26.1 各 5+1=6/6 含 vanilla always_pass）。
- actions v4 → v5（CI 里有 Node20 弃用警告，不影响运行）
- 决定正式分支策略：multiversion 合并/改名为 main 后，alpha workflow 的 branches 列表需同步

## 待办（历史，已全部完成）

- [x] 定位并修复 active 版本编译根因
- [x] 1.21.1 compile 通过
- [ ] 1.21.11 / 26.1 compile 通过（进行中，26.1 首次要下载 NeoForge 26.1.0.19-beta）
- [ ] `buildAndCollect` 全量构建 + 检查三个 jar 内容（mods.toml 展开、pack_format 15/15/34、mixins）
- [ ] 1.21.1 gametest 运行验证（`:1.21.1-neoforge:runGameTestServer`）
- [ ] 验证 1.21.11/26.1 的 `travel(Vec3)`、NBT `ValueInput/ValueOutput` mixin 目标（从对应 sources jar 确认签名）
- [ ] 提交 multiversion 分支（含 README、CI 更新）
- [ ] 后续可选：1.21.11/26.1 新 gametest 框架移植

## 参考
- 模板 rotgruengelb/stonecutter-mod-template（`C:/Users/xiey/AppData/Local/Temp/stonecutter_refs/sc-template`）：CI 用 `buildAndCollect`；根 src 提交状态 = active 版本状态
- HCsCR（0.9.7 同版本独立脚本）：`gradlew assemble` 一次构建全部
- 关键机制证据：stonecutter 0.9.7 jar 中 `StonecutterBuildTasksImpl.configureSource`/extend 字节码（`build/generated/stonecutter` 仅对非 active 项目接线；active 项目直接用 branch（根）src）
# 更新日志（Changelog）

本文件是模组的官方更新日志。CI 发布到 Modrinth 时自动读取 `## [版本号]` 小节作为该版本的平台更新日志；
版本号发生跳变时（如 1.8 → 1.9、1.8.1 → 1.8.2），旧版本号下最新一组 alpha 会被晋级为 beta 通道而不是删除。

## [1.2.1] - 2026-08-27

### 改进

- 冻结器方块纹理更新，并缩放至 1024×1024（2 的幂）以恢复完整 mipmap（原 1254×1254 导致 mip level 降至 1，远距离观感有闪烁/锯齿）。
- 冻结器方块增加金属音效（`SoundType.METAL`）。
- 方块掉落改用标准 loot table（`loot_tables`/`loot_table` 按版本路径），移除手写 `getDrops`，数据包工具可正常识别。

### 配置

- 繁殖限制配置键 `maxMobsInChunk` 更名为 `maxMobsNearby`（原命名暗示按区块计数，实际按父代周围 17×17×17 范围统计）。注意：已有配置文件中的旧键会失效回退默认值，需更新 `blockd-common.toml` 或删除后重新生成。

### 测试 / CI

- `.github/workflows/build.yml` 新增 gametest 矩阵 job：push/PR 时对全部 4 个版本分别跑 `runGameTestServer`（9 个行为测试），行为回归不再依赖本地手动验证。

## [1.2.0] - 2026-08-23

### 修复

- **重叠冻结器误解冻**：范围重叠的多台冻结器共同冻结同一生物时，其中一台断电会错误解冻其他冻结器仍在冻结的生物。冻结改为**引用计数**，全部断电/拆除后才真正解冻（新增重叠场景 gametest）。
- **原生 NoAI 生物被误唤醒**：解冻不再无条件恢复 AI，改为冻结时记录原始 NoAI 状态、解冻时原样恢复——地图/刷怪笼生成的 NoAI 生物不会被唤醒，其他模组设置的 NoAI 也不会被清掉（快照随 NBT 持久化，新增 gametest）。

### 改进

- 冻结状态只在冻结中写入实体 NBT（`blockd_freeze_count`、`blockd_freeze_was_noai`），不再给所有生物的存档添加多余字段；兼容读取旧版 `blockd_freeze_ai` 字段。
- 方块掉落不再白跑一次 loot table 查询；繁殖限制日志降为 debug 级别，大型养殖场不再刷屏。
- 命名清理：`Givetagblock` → `EntityFreezerBlock`、`limit_breeding` → `BreedingLimiter`、`givetag/cleantag` → `freeze/unfreeze`（注册表 id 与存档格式不变，完全兼容旧存档）。
- 事件监听统一改用 `@EventBusSubscriber` 自动注册；清理模组模板残留代码与已废弃 API 用法。

### 测试 / CI

- 每个版本的行为测试 7 → 9 个（新增：重叠冻结器引用计数、原生 NoAI 保持）。
- 平台更新日志改由本文件提供；版本号跳变时旧版本号下的 alpha 晋级为 beta（不再直接删除）。

## [1.1.0] - 2026-08-15

- 解冻瞬间速度矢量清零，修复"解冻弹飞"（挤压/爆炸冲量残留结算）。
- 新增配方合成、解冻速度清零两个 gametest。
- `pack.mcmeta` 增加 `min_format`/`max_format`（1.21.11+ pack format 规范）。
- 版本线调整为 1.21.1 / 1.21.11 / 26.1.2 / 26.2（移除 26.1）。

## [1.0.8] - 2026-08-15

- 迁移到 Stonecutter 多版本单分支构建（1.21.1 / 1.21.11 / 26.1）。
- gametest 移植到 1.21.11+ registry 框架；接入 Modrinth alpha CI。

## [1.0.6] - 2025-11-09

- 修复动物冻结后 AI 不恢复的问题。

## [1.0.5] - 2025-11-09

- 换用 Mixin 实现，修复繁殖限制不生效的问题。

## [1.0.4] - 2025-11

- 换用 Mixin 方法替代 tag 检测实现冻结。

# BlockDynamics 模组说明

## 概述
BlockDynamics 是一个基于 Minecraft Forge 的模组。
当前功能：

- 通过监测区块内生物数量来控制生物繁殖，避免特定区域内生物过度密集，提升游戏体验。
- 新增一个方块-实体冻结器，用于通过红石控制冻结指定区域内的生物。

## 获取方法

可自行编译或者通过[Release](https://gihub.com/huntersxy/BlockDynamics_Mod/releases) 页面下载。你也可以从[Github action](https://github.com/huntersxy/BlockDynamics_Mod/actions)中下载最新构建。

## 核心功能

### 繁殖限制
- **监测范围**：以生物父母所在位置为中心的 8x8x8 区域（从坐标 X-8, Y-8, Z-8 到 X+8, Y+8, Z+8）
- **繁殖限制**：当区域内 `Mob` 类实体数量超过配置的maxMobsInChunk值时，繁殖行为将被取消
### 实体冻结器
- **监测范围**：由配置文件设定半径，默认半径为 8
- **冻结条件**：受到充能时，实体将冻结。取消充能后，实体解冻。

注：当前该方块没有创造物品栏，请使用give 命令获取，方块id为 givetag_block .

## 安装指南

### 玩家安装步骤
1. 确保已安装对应版本的 Minecraft Forge（支持 1.20.1 版本）
2. 将模组的 JAR 文件放入 Minecraft 游戏目录下的 `mods` 文件夹
3. 启动游戏即可加载模组

## 配置说明

### 配置文件位置
```
config/blockd-common.toml
```

## 许可证

本模组采用 MIT 许可证 - 详情请参见 [LICENSE](LICENSE.txt) 文件。

本模组使用了 Minecraft Forge，其采用 LGPL 2.1 许可证。更多信息请参见 [Forge 的许可证](https://github.com/MinecraftForge/MinecraftForge/blob/main/LICENSE.txt)。
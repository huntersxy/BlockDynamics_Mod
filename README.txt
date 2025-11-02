# BlockDynamics 模组说明

## 概述
BlockDynamics 是一个基于 Minecraft Forge 的模组，主要功能是通过监测区块内生物数量来控制生物繁殖，避免特定区域内生物过度密集，提升游戏体验。

## 核心功能

### 繁殖限制机制
- **监测范围**：以生物父母所在位置为中心的 8x8x8 区域（从坐标 X-8, Y-8, Z-8 到 X+8, Y+8, Z+8）
- **繁殖限制**：当区域内 `Mob` 类实体数量超过配置的 [maxMobsInChunk](file://E:\mcmod\forge-1.20.1-47.4.0-mdk\src\main\java\com\huntersxy\blockd\Config.java#L25-L25) 值时，繁殖行为将被取消
- **日志记录**：阻止繁殖行为时会在日志中记录相关信息

## 安装指南

### 玩家安装步骤
1. 确保已安装对应版本的 Minecraft Forge（支持 1.20.1 版本）
2. 将模组的 JAR 文件放入 Minecraft 游戏目录下的 `mods` 文件夹
3. 启动游戏即可加载模组

### 开发者环境设置
1. 克隆或下载模组源代码到本地
2. 根据使用的 IDE 选择对应步骤：

#### Eclipse 配置
- 在命令行中运行 `./gradlew genEclipseRuns`
- 打开 Eclipse，通过 Import > Existing Gradle Project 导入项目
- 或运行 `gradlew eclipse` 生成项目文件

#### IntelliJ IDEA 配置
- 打开 IDEA，导入项目并选择 [build.gradle](file://E:\mcmod\forge-1.20.1-47.4.0-mdk\build.gradle) 文件
- 运行命令 `./gradlew genIntellijRuns`
- 如需刷新依赖，在 IDEA 中刷新 Gradle 项目

#### 常用维护命令
- `gradlew --refresh-dependencies`：刷新本地依赖缓存
- `gradlew clean`：重置项目（不会影响你的代码）

## 配置说明

### 配置文件位置
```
config/blockd-common.toml
```


### 配置方法
1. 启动一次游戏生成配置文件
2. 编辑配置文件中的 [maxMobsInChunk](file://E:\mcmod\forge-1.20.1-47.4.0-mdk\src\main\java\com\huntersxy\blockd\Config.java#L25-L25) 值，设置允许的最大生物数量
3. 保存文件并重启游戏，配置将生效
## English

An experimental client-side connected-texture compatibility mod for Minecraft 1.21.1 / NeoForge.

### Features and requirements

- Connected-texture compatibility for Yuushya materials rendered by LittleTiles, with automatic Fusion or NeoContinuity backend selection.
- LittleTiles is the only required mod declared by this mod, with no version restriction. LittleTiles' own dependency requirements still apply.
- Fusion / NeoContinuity are optional backends, with no version restrictions. Fusion takes priority when both are installed.
- Requires Minecraft 1.21.1 / NeoForge on the client.

### Validation

Compiled against official LittleTiles pre232, CreativeCore 2.13.50, NeoContinuity 3.0.0+0.0.1 and Sodium 0.8.13. All 18 interface checks, 14 backend-selection assertions, 49 Fusion assertions and 6 native Sodium mesh regression cases passed. Full client startup, Mixin injection and in-game visuals have not been validated. Unrestricted dependency versions do not mean every version has been tested.

### Installation

Place the attached JAR in the client's `mods` folder. Enable the Yuushya resource pack for your selected backend and fully restart the game.

SHA-256: `E131EA794066B468FF8985AEFA0961687C8677E6E171AD70DAAA45AF710A19FA`.

---

## 简体中文

Minecraft 1.21.1 / NeoForge 客户端实验性连接材质兼容模组。

### 功能与依赖

- 为 LittleTiles 渲染的方块小镇材质提供连接材质兼容，自动选择 Fusion 或 NeoContinuity 后端。
- 本模组只声明 LittleTiles 为必需模组前置，版本不限；LittleTiles 自身的前置仍需安装。
- Fusion / NeoContinuity 为可选后端，版本不限；同时安装时优先 Fusion。
- 适用于 Minecraft 1.21.1 / NeoForge 客户端。

### 验证

使用官方 LittleTiles pre232、CreativeCore 2.13.50、NeoContinuity 3.0.0+0.0.1、Sodium 0.8.13 编译，18 项接口检查、14 项后端选择断言、49 项 Fusion 断言和 6 组原生 Sodium 网格检查全部通过。未完成完整客户端、Mixin 启动或游戏画面验收；版本不限不表示所有版本均已验证。

### 安装

将下方 JAR 放入客户端 `mods` 目录。按所选后端启用方块小镇对应资源包，然后完全重启。

SHA-256：`E131EA794066B468FF8985AEFA0961687C8677E6E171AD70DAAA45AF710A19FA`。

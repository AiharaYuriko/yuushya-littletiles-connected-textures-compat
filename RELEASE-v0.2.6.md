## English

Client-side connected-texture compatibility for Minecraft 1.21.1 / NeoForge, with automatic Fusion or NeoContinuity backend selection.

### v0.2.6

Both Fusion and NeoContinuity compatibility apply to materials from any mod, with no source-mod namespace or resource-pack restrictions. Fusion provides neighbor material lookup repair, texture-quad cache invalidation and unculled model-face collection. NeoContinuity bridges connected-texture rendering into LittleTiles' quad clipping pipeline.

LittleTiles is the only required mod declared by this mod, with no version restriction; its own dependencies still apply. Fusion and NeoContinuity are optional and have no version restrictions.

### Validation and installation

Compilation, 18 target-interface checks, 14 backend-selection assertions, 49 Fusion assertions and 6 native Sodium mesh cases passed against LittleTiles pre232 / CreativeCore 2.13.50, NeoContinuity 3.0.0+0.0.1 and Sodium 0.8.13. Full game startup, in-game visuals and the performance impact of the broader material scope have not been verified.

Place the JAR in the client's `mods` folder and enable the resource packs required by your materials and backend. Fully restart the game. When updating an existing installation, keep only the new version of this mod.

SHA-256: `E55FBBEF713FD32F1E16FCAA0F223BE02AFBD08E1DE9B7A28C1ADDFFE59692CD`.

---

## 简体中文

Minecraft 1.21.1 / NeoForge 客户端连接材质兼容模组，自动选择 Fusion 或 NeoContinuity 后端。

### v0.2.6

Fusion 和 NeoContinuity 兼容均适用于任意模组的材质，不限制方块所属模组命名空间或材质包来源。Fusion 提供邻居材质查询修复、纹理面缓存刷新和非剔除面补取；NeoContinuity 将连接材质渲染接入 LittleTiles 的面裁剪流程。

本模组只声明 LittleTiles 为必需模组前置，版本不限；LittleTiles 自身的依赖仍需安装。Fusion 和 NeoContinuity 为可选后端，版本不限。

### 验证与安装

针对 LittleTiles pre232 / CreativeCore 2.13.50、NeoContinuity 3.0.0+0.0.1 和 Sodium 0.8.13，编译、18 项目标接口检查、14 项后端选择断言、49 项 Fusion 断言和 6 组 Sodium 原生网格用例通过。完整游戏启动、游戏画面及扩大材质范围后的性能影响尚未验证。

将 JAR 放入客户端 `mods` 目录，按材质及后端启用所需资源包，完全重启游戏。已有本模组的实例更新时只保留新版。

SHA-256：`E55FBBEF713FD32F1E16FCAA0F223BE02AFBD08E1DE9B7A28C1ADDFFE59692CD`。

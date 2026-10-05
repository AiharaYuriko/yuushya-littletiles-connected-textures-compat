Minecraft 1.21.1 / NeoForge 客户端实验性连接材质兼容补丁。

### 本次调整

- 模组必需前置只保留 LittleTiles，版本不限。
- 移除本补丁对 CreativeCore、方块小镇的直接 required 声明；LittleTiles 本身的依赖要求仍保留。
- Fusion / NeoContinuity 为可选后端，取消版本限制；同时安装时优先 Fusion。
- 保留 MC 1.21.1 / NeoForge 平台要求、现有渲染实现及旧独立补丁互斥规则。

### 验证

使用官方 LittleTiles pre232、CreativeCore 2.13.50、NeoContinuity 3.0.0+0.0.1、Sodium 0.8.13 重新编译，18 项接口检查、14 项后端选择断言、49 项 Fusion 断言和 6 组原生 Sodium 网格检查全部通过。未完成完整客户端、Mixin 启动或游戏画面验收；放宽依赖不表示所有版本均已验证。

### 安装

移除旧统一版及独立 CTM/Fusion 兼容补丁，将下方 JAR 放入客户端 mods。按所选后端启用方块小镇对应资源包，然后完全重启。

SHA-256：`E131EA794066B468FF8985AEFA0961687C8677E6E171AD70DAAA45AF710A19FA`。

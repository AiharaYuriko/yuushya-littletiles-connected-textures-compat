# Yuushya LittleTiles Connected Textures Compat

[English](README.md) | **简体中文**

Minecraft **1.21.1 / NeoForge** 客户端连接纹理兼容补丁，当前版本 **0.2.17-experimental**。

从 [GitHub Releases](https://github.com/AiharaYuriko/yuushya-littletiles-connected-textures-compat/releases) 下载 JAR，替换旧统一补丁后重启客户端。不要同时安装旧的独立 CTM/Fusion 兼容补丁。

需要 LittleTiles 及其自身依赖。Fusion 和 NeoContinuity 可选，同时安装时优先 Fusion，两者均未安装时不启用兼容挂点。按材料启用对应资源包：Fusion 使用 Yuushya Fusion Combine，NeoContinuity 使用 Yuushya Mcpatcher Feature。自动选择后端不会自动切换资源包。元数据不限制依赖版本，并不代表所有版本都已验证。

## 当前功能

双后端连接纹理不按材料来源命名空间过滤。Yuushya 柱子/横梁的模型选型是独立适配，仅处理受支持材料：以相邻方块位置是否存在同材料决定连接，横梁还检查朝向。临时渲染状态不改存档，编辑和加载时刷新相关缓存。

支持模型缺失的轴向端面使用同材料、同朝向的 none 模型补足。LT 要隐藏相邻普通方块的面时，必须用实际边界矩形并集证明完整覆盖；有缺口或无法证明时保留普通方块面。

0.2.17 默认关闭重复诊断，完整覆盖时提前返回，减少覆盖算法的分配与重复排序。排查时使用 JVM 参数 `-Dyuushya.lt.diagnostics=true`。

## 验证和限制

离线编译通过：148 后端、8 取面、28 材料、45 渲染入口/边界、26 安装目标接口、49 Fusion 断言及 6 组 Sodium emitter 检查。独立覆盖校验通过 4 万次。用户已报告 0.2.16 修复有效，0.2.17 优化仍待游戏内回归。

独立基准中 256 条细条约由 104 降至 2.6 微秒，分配约由 39 降至 10 KB；部分小型规则网格略慢。这不是游戏 FPS 测试。离线检查不能覆盖完整启动、实际 Mixin 和所有资源包组合。详见 [VALIDATION.md](VALIDATION.md)、[changelog.txt](changelog.txt) 和 [benchmarks](benchmarks)。

## 构建

需要 JDK 21。独立 Gradle 项目默认编译基线是 NeoForge 21.1.233、LittleTiles pre232 / CreativeCore 2.13.50。可用 `-PcompatCreativeCoreJar=... -PcompatLittleTilesJar=...` 指定目标 JAR。

```powershell
.\gradlew.bat build
```

发布产物采用已验证的离线编译路径，独立 Gradle 命令未作为本次发布验证：

```powershell
.\build-offline.ps1 -ClasspathFile 'development-classpath.txt' -JdkPath 'C:\path\to\jdk-21'
```

classpath 文件每行一个依赖 JAR 的绝对路径，需包含 NeoForge/Minecraft 开发类、LittleTiles、CreativeCore、NeoContinuity、Sodium 内层实现与 FRAPI、Mixin、ASM 及其他开发依赖，排除平台 native 库。`exportClasspath` 提供基础列表，再添加 NeoContinuity/Sodium 内层依赖。测试单独编译，不打包进发布 JAR。安装目标 ABI 审计还需显式传入目标 JAR。

## 许可

LGPL-3.0-only，见 [LICENSE](LICENSE) 和 [COPYING](COPYING)。不捆绑第三方模组源码和依赖 JAR。

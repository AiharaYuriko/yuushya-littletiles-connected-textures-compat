# Yuushya LittleTiles Connected Textures Compat

[English](README.md) | **简体中文**

Minecraft **1.21.1 / NeoForge** 客户端连接纹理兼容补丁，当前版本 **0.2.17-experimental**。

从 [GitHub Releases](https://github.com/AiharaYuriko/yuushya-littletiles-connected-textures-compat/releases) 下载 JAR，放入客户端的 `mods` 文件夹后重启。更新时替换上一版本。

需要 LittleTiles 及其自身依赖。Fusion 和 NeoContinuity 可选。同时安装时进入 `BOTH` 模式，两个后端同时启用，由共用的取面桥接协调渲染挂点；只安装一个时启用对应后端，两者均未安装时不启用兼容挂点。按材料启用对应资源包：Fusion 使用 Yuushya Fusion Combine，NeoContinuity 使用 Yuushya Mcpatcher Feature。后端检测不会自动切换资源包。元数据不限制依赖版本，并不代表所有版本都已验证。

## 当前功能

双后端连接纹理不按材料来源命名空间过滤。Yuushya 柱子/横梁的模型选型是独立适配，仅处理受支持材料：以相邻方块位置是否存在同材料决定连接，横梁还检查朝向。临时渲染状态不改存档，编辑和加载时刷新相关缓存。

支持模型缺失的轴向端面使用同材料、同朝向的 none 模型补足。LT 要隐藏相邻普通方块的面时，必须用实际边界矩形并集证明完整覆盖；有缺口或无法证明时保留普通方块面。

0.2.17 默认关闭重复诊断，完整覆盖时提前返回，减少覆盖算法的分配与重复排序。排查时使用 JVM 参数 `-Dyuushya.lt.diagnostics=true`。

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

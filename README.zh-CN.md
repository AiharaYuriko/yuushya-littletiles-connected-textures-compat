# Yuushya LittleTiles Connected Textures Compat

[English](README.md) | **简体中文**

Minecraft **1.21.1 / NeoForge** 客户端连接材质兼容模组。为 LittleTiles 渲染的方块小镇材质自动选择 Fusion 或 NeoContinuity 后端。

## 下载与安装

从 [GitHub Releases](https://github.com/AiharaYuriko/yuushya-littletiles-connected-textures-compat/releases) 下载 JAR，放入客户端 `mods` 目录。

- 本模组只声明 LittleTiles 为必需模组前置，版本不限；LittleTiles 自身的前置仍需安装。
- Fusion 和 NeoContinuity 均可选，版本不限。两者同时安装时优先 Fusion；均未安装时不启用后端模组。
- Minecraft 1.21.1 / NeoForge 平台要求保留。模组仅处理 `yuushya` 材质。
- Fusion 模式启用方块小镇的 Yuushya Fusion Combine 资源包；NeoContinuity 模式启用 Yuushya Mcpatcher Feature。自动后端选择不会自动切换资源包。
- 完全重启后查看日志 `selected backend` / `selected=...`。

## 功能与验证

将 NeoContinuity 的连接材质渲染接入 LittleTiles 的面裁剪流程。Fusion 模式修复邻居变化后的纹理缓存刷新、补取没有指定剔除方向的模型面，并修复通过 LittleTiles 模拟世界查询邻居材质的问题。后端在启动时自动选择。

针对官方 LittleTiles pre232 / CreativeCore 2.13.50、NeoContinuity 3.0.0+0.0.1 和 Sodium 0.8.13，生产代码编译、18 项接口检查、14 项后端选择断言、49 项 Fusion 断言和 6 组 Sodium 原生网格用例通过。开发编译基线为 NeoForge 21.1.233。

这是实验性预发布版本。无版本加载限制不代表所有版本已验证；尚未完成完整游戏启动、Mixin 注入、画面或性能验收。尤其需要检查跨容器连接、邻居编辑刷新、透明/发光层及动画结构过渡。

## 构建

需要 JDK 21。独立 Gradle 工程提供编译依赖配置：

```powershell
.\gradlew.bat build
```

默认使用 pre232 / Core 2.13.50 作为编译基线，不将这些依赖打入 JAR。可通过 `-PcompatCreativeCoreJar=... -PcompatLittleTilesJar=...` 提供实际目标 JAR。Gradle 独立构建入口尚未在本次发布运行；发布文件使用已缓存开发类路径和 javac 生成。

已验证的离线构建入口：

```powershell
.\build-offline.ps1 -ClasspathFile 'development-classpath.txt' -JdkPath 'C:\path\to\jdk-21'
```

类路径文件每行一个依赖 JAR 绝对路径，需要包含 NeoForge/Minecraft 开发类、LittleTiles、CreativeCore、NeoContinuity、Sodium **内层实现 JAR**及其 FRAPI、Mixin、ASM 和其他开发依赖。可先运行 `gradlew.bat exportClasspath` 获取基础开发类路径，再补入 NeoContinuity/Sodium 内层 JAR；勿把平台原生库加入类路径。离线入口分别编译三组测试，测试 RenderType 替身及测试类不进入交付 JAR。

## 源码与许可

本仓库包含统一入口及 CTM/Fusion 桥接实现，未包含 Minecraft、LittleTiles、CreativeCore、Fusion、NeoContinuity 或 Sodium 的源码/发行依赖。许可证为 LGPL-3.0-only，见 `LICENSE` 和 `COPYING`。

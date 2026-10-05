# Yuushya LittleTiles Connected Textures Compat

**English** | [简体中文](README.zh-CN.md)

A client-side connected-texture compatibility mod for Minecraft **1.21.1 / NeoForge**. It automatically selects Fusion or NeoContinuity for materials rendered by LittleTiles, without filtering by the source mod's namespace.

## Download and installation

Download the JAR from [GitHub Releases](https://github.com/AiharaYuriko/yuushya-littletiles-connected-textures-compat/releases) and place it in your client's `mods` folder.

- LittleTiles is the only required mod declared by this mod, with no version restriction. You still need to install LittleTiles' own dependencies.
- Fusion and NeoContinuity are optional, with no version restrictions. Fusion takes priority when both are installed. If neither is installed, no backend compatibility hooks are enabled.
- Requires Minecraft 1.21.1 / NeoForge. Materials from other mods, such as Rechiseled, are eligible for the Fusion fixes as well.
- Enable the resource packs required by your materials and selected backend. For Yuushya materials, use **Yuushya Fusion Combine** with Fusion or **Yuushya Mcpatcher Feature** with NeoContinuity. Automatic backend selection does not switch resource packs.
- Fully restart the game and check the log for `selected backend` / `selected=...`.

## Features and validation

Bridges NeoContinuity's connected-texture rendering into LittleTiles' quad clipping pipeline. For Fusion, it refreshes cached texture quads after neighbor changes, includes model faces that are not assigned a culling direction, and fixes neighbor material queries through LittleTiles' simulated world view. Backend selection happens at startup.

Version 0.2.6 removes the Yuushya-only material filter from all three Fusion fixes. Production code compilation, 18 interface checks, 14 backend-selection assertions, 49 Fusion assertions and 6 native Sodium mesh regression cases passed against official LittleTiles pre232 / CreativeCore 2.13.50, NeoContinuity 3.0.0+0.0.1 and Sodium 0.8.13. The development compilation baseline is NeoForge 21.1.233. Rechiseled's reported issue has not been reproduced or visually verified.

This is an experimental prerelease. Unrestricted dependency versions do not mean all versions have been tested. Full game startup, Mixin injection, visual behavior and performance have not been validated. Cross-container connections, neighbor-edit refreshes, translucent/emissive layers and animated structure transitions especially need in-game testing.

## Building

Requires JDK 21. The standalone Gradle project provides compilation dependency configuration:

```powershell
.\gradlew.bat build
```

The default compilation baseline is LittleTiles pre232 / CreativeCore 2.13.50; dependencies are not bundled in the output JAR. Use `-PcompatCreativeCoreJar=... -PcompatLittleTilesJar=...` to supply actual target JARs. The standalone Gradle entry point was not run for this release; the published artifact was produced using a cached development classpath and javac.

Verified offline build entry point:

```powershell
.\build-offline.ps1 -ClasspathFile 'development-classpath.txt' -JdkPath 'C:\path\to\jdk-21'
```

The classpath file must contain one absolute dependency JAR path per line, including NeoForge/Minecraft development classes, LittleTiles, CreativeCore, NeoContinuity, Sodium's **inner implementation JAR** and its FRAPI, Mixin, ASM and other development dependencies. Run `gradlew.bat exportClasspath` to obtain the base development classpath, then add the NeoContinuity/Sodium inner JARs. Exclude platform native libraries. The offline entry point compiles the three regression suites separately; test classes and RenderType stubs are not included in the release JAR.

## Source and license

This repository contains the unified entry point and CTM/Fusion bridge implementations. It does not contain the source or distributed dependencies of Minecraft, LittleTiles, CreativeCore, Fusion, NeoContinuity or Sodium. Licensed under LGPL-3.0-only; see [LICENSE](LICENSE) and [COPYING](COPYING).

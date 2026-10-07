# Yuushya LittleTiles Connected Textures Compat

**English** | [简体中文](README.zh-CN.md)

Client-side connected-texture compatibility for Minecraft **1.21.1 / NeoForge**, version **0.2.17-experimental**.

Download the JAR from [GitHub Releases](https://github.com/AiharaYuriko/yuushya-littletiles-connected-textures-compat/releases), replace the previous compatibility JAR, and restart the client. Do not install the old separate CTM/Fusion compatibility mods alongside this unified mod.

LittleTiles is required along with its own dependencies. Fusion and NeoContinuity are optional; Fusion takes priority when both are installed. Without either backend, compatibility hooks are disabled. Enable the resource packs your materials need: Yuushya Fusion Combine for Fusion or Yuushya Mcpatcher Feature for NeoContinuity. Automatic backend selection does not switch packs. Dependency versions are unrestricted in metadata, which does not imply compatibility with every version.

## Current behavior

Both backends handle connected textures without source-mod namespace restrictions. Yuushya pillar/beam model selection is a separate adaptation limited to supported Yuushya materials: matching material presence at adjacent block positions determines connection, with beam orientation checks. Rendering uses temporary states without changing saved blocks or tiles. Neighbor updates and loading invalidate relevant rendering caches.

Missing axial faces of supported pillar/beam models can fall back to the matching `none` model. Before LittleTiles hides an ordinary neighbor's face, actual boundary rectangle coverage must prove the face is fully covered. Incomplete or unproven coverage keeps the ordinary face visible.

0.2.17 disables repeated diagnostics by default, returns early for a fully covering box, and reduces allocation and repeated sorting in boundary coverage. Enable diagnostic logging with `-Dyuushya.lt.diagnostics=true`.

## Validation

The published JAR passed offline compilation and 148 backend, 8 quad, 28 material-state, 45 render-view/boundary, 26 installed-interface and 49 Fusion assertions, plus 6 native Sodium emitter cases. An independent coverage oracle passed 40,000 checks. The prior 0.2.16 fixes were reported effective by the user; 0.2.17 still needs in-game regression testing.

A synthetic 256-strip benchmark improved from about 104 to 2.6 microseconds per call and 39 to 10 KB allocated. Some small regular grids became slightly slower. These are algorithm measurements, not game FPS results. Full startup, Mixin behavior and all resource-pack combinations are not covered by offline checks. See [VALIDATION.md](VALIDATION.md), [changelog.txt](changelog.txt), and [benchmarks](benchmarks).

## Building

Requires JDK 21. The standalone Gradle compilation baseline is NeoForge 21.1.233 and LittleTiles pre232 / CreativeCore 2.13.50. Use `-PcompatCreativeCoreJar=... -PcompatLittleTilesJar=...` to supply target JARs.

```powershell
.\gradlew.bat build
```

The published artifact uses the verified offline compilation path rather than the standalone Gradle command:

```powershell
.\build-offline.ps1 -ClasspathFile 'development-classpath.txt' -JdkPath 'C:\path\to\jdk-21'
```

The classpath file contains one absolute dependency JAR path per line, including NeoForge/Minecraft development classes, LittleTiles, CreativeCore, NeoContinuity, Sodium's inner implementation JAR and FRAPI, Mixin, ASM and supporting development dependencies. Exclude platform native libraries. `gradlew.bat exportClasspath` provides the base classpath; add the required NeoContinuity/Sodium inner JARs. Tests compile separately and are excluded from the release JAR. The installed-target ABI audit additionally requires explicit target JAR arguments.

## License

LGPL-3.0-only; see [LICENSE](LICENSE) and [COPYING](COPYING). Third-party mod sources and dependency JARs are not bundled.

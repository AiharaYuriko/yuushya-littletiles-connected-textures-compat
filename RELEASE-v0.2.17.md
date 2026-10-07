# 0.2.17-experimental — connection and face fixes with coverage optimization

Minecraft 1.21.1 / NeoForge client-side prerelease.

- Includes changes since 0.2.6: coordinated backend handling, supported Yuushya pillar/beam rendering-state adaptation, neighbor/load refresh, missing axial-face fallback, and ordinary neighbor-face coverage protection.
- 0.2.17 disables repeated diagnostics by default, adds a full-coverage early return, and uses fewer allocations and one reusable sort in boundary coverage. No persistent coverage cache is introduced.
- Synthetic 256-strip coverage: approximately 104 → 2.6 µs/call and 39 → 10 KB allocated. Some small regular grids are slightly slower; this is not an FPS claim.
- Offline compilation/regressions passed: 148 backend, 8 quad, 28 material, 45 render/boundary, 26 ABI, 49 Fusion checks, 6 Sodium cases, and 40,000 independent coverage checks.
- The user reported 0.2.16 fixes effective. The 0.2.17 optimization still needs in-game regression testing; offline results do not establish full startup/Mixin or visual compatibility.

Place the JAR in the client's mods folder and restart; replace the previous version when updating. Install LittleTiles and its dependencies, and enable the resource packs required by your materials. Fusion and NeoContinuity are optional: both installed means both are enabled in BOTH mode through a shared quad bridge; one installed enables that backend; neither installed disables compatibility hooks.

中文版：包含 0.2.6 之后的柱子/横梁连接、加载刷新、缺失端面补全和普通邻面保护。0.2.17 默认关闭重复诊断并优化边界覆盖计算；离线回归和 4 万次覆盖校验通过，游戏内性能及优化回归仍待验证。替换旧统一补丁并重启。

SHA-256 (JAR): `E5EDBEE5D99FA718233FFBBA88A05B2A4B9065CB6F30AA53C7FB835BB1E6563F`

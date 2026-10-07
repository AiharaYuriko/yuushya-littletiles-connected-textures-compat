# 0.2.17-experimental 验证记录

日期：2026-10-07。0.2.16 已获用户实测修复有效，本次优化不改变模型选型和补面路径。

- 默认禁用有全局锁的逐决策诊断，以及邻面恢复原子计数。单个完全覆盖边界的普通盒提前返回；覆盖矩形不依赖持久缓存。
- BoundaryCoverage 改为两个原始边界数组、按较少分段轴扫描、矩形一次排序。CoverageOracle 使用独立 8×8 栅格判定，20,000 个随机/拼合/挖孔样例及其转置共 40,000 项通过；包含超出单元边界、重叠及零面积矩形。
- 同一 JDK/参数/基准程序计时，旧值见 analysis/performance-0.2.16/coverage-results.csv，新值见 analysis/performance-0.2.17/coverage-results.csv。256 细条 103.909→2.611µs，39104→9984B；256 网格 10.154→7.895µs，16368→9984B；64 网格 1.531→1.771µs，3760→2656B；16 网格 0.299→0.330µs，1232→656B。注意小网格耗时回退、分配减少的取舍。未做游戏内 A/B。
- 构建通过：148 后端、8 取面、28 材料、45 渲染入口/边界、26 ABI、49 Fusion 检查及 6 组 Sodium emitter。
- SHA-256：E5EDBEE5D99FA718233FFBBA88A05B2A4B9065CB6F30AA53C7FB835BB1E6563F。
- 审计目录：analysis/compat-unrestricted-audit/build-57327693d4ce4ab4bb1b9224f4902586。
- 未改变用户实例；尚未实机验证优化版本的 Mixin/画面及性能。

# 0.2.16-experimental 验证记录

日期：2026-10-07。

- 用户实测 0.2.15 基本有效，剩余大理石柱轴向连接处缺面，淡灰色混凝土柱正常。
- 直接读取已安装 yuushya-1.21.0-neoforge-2.3.0.jar：chiseled_vertical_marble_middle 仅四侧；light_gray_vertical_compact_middle 继承 template_vertical_block_middle，后者有 up/down。确认两种材料基础模型差异。
- 0.2.13 只在 LittleRenderBox 中补面，无法补完整方块的模型。新增 ColumnModelFaceMixin 在 SimpleBakedModel.getQuads 的空轴向返回值上补 none 端面；限定已有支持列表，none 终止重入，已有方向面和 unculled 同向面不添加。
- 编译通过；148 后端、8 取面、28 材料、45 渲染入口/边界、26 ABI、49 Fusion 检查及 6 组 Sodium emitter。新增 SimpleBakedModel 签名及后端启用检查；不等于实机 Mixin 或视觉验证。
- SHA-256：BD281F3987A887C0E53D0F5232ADCF07187A2174BF9393B8A35B295BEF196400。
- 审计目录：analysis/compat-unrestricted-audit/build-3972f83211c44b6393434b786293108d。
- 未修改用户实例；完整方块自定义模型加载器、复杂资源包路径尚未验证。

# 0.2.15-experimental 验证记录

日期：2026-10-07。

- 用户通过六张操作截图及明确文字确认，白色方块始终为完整方块，裁剪的是其旁边的深色 LT。该证据纠正了此前优先排查 LT 自身缺面的方向。
- 核对 NeoForge Block.shouldRenderFace 和 LT BlockTile.hidesNeighborFace：LT 可通过侧面覆盖缓存要求普通邻居隐藏接触面。此路径和 BERenderManager 内部的 LT 面缓存不同。
- 新增 NeighborFaceMixin，只拦截原方法返回 true 的情况，对真实边界矩形并集做完整覆盖证明；未知几何不用于证明。无直接证据证明旧 sideCache 的具体错误来源，故仍需实机验证是否命中。
- 停用 OcclusionRefreshMixin（配置移除、插件不再启用），没有修改服务器或实例目录。原有连接及 LT 端面补全保留。
- 新增覆盖测试：空面、整面、半面、两半拼合、细缝、中央开孔、重叠拼满、重复覆盖不应误算面积。新增实际 hidesNeighborFace ABI 和后端选择检查。
- 构建通过：148 后端、8 取面、28 材料、40 渲染入口/边界、26 ABI、49 Fusion 检查及 6 组 Sodium emitter。没有实机 Mixin 或性能基准验证。
- SHA-256：779C05F8B1E4F20CE072B0CCC13C96EA9DE634D8862BC5F63F3365980744396A。
- 审计目录：analysis/compat-unrestricted-audit/build-6917b6ceffff4d5390434858a004e79c。

# 0.2.14-experimental 验证记录

日期：2026-10-07。

实测结论更新：用户确认 0.2.14 修复无效。17:14:28 启动的 latest.log 确认加载 0.2.14，17:17 的柱子状态判定正常；全日志未发现 `LT occlusion revalidated` 修正记录。因此该次运行未提供“旧完全遮挡标记发生修正”的证据，不能继续把该假设视为已确认根因。0.2.14 不建议作为已修复版本使用；需获取失败结构或渲染阶段跟踪后再修改。

- 用户补充所有方块都会发生与完整方块相邻的缺面；移开海晶灯、F3+A 均未恢复。尚无导出的结构几何，不能认定完整根因已复现。
- 确认 LittleBox faceCache 可随扩展数据加载，并在 hasOrCreateFaceState 中直接复用；渲染盒缓存失效并不清除此标记。
- 新增 OcclusionRefreshMixin，在 boxCache==null 的 getRenderingBoxes HEAD、LT 原有 synchronized(be) 构建区间内，重新计算缓存的 coveredFully 面。仅更新实际变化的客户端面标记，保留模型/UV/碰撞几何。
- 使用实际安装 LT 类的离线测试证明：已有 covered 标记时 hasOrCreateFaceState 不访问计算器、保留原标记；更新为 uncovered 后不再请求隐藏。此测试证明缓存机制，不证明截图场景由该机制引起。
- 构建通过：148 后端、8 取面、28 材料、27 渲染入口/边界、26 ABI、49 Fusion 检查，6 组 Sodium emitter。未运行实机 Mixin/截图验证及性能基准。
- SHA-256：EDF1326851FD31846F47B06D5BF1716DF2EE2F832264E87ED80F64C79C4DE848。
- 审计目录：analysis/compat-unrestricted-audit/build-bd3dc865da88473590138c9a0284c9af。
- 新的核验有重建成本，尤其大量完全遮挡面的复杂 LT；已有渲染盒不重复核验。未修改用户安装目录或服务器。

# 0.2.13-experimental 验证记录

日期：2026-10-07。

- 用户反馈 0.2.12 连接纹理改善，但出现裁剪空洞。新日志确认 0.2.12；x401,z-253 的 LT 和完整方块两侧均识别匹配材料、输出 middle。
- 直接读取已安装 Yuushya 2.3.0 JAR：chiseled_vertical_marble_none 六面齐全，middle 只有 north/east/south/west，top 另有 up，bottom 另有 down。确认模型省略端面这一缺口，不能据此声称已复现截图全部几何。
- 补面钩子仅针对 LT 受支持材料的空轴向结果，通过 none 模型生成并沿用原 RenderBox 裁剪。重新获取模型数据并创建独立材料视图，不改共享视图或 box.state。none 状态终止重入。
- 编译成功；148 后端、8 取面、28 材料、19 渲染入口/边界、26 ABI、49 Fusion 检查及 6 组 Sodium emitter 通过。新增检查验证 CutFaceMixin 的完整方法描述符匹配实际 Core，及四种后端选择。不是实机 Mixin 或视觉验证。
- JAR SHA-256：23B12FB07EA4681E9698168A880A12FAFB51C9A115D1D6662FB96BE6FCA86BB7。
- 审计目录：analysis/compat-unrestricted-audit/build-d199eb00ca414e4797d73153e4a6f5a3。
- 未改实例模组；普通完整方块的模型省面行为未调整，需实测区分完整方块边界和 LT 裁剪内部缺面。

# 0.2.12-experimental 验证记录

日期：2026-10-07。

- 用户报告 0.2.11 基本无效，仍有单端连接、材质变化及裁剪方法相关差异。完整根因尚未确认。
- 已核对实际 pre233 入口：区块更新调用 loadAdditional，但 chunkUpdate=true 时跳过 updateTiles。旧钩子漏掉此路径；旧刷新代码也没有主动清理邻接 LT 的独立缓存。
- 新增 loadAdditional/setLevel 刷新钩子，延至客户端 Post tick、按世界和位置合并，丢弃旧世界请求。对涉及的受支持 LT 调用 render.queue(true,false,0)，对完整方块请求重绘。
- 增加限量状态判定日志，供实机区分模型状态和裁剪/UV 问题。当前可读实例 latest.log 仍为 16:18 的 Roxy 数据库占用崩溃，未取得用户最新失败样例日志。
- 编译成功，148 项后端、8 项取面、28 项材料、14 项渲染入口/边界、26 项目标 ABI、49 项 Fusion、6 组 Sodium emitter 检查通过。新增检查核对加载入口和 LT 缓存调用；不是实机生命周期或视觉验证。
- 产物 SHA-256：344F6289DD180F42FDAFEA5CA6E2F637773AEC75E318D25684F0F76BC9D02FC0。
- 构建审计：analysis/compat-unrestricted-audit/build-8ef725cb9a284ad0b1445aaef1264dc1。
- 未替换用户实例 JAR，未启动 Minecraft；不同裁剪方法的视觉差异仍待样例验证。

# 0.2.11-experimental 验证记录

日期：2026-10-07。

- 用户反馈 0.2.10 只有一端连接，切开后材质与完整柱对照略有差别。实际日志确认 0.2.10，并记录 LT 大理石块柱 middle→top，邻接完整方块 top→middle。
- 代码核对发现旧实现按各 LittleRenderBox 的端面覆盖独立计算 pos；Yuushya 各 pos 模型使用不同 UV 区域，所以切割形状本身可能导致额外纹理变化。此为确定的算法差异；未独立读取截图结构的全部保存数据，不能声称已逐像素复现两个截图。
- 改为 NeighborMaterials/MaterialNeighbors 提供方块位置材料快照，忽略片段形状和数量；LT 同位置同材料共享一个解析状态，再交给原有模型、取面和裁剪。完整方块一侧使用同样规则。朝向等非 pos 属性仍参与匹配。
- 删除 ContactIndex 及完整端面要求。这一行为有意允许微方块局部间隙处的视觉连接，符合用户以石英柱为对照的目标；整格空气或没有对应材料的容器仍不连接。
- 不改资源图、UV 算法、网格裁剪或存档状态；无法据此保证 Yuushya 原素材不同状态之间像素相同。图像效果与光照仍需实机确认。
- 实际 pre233 / Core 2.13.50 / Sodium 0.8.13 构建退出码 0；148 项模式、8 项合并取面、28 项材料存在性/方向、10 项渲染入口/边界、26 项目标 ABI、49 项 Fusion、6 组 Sodium 网格测试通过。
- 新回归覆盖片段数量不改变材料快照、混合容器识别、材质/朝向区分、未加载与已知空位置区分、快照隔离、删除后重新取样、柱子/横梁方向映射。未运行完整 BlockState/世界或 Mixin 变换测试。
- 产物：`../artifacts/yuushya-lt-connected-textures-compat-0.2.11-mc1.21.1.jar`。
- SHA-256：`084A6AF3EE4366F21AEE3567F7CA10348758D3DFA54EA4AC13A3F0A270F6182D`。
- 最终日志/实际目标哈希：`../analysis/compat-unrestricted-audit/build-d7045b97156e4740bf42aff0ab31239a/`。
- 未替换实例文件、未进行截图画面对照或性能测量。验收重点：切口前后保留柱身花纹，两端与完整方块连续；改变 LT 分割方式不改变同位置纹理选型；删除整个相邻材料后重新出现收边。

## 历史：0.2.10-experimental 验证记录

日期：2026-10-07。

- 用户实测：中间两个 LT 方块之间貌似正常，LT 与上下完整方块仍有收边。当前日志确认加载 0.2.9、BOTH、pre233 原生查询，并有 LT 材料 bottom→middle 的真实日志；不能将此等同于新混合边界已修复。
- 原因边界：0.2.9 仅改 LittleRenderBox.state，普通完整方块的渲染状态未适配。新增 FullBlockStateAdapter，只有沿连接方向存在 LT 容器时才重新计算完整方块渲染状态。
- 新入口：Vanilla RenderChunkRegion.getBlockState；Sodium 0.8.13 LevelSlice.getBlockState(III)。已读取实际 Sodium 字节码，确认网格任务先调用整数坐标入口，再 getBlockModel；BlockPos 重载委托整数入口。输出保存于 `../analysis/compat-pre233-audit/SodiumMeshing-javap.txt` 与 `SodiumLevelSlice-javap.txt`。
- 渲染视图从其自身 getBlockEntity 读取邻居 LT，不通过真实世界创建或加载实体；未就绪实体保留原状态。重入保护防止递归相互选模。视图内缓存与 Sodium copyData/reset 清理配套。
- LT updateTiles(ZZ) 后在客户端主线程请求相邻区块重绘，只对相邻存在支持材料的主客户端世界执行；未修改实际方块状态、NBT 或服务器。
- 完整方块端面必须被同材质、同朝向的标准 LT 矩形完整覆盖；多块拼合允许，部分接触保留收边。斜切和动画子世界跨边界未适配。
- pre233 / Core 2.13.50 / Sodium 0.8.13 实际 JAR 编译退出码 0；148 项后端策略、8 项合并取面、45 项几何/方向、10 项新入口 ABI/边界、26 项目标接口、49 项 Fusion、6 组 Sodium 网格回归全部通过。
- 产物：`../artifacts/yuushya-lt-connected-textures-compat-0.2.10-mc1.21.1.jar`。
- SHA-256：`AC472C4B6E973A70FE09D19E1C6E8774CE0104F7B4F030FA33A36C98881A1B89`。
- 最终日志/目标哈希：`../analysis/compat-unrestricted-audit/build-52032f281db0481cbb064703b474aa93/`。
- 未替换实例文件，未启动完整游戏测试新 Mixin、混合边界画面、重载或性能；新增回归是实际接口和端面几何检查，并非完整 BlockState/世界运行测试。Embeddium 等其他渲染器未验证。
- 验收：原截图的完整-LT-LT-完整链，两端连接、拆除/补回 LT 后完整方块刷新；横梁四朝向、跨区块边界；部分端面覆盖保留收边。新日志 `full-block boundary render state adapted` 仅表示完整方块适配已命中。

## 历史：0.2.9-experimental 验证记录

日期：2026-10-06。

## 归因与实现

- 核对 Yuushya 2.3.0 实际 JAR：大理石块柱、淡灰混凝土柱子 B 使用 pos=top/middle/bottom/none；混凝土横梁 B 使用四个水平 facing 与 pos=left/middle/right/none。源码 ColumnBlock.updateShape/LineBlock.updateShape 负责正常方块的更新，LT RenderingThread 则直接选取 cube.state 对应模型。
- 在统一版增加 MaterialStateMixin，getRenderingBoxes 返回时计算渲染盒状态，确保随后的模拟世界、getBlockModel、getModelData 使用一致的状态。改变仅限 LittleRenderBox.state 与其 quad 缓存，未修改 LittleTile 数据。
- ContactIndex 记录标准矩形面的覆盖并做矩形差集；要求完整覆盖，防止用重叠面积重复计数或跨细缝误连。材料键去除 pos，保留朝向等其他属性。邻接普通同类完整方块可参与。
- 支持范围与保守回退详见 README：只适配已核对资源的三组 ID；斜切、越界渲染盒、未加载邻居不自动推断。部分端面覆盖保持收边，不细分面生成混合连接图案。

## 已通过

- 实际 pre233 / Core 2.13.50 编译，保持 0.2.8 双后端逻辑及 pre233 原生查询检测。
- 116 项后端/挂点选择、8 项合并取面、45 项材料几何/朝向、26 项实际 JAR 接口检查、49 项 Fusion 回归、6 组 Sodium 原生网格回归全部通过。
- 几何用例覆盖六个方向、自身不连接、不同材料、拼合端面、部分覆盖、重叠不重复计数、1/4096 细缝、内部小块、不同 grid、邻居移除后的新索引；方向用例覆盖柱子四态和横梁四个朝向。
- 实际发布资源再次读取确认 property 名称与状态值；接口审计新增 BERenderManager.getRenderingBoxes 的准确描述符。
- 构建退出码 0。产物 `../artifacts/yuushya-lt-connected-textures-compat-0.2.9-mc1.21.1.jar`。
- SHA-256：`13209F86C00684A2B728274A96E4588A411EC592045C50B9C2A4C5DD56BB2E4A`。
- 最终构建日志、目标哈希：`../analysis/compat-unrestricted-audit/build-531fde20f15949639ba7201525d8aa76/`。

## 验证边界

- 用户随后反馈旧补丁似乎没有该问题，故不能把源码中的 pos 更新缺口认定为本次新旧表现差异的确定根因。0.2.9 是针对该缺口的新行为适配，不是已证实的回归还原。
- 追加检查实际发布 JAR：0.2.3～0.2.7 的 Fusion BERenderManagerMixin、RenderBoxMixin、RenderingThreadMixin、BlockTileMixin、UnculledQuadCache 字节一致；NeoContinuity 核心及 MaterialBlockView 在 0.2.3～0.2.9 字节一致。0.2.2 对这几个关键类的 javap -p -c 输出在忽略常量池编号后，与 0.2.3 一致。0.2.6 改材料筛选，0.2.7 改原生查询选择，0.2.8 改双后端合并入口，0.2.9 才加入 pos 适配。
- 更早 CTM 0.1.0 源码 JAR 的 MaterialBlockView 仅在当前位置替换材料状态，并未重算柱子 pos。不能把“旧补丁正常”解释为旧版原有状态适配被删除。
- 已保存的 pre224→pre233 官方比较中，RenderingThread 的选模段没有改动，差异主要为异常重试；BlockTile 查询和遮面等另有改动。若旧测试同时使用旧 LT/Core，不能把差异单独归因于补丁。
- 首次明确异常截图对应日志已为 0.2.7 / pre233 / FUSION，所以 0.2.8 双后端及 0.2.9 状态适配不是此前异常的起点。需在同场景、同资源、同 LT/Core 下比较用户确认正常的旧补丁，才能进一步定位。

- 尝试在独立 JVM 启动 Minecraft 注册表并执行真实 BlockState 测试，因缺少 NeoForge LoadingModList 启动上下文而失败；没有将此项算作通过，也没有用生产环境绕过替代。最终构建保留可执行的几何/方向和接口回归。
- 未进行完整 Mixin 启动、截图对应结构复现、动画结构、资源重载或 FPS 测量；实际保存的异常结构材料状态尚未取得。源码缺口已有针对性实现，但用户画面修复结果仍需实机确认。
- 未改实例文件。替换旧补丁并重启后检查首次 `connected material render state adapted` 日志和原异常柱子/横梁；还应验证编辑邻居后的更新以及预留间隙保留收边。

## 历史：0.2.8-experimental 验证记录

日期：2026-10-06。

- 改为 NONE / FUSION / NEO_CONTINUITY / BOTH，按后端是否存在独立启用。BOTH 仅使用 CombinedRenderBoxMixin 接管两处取面调用，避免两个 Redirect 冲突；其他修复按所属后端启用。
- CTM 成功输出（包括空列表）不被原始方向面替代。非剔除面通过 CTM 处理后分组；未处理模型或桥接异常回退到 Fusion 补面路径。pre233 原生查询检测保留。
- 实际 pre233 / Core 2.13.50 编译通过；后端选择 108 项、合并取面 8 项、目标接口 25 项、Fusion 49 项、Sodium 原生网格 6 组全部通过。合并取面检查直接调用已编译重定向的回退路径，并检查转换结果的方向分组、缓存和空方向防重复；不是完整 NeoContinuity 规则运行测试。
- 构建命令与 0.2.7 相同，传入实际实例 mods 目录及 `-VerifyNativeNeighborLookup`。JAR 内容和依赖检查通过，包含第三份合并 Mixin 配置及对应类；没有加入测试或依赖类。
- 产物：`../artifacts/yuushya-lt-connected-textures-compat-0.2.8-mc1.21.1.jar`。
- SHA-256：`13C1E1A9598E0092DE10C69B7D5ECE4049B1ED3A9E56110013CDA35AB8273AF4`。
- 日志和实际目标哈希：`../analysis/compat-unrestricted-audit/build-31caa1b6bae84da5825d931531b8213e/`。
- 尚未启动完整客户端验证 Mixin 变换、两种资源包规则或 FPS；未替换实例文件。缺失依赖的选择已离线检查，但四套真实模组组合均未启动验收。柱子/横梁状态适配不在本次改动中。
- 两后端安装的实机日志应为 selected=BOTH；pre233 仍应显示 nativeFixed=true / compatibilityRedirect=false。分别用 Fusion 与 NeoContinuity 资源规则验证，检查补面、邻居编辑和透明层。

## 历史：0.2.7-experimental 验证记录

日期：2026-10-06。

- 目标：实际实例 LittleTiles 1.6.0-pre233、CreativeCore 2.13.50、NeoContinuity 3.0.0+0.0.1、Sodium 0.8.13；Minecraft 1.21.1 / NeoForge。编译依赖沿用已有 NeoForge 21.1.233 开发类路径。
- 生产变化仅在统一版 Mixin 选择：识别官方原生邻居查询能力后停用 Fusion 的 BlockTileMixin 和专用 accessor；旧版或识别异常保留。缓存失效、补面、材料范围和 NeoContinuity 桥接实现未修改。
- pre233 原生能力识别为 true；后端及挂点选择 45 项、实际目标 JAR 审计 25 项、Fusion 49 项、Sodium 原生网格 6 组回归全部通过。目标审计包含 Level 强转、非 CHECK 查询和旧方法签名的否定用例。
- 额外读取保存的 pre232 发布 JAR，原生能力识别为 false，目标审计 21 项通过；旧版选择逻辑保持绕行。此项为字节码/选择策略检查，不是旧版游戏启动测试。
- 构建命令：`./build-connected-textures-installed.ps1 -ModsPath 'D:/MC/1.19.2_C/.minecraft/versions/1.21.1-NeoForge_21.1.216/mods' -VerifyNativeNeighborLookup`。
- 构建退出码 0；打包及依赖元数据检查通过，继续保持 LittleTiles/Fusion/Continuity `[0,)`；既有 FRAPI 弃用警告仍存在。
- 产物：`../artifacts/yuushya-lt-connected-textures-compat-0.2.7-mc1.21.1.jar`。
- SHA-256：`CBACD059D53F4744A6236F0B85DDCF2F4D9E66AACA881F366F01CF37D43DE512`。
- 构建日志、目标 SHA-256、pre232 负对照：`../analysis/compat-unrestricted-audit/build-ababad3108604da58c8dacdde012da15/`。
- pre233 目标 SHA-256：`EF4419A7BFB6F071152ECC68D6D770CB1132A155539026F156CB91830FCE5E3C`；pre232 对照 SHA-256：`F062A84702A67AFD0B13D5F2867E8A4C6C1CB924D061766E975D720B3260D05A`。
- 本次未替换实例文件，未运行完整 Mixin 启动、实机画面或性能测试。没有宣称柱子问题修复或达到特定性能提升；这是对原有补丁利用 pre233 修复的适配。
- 实机重点：确认 Fusion 日志 nativeFixed=true / compatibilityRedirect=false，检查相邻 LT、混合材料、邻居编辑、动画结构和柱子原场景；NeoContinuity-only 仍走原桥接。

## 历史：0.2.6-experimental 验证记录

日期：2026-10-06。

- TargetMaterials.includes 仅保留 state != null，移除方块注册表命名空间筛选；三个 Fusion 入口沿用同一筛选，因此均解除方块小镇范围限制。NeoContinuity 路径未改变。
- 使用官方 pre232 / Core 2.13.50 及实际 NeoContinuity 3.0.0+0.0.1、Sodium 0.8.13 构建，18 项接口、14 项后端选择、49 项 Fusion 断言和 6 组网格用例通过；打包及依赖元数据检查通过。
- 产物：`../artifacts/yuushya-lt-connected-textures-compat-0.2.6-mc1.21.1.jar`。
- SHA-256：`E55FBBEF713FD32F1E16FCAA0F223BE02AFBD08E1DE9B7A28C1ADDFFE59692CD`。
- 日志：`../analysis/compat-unrestricted-audit/build-f0d4a4e8bcdf4b928298e9e6b967f934/`。
- 尚未验证 Rechiseled 实机、完整 Mixin 启动或扩大范围后的性能；GitHub issue 回复仍待用户审核，未发送。

## 历史：0.2.5-experimental 验证记录

日期：2026-10-05。

- 依赖调整：模组 required 只保留 LittleTiles `[0,)`；删除本补丁直接 required 的 CreativeCore / Yuushya 声明；Fusion / Continuity optional 均改为 `[0,)`。Minecraft 1.21.1 / NeoForge 平台限制和旧独立补丁 incompatible 声明保留。
- 未修改渲染生产逻辑。用官方 pre232 / Core 2.13.50 及实际 NeoContinuity 3.0.0+0.0.1、Sodium 0.8.13 编译，既有 18 项目标接口、14 项后端选择、49 项 Fusion 断言和 6 组原生网格检查全部通过。
- 最终构建退出码 0，发布 JAR 依赖、版本、入口和内容检查通过；只含 LittleTiles、Minecraft、NeoForge 三项 required。未包含测试或上游类。
- 交付：`../artifacts/yuushya-lt-connected-textures-compat-0.2.5-mc1.21.1.jar`。
- SHA-256：`E131EA794066B468FF8985AEFA0961687C8677E6E171AD70DAAA45AF710A19FA`。
- 日志和目标哈希：`../analysis/compat-unrestricted-audit/build-af8254863e6a4155a9d04133fed492e0/`。
- 初次打包受系统临时目录移动权限影响而失败；构建脚本改用本次工作区构建目录作为 JAR 临时目录后完整重建成功。以最终成功记录为准。
- 未修改游戏实例、未进行完整 Mixin 启动和游戏画面验证；无版本加载限制不等于所有 LittleTiles / 后端版本已经验收。

## 历史：0.2.4-experimental 验证记录

日期：2026-09-30。

- 目标：Minecraft 1.21.1 / NeoForge 21.1.248；开发依赖 NeoForge 21.1.233、JDK 21。
- 使用实例 LittleTiles 1.6.0-pre231 / CreativeCore 2.13.50、NeoContinuity 3.0.0+0.0.1 和 Sodium 0.8.13 内嵌实现/FRAPI 3.4.1 编译。
- 7 个关键挂点类与 pre230 / Core 2.13.49 的实际 JAR 字节完全一致；保留现有修复逻辑，精确锁定新目标。
- 18 项目标接口检查、14 项后端选择断言、49 项 Fusion 断言、6 组 Sodium 原生网格用例全部通过，构建退出码 0。
- 打包检查通过：版本 0.2.4-experimental、Core [2.13.50]、LT [1.6.0-pre231]；包含统一入口及两套 Mixin 配置，不包含测试类、旧独立入口或上游模组。
- 构建脚本支持已有开发依赖的工作区副本，编译类路径排除平台原生库；仍有现有 FRAPI 弃用警告。
- 交付：`../artifacts/yuushya-lt-connected-textures-compat-0.2.4-mc1.21.1.jar`。
- SHA-256：`68CDC079F0289A25E03E0908E860C30C098BD5AF1C1D827905A8896909E84604`；同目录有 `.sha256` 文件。
- 完整目标哈希和测试日志：`../analysis/compat-pre231-audit/build-877b52397e634f9d9243343e0e0ded90/`。
- 源码变化审计及安装说明：`../analysis/compat-pre231-audit/RESULT.md`。
- 未修改实例文件，未运行完整游戏、Mixin 启动、画面或帧时间验收。当前实例 Fusion 禁用，预期 selected=continuity。

## 历史：0.2.3-experimental 验证记录

日期：2026-09-27。

- 目标实例 Minecraft 1.21.1 / NeoForge 21.1.248；开发编译依赖 NeoForge 21.1.233。
- 直接以实例 LittleTiles pre230、CreativeCore 2.13.49、NeoContinuity 3.0.0+0.0.1 与 Sodium 0.8.13 内嵌 FRAPI 3.4.1 编译。
- 固定依赖范围为 LittleTiles `[1.6.0-pre230]`、CreativeCore `[2.13.49]`，未将未经检查的中间版本加入声明。
- CTM RenderBox 重定向锁定完整方法描述符及两个调用入口；其余现有渲染实现可通过目标编译和回归，无需改动。
- 新增目标 JAR 审计：18 项完整签名、字段和调用数量检查。旧的 Fusion 检查此时也加载实际 Core/LittleTiles JAR。
- 后端选择 14 项、Fusion 49 项、Sodium 0.8.13 原生网格 6 组检查通过。
- 构建脚本：`../build-connected-textures-installed.ps1 -ModsPath '实例mods目录'`。使用已缓存开发类路径，javac 编译全部交付源码，测试单独编译，不将依赖或测试打进 JAR。
- 最终交付路径与 SHA-256 见 `../analysis/compat-pre230-audit/RESULT.md`。
- 未启动完整游戏、未进行画面或帧时间验收，未替换实例 JAR；Fusion 1.3.15b 规则执行也未实测。

## 历史：0.2.2-experimental 验证记录

日期：2026-09-17。

- 修复 pre227+ 的 Fusion 邻居查询：`getAppearance` 内的 `loadBE` 改为在受限条件下接收模拟视图的父 `Level`，保留原生实体读取与加载检查。
- 原基线 LittleTiles pre224 / CreativeCore 2.13.43 构建通过，Fusion 49 项断言通过。
- 新增 5 项检查为编译产物的注入接口检查，不是游戏内行为测试。
- 后端选择新增对两个修复 Mixin 的互斥检查，现为 14 项。
- 最新版验证使用隔离源码目录，固定提交和命令见 `../analysis/compat-pre228-audit/FIX.md`。
- LittleTiles pre228 / CreativeCore 2.13.46 源码构建通过；后端选择 14 项、Fusion 49 项检查全部通过。
- 统一版依赖声明更新为 LittleTiles `[1.6.0-pre224,1.6.0-pre228]`、CreativeCore `[2.13.43,2.13.46]`。pre225～227 / Core 2.13.44～45 未在本次逐一重建，不声称每种组合均实测。
- 最终 0.2.2 构建退出码 0；JAR 内版本、依赖范围、新增 Mixin 类和配置已核对，未包含回归测试类或上游依赖类。
- 交付文件：`../artifacts/yuushya-lt-connected-textures-compat-0.2.2-mc1.21.1.jar`，32,051 字节。
- SHA-256：`5997c51b79c70eef2a73dae3585b5ecf139812af52ddaac34cfca0d12955d524`。
- 未运行完整游戏实例。相邻容器连接、邻居变化刷新、动画结构和 Mixin 实际加载尚待实机验收。

## 历史：0.2.1-experimental

日期：2026-08-28。

- 完整构建退出码：0。
- 模式选择：10 项断言通过，覆盖四种安装组合以及 Fusion/NeoContinuity Mixin 互斥。
- Fusion 逻辑：既有 44 项无窗口回归通过。
- JAR 检查：两套配置和实现均存在；只有一个统一 `@Mod` 入口；没有测试类、依赖类或两个旧入口。
- 元数据：Fusion、Continuity 均为 optional；两个旧补丁均为 incompatible。
- 版本范围：CreativeCore `[2.13.43,2.13.44]`、LittleTiles `[1.6.0-pre224,1.6.0-pre226]`。
- 源码差异核对：目标版本间 `RenderBox`、`BERenderManager`、`RenderingThread` 及其相关类型未改变。
- 目标上限构建：使用 CreativeCore 2.13.44（`ed17bbe`）与 LittleTiles 1.6.0-pre226（`1a25a1e8b`）执行干净构建，退出码 0。
- 文件：`build/libs/Yuushya LittleTiles Connected Textures Compat_EXPERIMENTAL_v0.2.1-experimental_mc1.21.1.jar`
- 大小：30,203 字节。
- SHA-256：`30d439c13b9eb259b2d682de67a1bcd4a0bed0095cd13ef9ab35c07e335e4012`。

本次没有运行完整游戏实例，因此尚未验证 Mixin 启动日志、资源包自动配合和实际画面。
首次实机测试应分别运行 Fusion-only、NeoContinuity-only、两者同时安装三种配置，检查日志中的 selected 值。

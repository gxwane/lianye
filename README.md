# ScrollLoom (长卷)

> **Stitch any scrollable screen, entirely offline.**  
> 私密、纯粹、离线。把任意可滚动的屏幕，织成长卷。

---

## 📖 项目简介 (Overview)

**ScrollLoom（长卷）** 是一款面向 Android 平台的现代开源长截屏工具。它专注于解决原生系统与传统长截图工具的核心痛点：无需繁琐的录屏授权弹窗，不声明任何联网权限，纯依靠本地轻量视觉算法，像织布机（Loom）穿引经纬线一样，将连续滚动的屏幕条带（FrameStrip）严丝合缝地编织为一幅完整长卷。

* **平台**：Android 11+ (API Level 30+)
* **技术栈**：100% 纯 Kotlin 2.x (K2) + Jetpack Compose BOM + Material 3 Expressive + `kotlin-inject`
* **协议**：MIT License
* **开源分发**：GitHub Releases / F-Droid / 酷安 (Coolapk)

---

## 🌟 核心特性 (Key Features)

### 1. 结构性隐私安全 (Structurally Private)
* **绝对零网络权限**：`AndroidManifest.xml` 中**彻底不声明 `android.permission.INTERNET`**，并在 Gradle 构建期设置熔断检查任务。这不是一句道德誓言，而是系统底层施加的物理硬约束——应用在能力上根本无法将捕获的屏幕内容发送至任何服务器。
* **“失明”的无障碍服务 (Blind Accessibility Service)**：完全不申请 `canRetrieveWindowContent` 与 `accessibilityEventTypes`。应用**不能、也绝不去读取**界面上的任何控件树和私密文本（如密码、银行账号、聊天记录），纯粹仅行使“静默截图 + 手势滚动”两项视觉能力。

### 2. 免弹窗原生级体验 (No MediaProjection Friction)
* 告别 Android 14+ 录屏框架（MediaProjection）烦人的“每次截屏都要全屏系统弹窗确认”的致命折磨；
* 基于 Android 11 原生 `AccessibilityService.takeScreenshot()`，直接读取 GPU 硬件缓冲（HardwareBuffer），启动即截，用完即走；
* 利用 `TYPE_ACCESSIBILITY_OVERLAY` 特权悬浮窗，免除用户在系统设置手动授权“显示在其他应用上层”的高门槛弹窗。

### 3. 专治各种“系统截不了” (Works Where Native Fails)
* Android 12+ 原生的 `Capture more` 极度依赖 App 主动实现 View 层的 `ScrollCaptureCallback` 接口；
* 在面对 **Flutter 跨平台应用、动态 WebView 网页、复杂自定义 Compose 容器、微信长图文** 时，原生长截图按钮经常神秘失踪；
* ScrollLoom 纯靠外挂式“手势驱动 + 纯视觉逐行亮度匹配”，只要屏幕内容在垂直滚动，就能无死角编织长图。

### 4. 极致轻量与边缘攻防 (Lightweight & Edge-Case Defenses)
* **拒绝几十兆臃肿库**：不依赖 OpenCV，核心对齐采用纯 Kotlin 实现的 **3 栏分带 1D 亮度投影（Left 25%, Mid, Right）+ 水平方差指纹**，单次计算仅需 0.8~1.5ms；
* **3 帧时域方差掩码 (Temporal Variance Mask)**：滚动中自动识别并剔除 App 内部静态吸顶导航栏（AppBar）与悬浮按钮（FAB），彻底杜绝悬浮重影；
* **Continuation Hold 手势硬停 + 协程看门狗**：滑动终点注入 150ms 零位移滞留手势化解 Fling 惯性滑行，配合 1500ms 协程看门狗防止触摸队列卡死。

### 5. 极限防爆内存与智能分卷 (Anti-OOM & Smart Pagination)
* **双区域局部切片提取 (ROI Sub-region Extraction)**：坚决禁止整帧全尺寸 Bitmap 软拷贝，仅切取用于匹配的特征带与新增条带，2K/4K/折叠屏实测图形常驻峰值严格压制在 **8MB ~ 12MB**；
* **坚决禁用 WebP 超长图**：受 WebP 格式规范 16,383px 物理上限限制，项目确立 **PNG 为唯一通用导出格式**（最高支持 20 亿像素）；
* **流式 Deflater 直写 MediaStore**：导出过程无全量大图实例化，通过 `DeflaterOutputStream(level=1)` 逐块直写系统相册，导出堆内存峰值 $< 1\text{MB}$；
* **30,000px 智能分卷阈值 (Smart Pagination)**：超长图自动在空白间隙分页导出为 Part 1 / Part 2，彻底解决微信发送截断（25,000px 限制）与系统相册崩溃问题。

---

## 🏛️ 核心架构隐喻 (Loom Metaphor)

整个项目代码库的概念设计严格同构于传统**织布机（Loom）**的工业意象：

```
[Screen Display]
       │
       ▼  (takeScreenshot)
 [Dual-ROI Extraction] ──► 仅提取局部特征带，立即释放 HardwareBuffer (显存峰值 < 12MB)
       │
       ▼  (OverlapMatcher)
 [3-Column Luma & Mask] ──► 3 栏投影 + 3 帧时域方差掩码消除吸顶栏，计算重叠位移 Δy
       │
       ▼  (LoomEngine.weave)
 [Weaving & Trim] ──► 剔除重叠冗余，将纯新增条带灌入
       │
       ▼  (TileStore)
 [1024px Disk Chunks] ──► 增量瓦片磁盘落盘，内存零常驻
       │
       ▼  (Streaming Deflater Assembler)
 [Finished Long Scroll] ──► 流式直写 MediaStore，支持 30k 智能分卷或无损单图
```

---

## 📂 完整知识资产与文档导航 (Documentation Hub)

* 📐 **[技术架构与算法设计白皮书](docs/ARCHITECTURE.md)**：2026 工业级架构规范，包含 3 栏投影、时域方差掩码、双 ROI 提取与 4 大终审补丁设计。
* 🗺️ **[产品路线图与里程碑](docs/ROADMAP.md)**：从 Phase 0 脚手架到 Phase 4 开源发布的四阶段敏捷实施计划。
* 🔬 **[参考开源项目深度解析](docs/REFERENCES.md)**：深入剖析参考项目 `garregusev/android-scroll-capture` 带来的实战避坑经验与生产范式。
* 🌍 **[生态调研、竞品全景与立项决策](docs/ECOSYSTEM_AND_COMPETITORS.md)**：LongShot 的兴衰、闭源商业工具的隐私信任困境、以及放弃国内商业市场的财务与政策硬核论证。
* ⚙️ **[AOSP 底层硬限制与失效机理](docs/AOSP_CONSTRAINTS.md)**：333ms 节流常量、FLAG_SECURE、Android 15 录屏强制弹窗、VSYNC 管线时延、WebP 16k 物理上限与 30k 智能分卷论证。
* 🎨 **[品牌决策、安全哲学与定位战略](docs/BRAND_AND_STRATEGY.md)**：五大命名避坑推演、Loom 隐喻体系、拒绝“Zero-permission”文字游戏的结构性安全哲学。

---

## 🙏 致谢 (Acknowledgments)

本项目深度致敬并参考了由 **garregusev** 开源的 [android-scroll-capture](https://github.com/garregusev/android-scroll-capture)（MIT License）。该项目提纯自已上架商用 App 的宝贵生产实战经验，为 ScrollLoom 攻克手势防甩动、GPU 缓冲释放与纯 Kotlin 逐行亮度轻量对位提供了至关重要的工程基石。

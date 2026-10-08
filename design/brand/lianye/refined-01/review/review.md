# 连页｜01 精密取景：第一轮精修

用户于 2026-10-05 选择只继续 01，其他方向停止考虑。本轮保持“四个有限取景角＋一张完整长图越过下界”的主体关系，完成准确几何、试色、光学尺寸与应用预览。方向已选，精修稿与配色尚待用户评审。

## 看图与源文件

- [浅色品牌组合 PNG](E:/Documents/scroll-loom/design/brand/lianye/refined-01/review/brand-light.png)
- [深色品牌组合 PNG](E:/Documents/scroll-loom/design/brand/lianye/refined-01/review/brand-dark.png)
- [圆角方形与圆形桌面预览 PNG](E:/Documents/scroll-loom/design/brand/lianye/refined-01/review/applications.png)
- [单色正形与反形 PNG](E:/Documents/scroll-loom/design/brand/lianye/refined-01/review/monochrome.png)
- [透明主标识 SVG](E:/Documents/scroll-loom/design/brand/lianye/refined-01/assets/symbol.svg)｜[透明主标识 1024 PNG](E:/Documents/scroll-loom/design/brand/lianye/refined-01/assets/symbol-1024.png)
- [单色 SVG](../assets/symbol-mono.svg)｜[反白 SVG](../assets/symbol-reverse.svg)｜[小尺寸光学校正 SVG](../assets/symbol-small.svg)
- [108 前景 SVG](../assets/adaptive-foreground.svg)｜[108 背景 SVG](../assets/adaptive-background.svg)｜[108 单色层 SVG](../assets/adaptive-monochrome.svg)
- [24 px 单色实际文件](../assets/symbol-small-24.png)｜[32 px](../assets/symbol-small-32.png)｜[48 px](../assets/symbol-small-48.png)｜[64 px](../assets/symbol-small-64.png)
- [24 px 原尺寸及像素放大检查](pixel-check.png)

## 本次改变与原因

1. **整体比例更紧凑。** 100 单位母版中，取景范围宽 66、高 50，长图宽 28、高 72。长图起于取景范围内，下端比范围下界再延长 32 单位，保留“超过一屏”的主证据。四角与长图没有连接成机器机身。
2. **下方出口更清楚。** 上方角标横臂长 12，下方内侧横臂缩到 8；避免下方角标挤向长图侧边或看成纸槽。下界仍只有两侧角标，中间没有横梁截断画面。
3. **统一小圆角与平直端点。** 主版统一 5.8 单位线重、3 单位几何圆角；曲线只用于角部，没有圆头速度线、阴影、渐变或新增图形。用准确 SVG 替代生成稿中的线重与比例漂移。
4. **小尺寸单独校正。** 校正版线重 7 单位，圆角半径 2，下方横臂进一步缩到 7，长图顶边下移 1。轮廓关系保持一致，增加角标体量并为内部空隙留位置。它是同一标识的光学尺寸，不是第二个 Logo 方向。
5. **对齐导出画布。** 图形外接范围的中心位于源坐标 (50,53)，独立标识导出整体上移 3 单位，使 100×100 画布上下留白一致。首次像素检查发现 24、32 px 的下边留白不足，已修正并重新验证。

## 当前推荐配色

| 用途 | 色值 |
| --- | --- |
| 石墨取景角、深色应用底 | `#252B2A` |
| 连续长图的强调色 | `#DB653D` |
| 浅色品牌底、深底取景角 | `#F7F4EE` |

取景角保持中性，强调色只标明完整长图；单色时同样能看到长图越界，因此功能关系不依赖颜色。计算得到橙色对暖白约 3.22:1、橙色对石墨约 4.08:1；这是颜色间的计算对比，不等于整套 UI 的无障碍验收。

旁边的「连页」与「自动长截图」采用标准微软雅黑排版，作为品牌组合示范；未引入其他方向的定制共笔字形，文字也不嵌入桌面图标。标识本身的 SVG 不依赖字体。

## 桌面应用与实际检查

依据 [Android 官方 adaptive icon 指南](https://developer.android.com/develop/ui/compose/system/icon_design_adaptive)，准备 108×108 的独立前景、背景和单色层；预览以中心 72 单位窗口模拟圆形与圆角方形遮罩。真实前景保持透明，没有把遮罩、圆角底板或外部阴影烘入前景文件。单色层由相同光学校正路径组成。

实测 24、32、48、64 px 两套单色源文件的四邻域不透明像素连通结构：均保留 **5 个独立部件**，即四个取景角及一张长图；没有部件粘连或消失，画布边缘没有达到检测阈值的墨色，中央长图内部保持透明。阈值为 alpha ≥128，不能把这项机器检查说成用户第一眼识别测试。

108 单色前景的检测像素离中心最远约 31.313 单位，位于半径 33 的中心安全区内。七份 SVG 的 XML 与路径数量通过检查。主 agent 直接查看了浅色、深色、桌面两种遮罩和单色展示；原尺寸 PNG 与像素放大检查另行保留。完整数据见 [verification.json](verification.json)。

尚未声称手机桌面实机验收或无名称用户辨识已完成。当前仍可能被联想到扫描、文档捕获；与普通扫描符号的区别来自长图明显超过取景范围。品牌独特性也不能由机器连通检查证明。

## 制作方式与范围

本轮是原生矢量细化，没有新的 imagegen 调用或生成 prompt。统一几何源为 [geometry.json](../geometry.json)，SVG 与所有栅格尺寸由 `build/logo-review/render-refined-01.ps1` 同源输出；四倍采样后生成 PNG。复核脚本为 `verify-refined-01.ps1`。没有编辑旧生成稿或覆盖原始九方向评审。

本轮产物是可审阅的品牌精修稿，未更改应用资源、包名或 UI，也未进入交互原型。下一阶段在本稿确认后继续交互设计。

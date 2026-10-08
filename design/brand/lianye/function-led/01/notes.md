# 01 — 穿框长页：品牌方向视觉稿记录

## 生成方式与文件

- 使用 built-in imagegen，类别 logo-brand；未使用 CLI/API，未用代码绘制或编辑图像。
- 原始生成图：`C:/Users/wgx/.codex/generated_images/01a10b50-1019-7212-a124-6e8c2c8778b1/exec-e451a373-fc3a-41fd-9730-f57c1bfa92f2.png`。
- 项目副本：`E:/Documents/scroll-loom/design/brand/lianye/function-led/01/logo-board.png`。
- 实际画布：1536 × 1024 横向，Format24bppRgb；已用 System.Drawing 读回副本核验尺寸。
- 一次生成，无后续编辑。原始生成文件保留。
- 展示板包含左侧大主标识、右上应用图标容器、右下单色结构，共同展示一个方向。
- 试色：石墨黑、暖白、烧橙。实际栅格颜色并非经过数值色彩校准的正式品牌色。

## 引用来源

1. [Apple — Take a screenshot on iPhone](https://support.apple.com/en-ca/guide/iphone/iphc872c0115/ios)。官方产品动作是把超出当前屏幕的内容捕获成整页；本案据此对比短取景范围与越界长页。形态转译属于本次设计推断，未复用 Apple 图形或 UI。
2. [Leica — Q3 官方产品页](https://leica-camera.com/en-GB/photography/cameras/q/q3-black)。官方介绍数字取景选择、OLED 取景器与紧凑产品构成；本案从范围边界与节制构图中提炼线重、直角、对齐、空白原则。该原则属于本次设计推断，未复用 Leica 品牌图形或相机外形。

以上两个官方来源在概念阶段经 web 搜索并打开阅读。本轮未读取其他组作品，也未加入来源图片作为图像参考。

## 图像自评与检查

- **可见功能结构：** 四个分离的直角角标围出上方较短捕获范围；完整长矩形的顶部在范围内，两条连续侧边跨过原下边界，闭合底部位于框外。这一短框与长页的长度差异是跨过一屏形成长图的可见证据。
- **风格：** 平面几何与直线占主导，暖橙用于突出长页，单色结构仍可看见穿越关系。线条比最初文字设想更粗，靠近工业标识而非纤细的制图线。
- **三种应用：** 三者保持四角加单张连续长页的拓扑关系；由于生成式栅格绘制，线粗与角臂比例存在小差异，未做到像素级一致。它们是同一方向的应用展示，不是三个概念。
- **文本禁令：** 直接查看后，未见标题、字标、说明、数字或水印。
- **语义风险：** 最主要是扫描工具或文档捕获；长页的纵横比亦可能被读成设备屏幕。分离角标、开放边中段、没有机器外壳或纸槽，有助于降低小票打印机联想，不能保证所有观者都先读出长截图。
- **旧构形禁令：** 未见 S/Z 折带、闪电、基础门洞、分段数字 0、递增三横条、动物或软尺。未使用相机镜头、剪刀、锁、云、链条或箭头。长矩形是完整页轮廓，没有分段为数字结构。
- **画面限制：** 存在轻微的生成式栅格颗粒和色面起伏；该稿适合比较品牌方向，不能当作已定稿的平面矢量母版。
- **当前验证边界：** 这是栅格探索。未做 24 px 可用性、正式 Android adaptive icon、无障碍对比度、原创唯一性或商标核验；没有宣称已通过这些检查。未改应用代码。

## 完整实际生成 prompt

```text
Use case: logo-brand.
Asset type: one clean raster brand direction presentation board, exact 1536x1024 landscape canvas.
Primary request: create a restrained original industrial line emblem for an Android scrolling long-screenshot tool. The visual must immediately suggest a capture boundary with one continuous long captured image passing beyond it. This is ONE logo direction shown in exactly three applications, no words.

Backdrop and layout: uniform warm white #F7F5EF. Large standalone emblem centered in the left two thirds, approximately 430 pixels high. On the upper right, show the IDENTICAL emblem centered inside a single graphite rounded-square app-icon container, about 260 pixels wide. On the lower right show the IDENTICAL emblem in pure graphite monochrome on the bare warm-white background, about 225 pixels high. Generous quiet whitespace, strictly flat orthographic artwork, no dividers or extra decoration.

Exact emblem geometry: four detached precision right-angle corner brackets define a short near-square capture window. Think of a 56-unit-wide by 54-unit-high imaginary capture window; each corner has an 8-unit horizontal arm and a 12-unit vertical arm, all straight, same weight, flat line ends, minimal manufacturing radius. The middle of every window edge remains open. Inside this short capture window is ONE narrow, upright, complete rectangular long-image outline, 32 units wide and 80 units high. It is horizontally centered, its top begins 8 units below the capture window top, and its closed straight bottom ends 34 units BELOW the capture window bottom. Its two side edges pass uninterrupted through the open space between the capture window's lower two brackets. The short capture corners remain at their original lower height, clearly revealing the same long image extending beyond one screen. Nothing segments the page. The inner page is EMPTY, no content lines. No full horizontal bar crosses the page at the window bottom. The four corner brackets are not attached to a device outline or any machine body.

Style: precise equal-weight industrial linework, disciplined geometric spacing, professional brand mark, economical five-part composition (four open capture brackets plus one continuous long page). No UI controls, no process diagram, no physical object realism.
Color palette: graphite #272829 capture brackets and burnt signal orange #CD572C long-page outline on the large left emblem. Upper-right graphite app container uses warm-white capture brackets and the same burnt orange page. Lower-right monochrome retains EXACTLY the same outline structure and proportions, all graphite. Color is only a trial brand palette, structure must remain readable in monochrome.

Text: absolutely NO text of any language, no title, no name, no captions, no labels, no lettering, no numerals, no watermark.
Constraints: exactly one main emblem and two faithful smaller applications of it; same corner lengths, page proportion and uninterrupted crossing relationship everywhere. It must read as a capture boundary and long image, not as a printer, receipt machine, ordinary smartphone, door, elevator, or standard crop button. Avoid printer chassis, output slots, paper-roll curls, jagged receipts, device buttons, screen notch, lens, shutter, arrows, scissors, chains, clouds, locks, pixels, folded ribbons, S/Z shapes, lightning bolts, broken digit zero, stacked increasing horizontal bars. No gradients, shadows, 3D, metallic material, texture, perspective, glow, diagrams, extra shapes, decorative marks, or presentation typography.
```

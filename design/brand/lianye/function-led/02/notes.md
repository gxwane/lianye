# 02 共区长窗：品牌方向视觉稿记录

日期：2026-10-05。本记录的初次栅格探索使用 built-in image_gen，通过 imagegen skill 工作流；没有使用 CLI、外部 API 或应用代码。只研究本组概念，没有读取其他组作品。最终展示稿已换为本组提供的准确几何 SVG 的 PNG 预览，详情见文末更新。

## 交付与生成路径

- 最终供总审的展示板：`E:/Documents/scroll-loom/design/brand/lianye/function-led/02/logo-board.png`，已实测 1536 × 1024 px。
- 保留初稿：`E:/Documents/scroll-loom/design/brand/lianye/function-led/02/logo-board-original.png`，已实测 1536 × 1024 px。
- 初次 built-in 生成原路径：`C:/Users/wgx/.codex/generated_images/01a10b50-4297-7143-8ba7-d861fa1044d1/exec-ef7daf58-99b0-4147-9de0-c3606ee845ea.png`。
- 一次针对性编辑原路径：`C:/Users/wgx/.codex/generated_images/01a10b50-4297-7143-8ba7-d861fa1044d1/exec-6f8719ac-976e-4df0-9287-992a1973f289.png`。
- 两个生成原文件仍在原路径，工作区为复制件。
- 编辑输入仅为本组初次生成图，不包含官方品牌图片或其他组图形。

## 来源与借鉴范围

[Figma：The making of a product icon](https://www.figma.com/blog/the-making-of-a-product-icon/) 的官方叙述支持从产品相关概念开始并检查不同尺度下的图标部件；[Figma：Evolving our visual language](https://www.figma.com/blog/figma-on-figma-evolving-our-visual-language/) 描述由基本图形与较大节点构成的视觉语言。设计推断是用较粗模块边界承担连页的捕获与接合动作；没有采用 Figma 标志的具体模块排布或配色。

[Adobe Photoshop：Get started with Photomerge](https://helpx.adobe.com/photoshop/desktop/repair-retouch/clean-restore-images/get-started-with-photomerge.html) 说明重叠照片可通过匹配合为单张横向或纵向画面。设计推断是删除来源框在共享区的内部边界，让同一内窗从上画面贯穿到下画面；没有采用 Adobe 的品牌图形，也没有把其参数视为连页产品参数。

完整文字构思见同目录 `concept.md`。

## 初次生成实际 prompt

```text
Use case: logo-brand
Asset type: raster brand direction presentation board, 1536 x 1024 landscape.
Primary request: Create ONE distinctive geometric brand mark for a mobile automatic long-screenshot stitching app. Display exactly three applications of the SAME mark: a large primary logo on the left, the same logo in an app-icon tile at upper right, and the same logo in one solid monochrome at lower right. This is a sophisticated brand composition with a strong dominant mark and ample whitespace, not a flow diagram.
Scene/backdrop: uniform warm ivory #F4F0E8; no grids, panels, divider rules, labels, letters or text of any kind.
Mark construction: The mark is an elongated merged screenshot window built from TWO straight upright portrait screen frames. The upper source frame is 10 units wide and 12 units tall at (0,0). The lower equal frame is at (2,8). Their overall union is 12 units wide and 20 units tall. Retain the stepped EXTERNAL contour: a small right-side outward shoulder at height 8 and a small left-side inward shoulder at height 12. All straight edges, square corners, no rotation. Use visually substantial border bands about 1.5 units thick, not a default thin line icon.
Crucial function geometry: The overlap is MERGED. REMOVE both frames' duplicated INTERNAL transverse borders and internal overlapping side borders in the central overlap. There must be ONE uninterrupted tall inner aperture from the top source down into the bottom source. No horizontal divider across the aperture, no two closed square holes. Place exactly ONE small solid horizontal rectangular content block floating centrally in the shared overlap, about 4 units wide and 1.5 high; this shared feature appears only once. Do not repeat content bars above or below. The two external offset shoulders show separate captured sources while the open long interior shows that they became one long image.
Style/medium: strict 2D flat modular geometry, graphic design, bold filled border pieces, crisp edges, no surface texture, no perspective, no shadows, no highlights.
Color palette: main left mark uses deep ink plum #2F2452 for its upper source border and vivid vermilion #F16B3D for its lower source border; shared content block plum; blend no transparency and add no third overlap color. The upper-right app tile is a flat deep plum rounded square with generous padding and the same mark in warm ivory and vermilion. Lower-right monochrome mark is entirely deep plum on the ivory backdrop.
Composition/framing: large mark roughly 440px tall centered around (460,512), with broad quiet negative space. Upper-right app tile about 280px square centered around (1180,310). Lower-right same mark roughly 200px tall centered around (1180,730). Maintain identical outer contours and merged-open-interior geometry in all three uses.
Constraints: no brand name, no words, no captions, no arrows, no phone hardware, no camera, no folded-paper corners, no three content lines, no gradient or 3D. Avoid separate stacked documents, generic copy icon, chain link, segmented zero, number eight, S or Z ribbon. Only the one mark and its two smaller applications; do not add demonstrations or stages. Deliver a polished graphic board at 1536 x 1024.
```

调用参数：`transparent_background=false`；新图生成未设置引用图像参数。

## 一次针对性编辑实际 prompt

```text
Use case: precise-object-edit / logo-brand.
Image 1 is the edit target: the complete 1536 x 1024 brand board.
Change only the geometric construction of the SAME logo in all three positions. Keep the ivory background, left dominant composition, upper-right plum app tile, lower-right monochrome application, the plum/vermilion palette, and all placements unchanged. No text.
The current narrow-upper / broad-lower construction resembles a connector. Correct it so BOTH SOURCE SCREEN FRAMES HAVE EXACTLY THE SAME WIDTH. The lower frame is a simple 2-unit RIGHT and 8-unit DOWN translation of the upper frame; it is never wider and never flares outward symmetrically.
Use this single external contour template for ALL THREE logos, normalized units: (0,0) -> (10,0) -> (10,8) -> (12,8) -> (12,20) -> (2,20) -> (2,12) -> (0,12) -> close. This is two same-size upright rectangular screen captures with a small rightward offset, merged into one longer frame. Border thickness approximately 1.5 units. Main mark's upper frame bands are plum, lower frame bands are vermilion. Only the app tile uses ivory in place of plum so it remains legible.
IMPORTANT: Remove any horizontal source-screen edge crossing the shared central interior. There is ONE CONTINUOUS LONG APERTURE, not two closed rectangles. The aperture follows the one-sided offset of the external contour, with no central bridge or divider attached to the border. Exactly one detached short rectangular content block floats within the shared central overlap, as in the existing left mark.
All three applications MUST be geometrically identical scaled copies, including that detached content block, all shoulders and the shared open interior. The upper-right tile may not keep an extra inner rectangular outline or extra horizontal rail. The lower-right mark is the same external contour and aperture in one solid plum color.
Produce strictly uniform flat color fills and square-corner geometric bands. Preserve current canvas and layout. No gradients, surface grain, shadows, arrows, hardware connector features, folded corners, S/Z ribbons or explanatory diagrams.
```

调用参数：`referenced_image_paths=[初次生成原路径]`、`transparent_background=false`。编辑目标在编辑前已调用 view_image；生成工具内显示的完整图也进行了直接目视审查。

## 目视自评与审核结论

这是一个方向的三个应用：左侧大主标识；右上应用图标容器；右下单色结构。没有标题、文字、说明或功能流程图。大标识采用厚重平面边界、深紫与朱橙试色；视觉主次明确。

**可见功能证据：** 左侧主标识和右下单色版可以看见上、下画面边界接成一个纵向内窗；中间没有贯穿窗口的横向分隔边，仅有一个悬置内容矩形。共享内容出现一次，内窗贯通，至少比两张完整叠纸更接近连接成长画面的动作。

**仍未解决的结构偏差：** 两个来源模块没有严格保持概念指定的相同宽度与右下平移，实际仍是上窄下宽，外肩也偏向对称扩展。右上应用容器中的白色内框仍保留额外下横边，与左侧、右下的贯通结构不完全一致。针对性编辑没有彻底消除这些问题，不能宣称三应用是精确同一几何母版。

**主要误认风险：** 粗边界、窄上部、宽下部和中央短矩形容易让人想到 USB 接口、连接器、电池或抽象设备；这是当前比“复制文件”更明显的风险。自动截屏与长图的语义尚不够独占，不能靠方向名称替代第一眼辨识。

**审核建议：** 保留作比较用品牌方向探索，但当前不通过精确几何一致性检查，建议总审将其列为需要重构的候选。若后续继续，需要先修正等宽来源模块与三处同形，再评估设备误认；不应直接导入应用作为正式 Logo。

**资产边界：** PNG 栅格探索，不是矢量资产；没有做 24 px 可用性检验，也没有商标检索或唯一性核验。没有更改应用、提交 Git 或发布资产。

## 总审后更新：展示准确几何

上述自评针对两次生成式栅格结果，作为过程记录保留。agent 后续提供了 `logo-geometry-correct.svg` 及对应的 `logo-geometry-correct.png`。主 agent 直接看图后，将该准确几何预览复制为当前 `logo-board.png`；错误生成板保留为 `logo-board-generated-failed.png`，不再作为对比图中的现稿。

当前图是一个居中的标识，没有三应用副本。上下捕获模块等宽，下模块向右、向下平移；重叠区的内横边被删除，只有一个悬置的共区内容块。它已修正上窄下宽和多余内边的问题，但构形修正不等于品类识别已成立：无名称时仍可能像连接器、剪贴工具或文档合并。

当前资产为本轮方向验证用 SVG 与 PNG，未完成单色、反白及 24/32/48 px 的正式应用验收。原始实际生成 prompt、生成文件及失败结论均保留，不将生成稿描述成准确矢量母版。

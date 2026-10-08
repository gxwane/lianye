# 连页 · B「延尺」品牌方向展示板

## 交付与范围

- 成稿：`brand-board-yanchi.png`。
- 类型：一张 3:2 品牌方向展示板，内含左侧主标识、右侧圆角应用图标、底部黑色结构示意。
- 工具：built-in imagegen；一次初始生成、一次针对性材质精炼。未采用 CLI、SVG 替代图或手工像素编辑。
- 主物件：压紧的青绿裁缝软尺卷，前缘接出近直宽尾，短银色包头，少量粗刻痕。
- 本轮只做品牌探索；没有查看其他 agent 的成稿、改动应用或提交 git。

## 研究来源与可借鉴原则

这里记录的是我从官方材料提取的设计原则，不是对品牌方设计意图的引用；没有把参考图作为生成输入，也没有照描其主形。

1. **Things** — [官方图标更新](https://culturedcode.com/things/blog/2025/09/things-for-os-26/)，[官方品牌资产入口](https://culturedcode.com/press/)。
   - 官方介绍蓝色盒子的更新及多种外观。
   - 提取原则：材质与光影可以更新，核心物件的体量、比例与身份需要保持稳定。
2. **GarageBand** — [Apple 官方产品页](https://www.apple.com/ios/garageband/)，[官方 App Store](https://apps.apple.com/us/app/garageband/id408709785)。
   - 提取原则：用单一可认物件的主要体量建立识别，控制内部细节，比例与轮廓先于写实程度。
3. **Paste** — [官方 Media Kit](https://pasteapp.io/media-kit)。
   - 官方品牌资产页提供图标、浅深版本及有限的主色。
   - 提取原则：收窄色域和明暗层次，让主体比表面效果更先被看见。
4. **Apple App Icon HIG** — [官方指南](https://developer.apple.com/design/human-interface-guidelines/app-icons)。
   - 提取原则：用提炼的插画代替细节繁多的照片，以小尺寸可辨的主要元素形成核心概念；不要直接复刻应用截图或标准 UI。

## 概念取舍

- **展开的裁缝软尺卷：保留。** 卷体与尾端是同一个连续物件；长而宽的展开部分和刻痕是物件身份的一部分。
- **单柄长尾夹：淘汰。** 金属柄容易像提包把手，形态和语义偏向文件收纳。
- **短柄订书机：淘汰。** 识别依赖夹口与底座，缩略后容易变成普通夹具。

没有再发散第四个方向，也没有把渐变当作重新设计的理由。

## 视觉审核与修改

第一版可认出裁缝软尺，整体布局与三种应用成立，中文字「连页」及标签「B · 延尺」正确。但织物颗粒与金属纹理偏写实，底部单色结构仍有明暗效果。

只做了一次修改：减少表面细纹，把色彩图形收束成宽而清楚的哑光面、少量卷绕边缘和克制的金属端头；要求底部结构图改为平面黑白。选择修改版作为当前展示成稿。

## 自评与风险

- **结构识别：** 实物卷体、展开尺带和金属包头完整可见，主要识别仍来自物件结构与刻痕；没有退化成纸页边框、S/Z 折带或分段 0。
- **呈现一致性：** 大主标识与应用图标保留同一方向、相同的主体比例和刻痕关系，图标使用深青底，形成清楚的前后层次。
- **最大误读：** 测量或裁缝应用。只看外轮廓时也可能被读成带尾端的卷状物；刻痕和端片承担区分作用。
- **材质风险：** 精炼后织物纹理明显减少，质感略偏柔软合成材料，仍需在后续正式品牌制作中校准哑光织物与软胶之间的区别。
- **单色风险：** 底部虽为黑白结构示意，但卷绕线比彩色版多且细，黑色仍有轻微深浅；它还不是严格同构的纯黑矢量母版。已达到本轮最多一次修改的限制，不额外生成第三版。
- **本轮边界：** 这是品牌方向图，不是已批准 Logo，也没有声称已完成 24 px 验收或商标可用性检查。

## 初始生成 prompt

```text
Use case: logo-brand.
Create ONE finished brand-direction presentation board for the Chinese Android utility app 连页, in a wide approximately 3:2 composition, about 1536 by 1024. Refined contemporary tool-app identity design, not a commercial product photograph. Pure clean warm-white background with generous whitespace and an orderly editorial grid.

Exact board content: only THREE applications of the SAME original mark.
1. Left: a large freestanding full-color master mark.
2. Right: one finished rounded-square app icon containing exactly the same mark in the same orientation.
3. Bottom: one smaller flat black monochrome structural version of the same mark, without a surrounding app tile.
Do not add alternate logo concepts, small-size test rows, duplicate icons, diagrams, mockups, rulers outside the mark, or extra decorative objects.

The sole identifiable object is a tightly rolled fabric tailor's measuring tape, NOT a retractable tape-measure box. A compact slightly flattened turquoise/teal coil sits at the upper left of the object. Its wide continuous tape exits the front-lower rim and extends almost straight toward the lower right, ending in a short satin-silver metal end cap the same width as the tape. Use a restrained three-quarter slightly elevated view. The visible coil is compressed and solid, with one or two broad indications of the wound edge; NO hollow doughnut hole. The rolled head is broad and compact. The outgoing tail is genuinely WIDE, substantial, and nearly straight, with only one gentle transition where it leaves the roll. Its form must be physically understandable as one continuous flexible measuring tape. Keep a small number of thick, clear pale measurement ticks on one edge of the turquoise tail, with no numerals or letters. Those ticks identify a measuring tape rather than toilet tissue or a paper receipt. Make the head-tail proportion distinctive and visually balanced before adding lighting.

Materials and rendering: matte tightly woven teal fabric, only a very subtle suggestion of weave, smooth simplified large surfaces, a short satin-silver end fitting with one quiet highlight. One controlled soft upper-left light source. Clean illustration-like edges, compact depth, carefully edited highlights and gentle contact shadow. Aim for the polish, object compression and readable material hierarchy of a mature productivity app icon, while inventing this object mark independently. Not a detailed still-life, not a tailoring illustration, not a photorealistic retail product, not plastic toy packaging, not a glass or rainbow-gradient logo. Color cannot substitute for the object silhouette.

The right rounded-square app icon has a deep dark blue-green solid or very subtly shaded background, carefully centered teal tape mark with restrained dimensionality and generous safe margin. It must reproduce the exact same coil, broad tail, silver end cap, and sparse measurement ticks as the freestanding mark.

The bottom monochrome mark must be entirely flat BLACK with a few purposeful WHITE structural openings where necessary to preserve the same tightly rolled tape and broad-tail identity. No gray shading, no color, no decorative drop shadow. Same object proportions, orientation and measurement-tick placement as the color master. This is a structural reduction, not a different symbol.

Typography: small quiet top-left label exactly “B · 延尺”. The only brand name is exactly “连页”, in a restrained, clean Chinese sans-serif, placed near or just below the large master mark. Correct Chinese characters. No English name, no slogans, no explanatory captions, no extra text. Layout should feel like a finished brand presentation, not a comparison worksheet.

Hard exclusions: no animal, no face, no anthropomorphic features, no Chinese-character or letter-shaped primary logo, no abstract geometric primary symbol, no S or Z folded ribbon, no lightning bolt, no doorway arch, no segmented zero, no three ascending stripes, no phone or screenshot frame, no overlapping paper pages, no stacked sheets, no camera/crop/share-icon collage, no toilet roll, no receipt roll. Make the actual identifiable fabric measuring tape the sole main object.
```

## 最终使用的针对性编辑 prompt

输入是第一版展示板，作为唯一 edit target。不是其他品牌或其他 agent 的参考图。

```text
Use case: logo-brand / precise refinement.
Refine the supplied brand-direction presentation board in ONE focused way: reduce product-render surface detail to a finished, refined tool-app icon illustration. Preserve the exact existing measuring-tape object, overall outline, head-to-tail proportions, orientation, location of silver end cap and pale measurement ticks, all three applications, wide 3:2 composition, whitespace and typography.

The two color marks should have broad, smooth MATTE fabric surfaces with no granular weave pattern or photographic cloth fibers. Keep the material supple and tactile through only a few controlled broad light and dark planes, not a shiny plastic or glossy glass effect. Simplify the visible coiled top to two broad wound-edge indications with generous spacing, preserving the recognizable tight rolled measuring tape and solid closed center. Simplify the silver end fitting to one clean satin-metal highlight, remove fine brushed-metal striations. This should look like a carefully illustrated mature productivity-app icon, not a retail photograph. Keep the freestanding color master and rounded-square icon identical in object geometry and same teal palette; do not introduce new variants or alter the object into an abstract symbol.

The bottom monochrome application must be genuinely FLAT solid black, with clean flat white structural openings only: no gray, gradient, glossy sheen, texture, or cast shadow. Preserve precisely the SAME silhouette, coil/long wide tail/end-cap structure and thick measurement ticks as the colored master. Reduce coiled interior details to the same two broad wound-edge indications. This is the same measuring tape mark converted to black and white, not a newly invented spiral or letter.

Keep the exact top-left small label “B · 延尺” and exact Chinese name “连页” where they already are, no other text. Keep only the same THREE applications. No extra marks, no dimension/test rows, no photos or mockups. Preserve the near-straight broad tail toward the lower right and its short end fitting. No S/Z folds, no receipt or toilet-paper roll, no hollow doughnut hole, no face or animals, no phone or screenshot frame, no added letters or Chinese-character logo.
```

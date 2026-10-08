# 06｜补行成图：视觉候选记录

日期：2026-10-05。下面四轮过程使用 built-in image_gen；未使用CLI/API。分类：首轮logo-brand，后三轮precise-object-edit。总审后，展示稿已替换为原生矢量几何及其确定性 PNG 预览，详情见文末更新。

## 最终文件与版本
第四轮生成结果现保留为 `logo-board-generated-final.png`，当时 PNG 文件头为1536×1024，文件1,321,104字节。首稿、第二稿、第三稿分别保留为`logo-board-v1.png`、`logo-board-v2.png`、`logo-board-v3.png`。当前 `logo-board.png` 是文末所述矢量构形的预览。

原生成目录：`C:/Users/wgx/.codex/generated_images/01a10b58-f0c6-7742-8d33-d2255223edec/`。
- V1：`exec-364150ac-a6d9-4e32-8b02-232c44b4aaed.png`
- V2：`exec-d6a081ad-624c-476e-a9d4-76a5e1a8d4f6.png`
- V3：`exec-e6b59ccf-26b1-48a0-8387-6adae38afdcc.png`
- 最终V4：`exec-c6c0cf51-c8c8-4c82-81a0-188498ca0f54.png`

## 来源与设计判断
[Microsoft应用图标设计指南](https://learn.microsoft.com/en-us/windows/apps/design/iconography/app-icon-design)已在概念阶段搜索并打开，借鉴核心功能集中在单一主体、减少不支持语义的细节的原则。[IBM图标设计规范](https://www.ibm.com/design/language/iconography/ui-icons/design/)同样已搜索并打开，借鉴像素网格控制比例、位置和视觉重量的原则。未使用官方图标作为生成输入，也未照描品牌形状。粗像素、纵长画面及仅一次新增带，是本稿对产品行为的设计推断；来源没有替本稿证明可识别性。

## 直接查看后的自评
最终画面是居中的一枚纵长朱红标识，暖白背景，没有名称、副本、图标容器或流程标签。方形亮点和阶梯山形提供图片类别线索；底部只有一条与主体等宽的带，左侧已接通，右侧保留一道横缝。上部白色山形已接到横缝，下带白形从相同左边界继续向下，较首稿的独立斜纹更能看出一幅画面的延续。整体主动作是图片在底部继续补入。

V1存在多余山峰、独立下带斜纹及色面纹理；V2去掉多余山峰，但下带仍偏左；V3将起点移到上部落脚列，但上部与横缝间仍有细红缝；V4闭合了该细红缝。四次均直接看图，结构问题也已消息根agent。

主要风险：下带的白形被模型画得比上段窄，只有左边界接续明确，不能宣称两边都精确同列；横缝与白色山形合并后可能被看成像素管道或损坏图片。纵长图片及方亮点仍可能引起像素绘画、相册或照片打印联想。色面仍可见极轻微生成式不均匀；目标为平色，但不能宣称像素级平色准确。

## 栅格与小尺寸限制
八列十二行是构形意图，生成图的台阶宽高并未全部落在统一模数上。最终不是严格像素字体或确定性网格图案，也不是SVG。若方向被正式采用，需重绘轮廓、接缝、统一模数，并修正下带白形宽度；不在此轮更改应用。尚未制作或验证24/32/48像素资产。缩小时方亮点、阶梯转折和横缝可能合并，底部补入的动作也可能退化成一般缺口或下划线。当前只能作为大尺寸品牌候选供总审，不宣称小尺寸可用、原创唯一或完成商标检索。

## 完整实际prompt
以下逐字保留四轮实际提交的prompt；V2使用V1工作区副本作edit target，V3使用V2副本，V4使用V3副本，均设置transparent_background=false。

### V1｜生成
```text
Use case: logo-brand.
Asset type: one original logo mark, an exploratory brand identity board for an Android scrolling screenshot application that appends only newly captured image strips into one long screenshot.

Canvas and backdrop: 1536x1024 landscape, perfectly plain warm ivory #FFF9F1 background. One large mark only, centered horizontally and vertically, generous clear margins. No typography or captions.
Primary request: draw a finished, distinctive brand symbol named in the brief as "补行成图", rendered through coarse digital pixel geometry. It must show ONE vertically elongated pixel image being extended by ONE new wide pixel strip at its bottom.
Structure: build the main silhouette on an invisible grid approximately 8 units wide and 12 units tall. The upper nine rows form one solid upright vermilion image face. Inside that face, cut out an ivory square sun near the upper right and ONE bold ivory stepped mountain contour below it so the subject clearly reads as an image. The image should be materially longer than a normal landscape photo icon. At the bottom, place exactly ONE two-unit-high wide new strip, perfectly aligned to the same left and right edges and pixel columns of the upper image.
The critical join: between the image face and this bottom strip, leave a one-unit-high ivory seam ONLY across the right three quarters of the width. At the left quarter, the image face and the strip are already fused through a broad solid pixel connection. Thus the lower strip is already joined on the left and still visibly separated on the right, within a single coherent mark. Carry the same stepped image contour into the bottom strip in precisely aligned pixel columns, so the lower strip visibly contains the continuation of the SAME image and is not an empty underline or a progress bar. Make the silhouette and the image contour simple, intentional and optically balanced.
Style and palette: flat single-ink vermilion #DE432D with warm ivory negative spaces, assertive coarse pixels, square terminals, all edges orthogonal stair steps. No visible construction grid, no hairlines, no perspective, no texture, no gradient, no shadow, no glow. Professional brand mark, carefully composed negative space, not an algorithm flow diagram and not retro game scenery.
Mandatory constraints: exactly one main mark. No app-icon rounded-square container. No phone outline or screenshot frame corners. No repeated full cards, no second photo, no gallery stack. No three increasing horizontal bars, signal bars, equalizer, downloading arrow, percentage, decorative detached pixels, pixel rain, fragments, AI repair symbolism, cloud, lock, chain, camera lens, letters, numbers, logo text, mockup, watermark or signature. The partly fused lower strip is the sole action cue. The image metaphor and downward accumulation must both be visible without a brand name.
```

### V2｜编辑
```text
Use case: precise-object-edit.
Input image 1 is the edit target: the existing vermilion coarse-pixel long-image logo on a warm ivory landscape board.
Primary request: correct ONLY the image contour at the lower joining strip, so this is unmistakably one picture being extended, not an image icon above a separate diagonal streak.
Keep invariant: 1536x1024 landscape board, one large centered mark, warm ivory plain background, vermilion color, upright long-image silhouette, square sun cutout, coarse orthogonal step edges, overall scale, broad left-side bridge, and ONE right-side horizontal seam above ONE wide lower strip. No extra symbol or text.
Targeted structural correction: simplify the ivory pixel mountain to a single main peak with a broad descending right flank. Remove the small extra right peak and the disconnected lower diagonal. The single descending right flank must visibly reach the horizontal seam from above, then resume directly below the seam in EXACTLY the same vertical pixel columns and at the same width, continuing downward and rightward inside the new bottom strip. At the join, both sides of this contour should line up so the eye can follow the same mountain silhouette across the seam. The bottom strip must contain a true continuation of that upper image contour, never a newly starting decorative zigzag. Keep sufficient vermilion thickness around the cutout so the lower strip still reads as one robust appended band. The left quarter of the new band remains already fused with the main image; the right three quarters retain the single horizontal ivory separation. This is one coherent finished brand mark.
Finish: perfectly flat solid color fields, hard clean pixel geometry, no mottling, gradients, dimensional effects or textures.
Avoid: disconnected streaks, duplicate mountains, extra bands, repeated photo cards, progress bars, falling pixels, download symbols, letterforms, mobile device frames, app-icon containers, captions, variants, legends, explanatory diagrams, watermarks.
```

### V3｜编辑
```text
Use case: precise-object-edit.
Input image 1 is the exact edit target, a 1536x1024 image. Keep the entire image unchanged EXCEPT the small ivory contour in the bottom band and its point of contact above the seam.
Critical correction: the current upper mountain ends with a broad ivory vertical foot at roughly x=820 to x=938, y=646 to y=698. The current lower ivory staircase incorrectly starts far left, roughly x=714. ERASE that entire lower staircase by filling it vermilion matching the surrounding band.
Replace it with a simple broad ivory pixel foot that STARTS directly beneath the upper mountain foot: x approximately 820 to 938, starting at y=744 (the top edge of the lower band) and descending to approximately y=797. After that, one square stair step to the RIGHT, so the lower part spans approximately x=861 to 979 and ends at y=852. No diagonal tail starting at x=714. The upper and lower foot must share EXACTLY the same x columns at the seam, like one landscape cutout interrupted by a horizontal slit. Extend the upper mountain's last foot by the tiny remaining amount down to the seam at y=705 so its continuation is visible. This is the only change to the upper part.
Keep the horizontal seam itself at y=705 to 744, and keep the broad vermilion bridge at the left from x=540 to x=668 unchanged. Keep the square sun, the main mountain peak, all exterior contours, mark size, placement, colors and the plain ivory background unchanged. The result must remain a single completed pixel brand mark: one long picture and ONE bottom strip already joined on the left, separated on the right, with the SAME image content visibly continuing across that join.
No new features, no extra marks, no text, no secondary version, no visible coordinate guides, no grid, no shadows. Flat solid color.
```

### V4｜编辑
```text
Use case: precise-object-edit.
Image 1 is the exact edit target. Make ONE local correction at the join of the vermilion pixel logo. Preserve every other part of the image unchanged.
The mountain ends above the horizontal white seam with a WIDE ivory rectangular foot. Immediately below that seam, there is currently a NARROW ivory stem with a small shifted square below. Replace that narrow stem and small square with a WIDE ivory continuation of the upper foot. Its LEFT edge AND RIGHT edge must be exactly vertically aligned with the corresponding LEFT and RIGHT edges of the upper foot. It must have the SAME FULL WIDTH as the upper foot. Do not narrow it and do not place it farther left. Imagine the horizontal seam temporarily hidden: the wide ivory foot would then form ONE uninterrupted stepped mountain contour.
Let this full-width ivory foot extend through the upper half of the new bottom strip, then turn a SINGLE short square step to the right in the lower half, maintaining the same stroke mass. This bottom negative shape is the continued lowest part of the image above, not a pipe, progress bar, new mountain or decoration. Also close the tiny red gap between the upper foot and the horizontal seam, so the white mountain foot actually reaches the seam.
Keep unchanged: one tall vermilion mark, upper mountain peak, square sun, entire outer silhouette, the broad already-joined left bridge, one right-side horizontal seam, one bottom image strip, 1536x1024 landscape canvas, centered placement, plain warm ivory background. No additional shapes, text, grid lines, explanation, shadows, effects, variants or watermark. Clean solid flat vermilion and ivory fields.
```

## 总审后更新：统一像素构形

主 agent 直接查看四轮生成结果后，认为台阶模数与内容接续仍不能只靠继续解释成立。因此按本组独立概念，用原生路径准确绘制 `logo-geometry.svg`，并由同一份几何数据输出当前 `logo-board.png`。源定义为 `build/logo-review/function-vector-scenes.json` 的 06 项，渲染脚本为 `build/logo-review/render-function-vectors.ps1`。这一步是几何绘制，没有新的 imagegen prompt。

当前构形采用八列粗像素，方形亮点、单个填实山形、只出现一次的底部新增带。上部蓝色与下部青色是未确认的探索试色；左侧两列连接，中右部留一条横缝，同幅内容在固定列内继续进入新增带。它修正了生成稿的细管道、偏移台阶与不均匀色面；原生成结果保留。

仍然存在相册、像素绘画、图片修复的品类误认，新增带能说明图像继续接入，但不能凭此证明用户第一眼会认出自动长截图。正式小尺寸、单色反白、应用容器和商标检索均未完成；当前 SVG 是方向几何稿，尚非生产资产。

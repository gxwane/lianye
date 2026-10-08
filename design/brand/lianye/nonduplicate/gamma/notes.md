# C · 接步：壁虎品牌方向探索

日期：2026-10-05。
阶段：概念登记获确认后的图稿探索；不是品牌定稿或应用实现。
领域：生物角色 / 有机轮廓。本轮没有读取其他 agent 的图稿或文件。
方法：已应用 brainstorming 的需求确认、三概念比较及取舍；获得壁虎方向确认后，使用 imagegen 技能与 built-in imagegen 生成展示板，做一次趾端简化；随后按根 agent 明确要求，另做一次仅删除错误方向标题的排版例外编辑。

## 确认展示板

- 最终文件：[gamma-gecko-step-board-v3.png](gamma-gecko-step-board-v3.png)
- 删标题前的原版保留：[gamma-gecko-step-board.png](gamma-gecko-step-board.png)
- 尺寸：1536 × 1024，3:2。
- 内容：左侧完整主标识、右侧圆角应用图标、底部黑色单色版本；三者沿用同一壁虎身份和接步姿态。
- 最终文字：仅保留主体下方正确的“连页”；顶部原有方向标题出现错字，现已整行移除。外围展示将使用原生文字标注“C · 接步”。没有英文品牌名或宣传口号。
- 已使用 view_image 读取实际输出，并确认文件内容与 imagegen 返回的图像内容一致。
- 最终复制后的 PNG 与选中原文件 SHA-256 一致：9603D0078EA9FAD85C85C517837DB76A96F3705980C0C71DD75D07246042ECAA。
- 图像读取工具返回 application/octet-stream 媒体类型，导致部分预览异常；读取文件的图像数据后以 image/png 显示，未改动图片像素。
- 没有伪造或宣称 24 px 验收；展示板上的底部图是单色应用，不是最小尺寸测试。

## 官方参考来源与可借鉴原则

以下原则为本轮的设计提炼，不是对其官方规范逐字转用，也没有转用任何现成角色外形。

1. **Duolingo**
   - 官方来源：[Reshaping Duo](https://blog.duolingo.com/reshaping-duo/)；另查看了官方角色形态与比例说明：[Building character](https://blog.duolingo.com/building-character/)。
   - 页面强调轮廓先成立，再加入动作与细节。
   - 本轮借鉴：先让完整生物轮廓和接步姿态清楚，再用一个克制的眼部细节赋予神态；不借猫头鹰、眼罩、巨大双眼或其身体比例。

2. **GitHub**
   - 官方来源：[Logo](https://brand.github.com/foundations/logo)、[Mascots](https://brand.github.com/graphic-elements/mascots)。
   - 品牌工具包分别管理 Invertocat 主标识与完整角色表达。
   - 本轮借鉴：标识只保留关键生物结构，不把皮肤、关节、服饰或完整角色插画细节装进 Logo；不借猫耳、章鱼肢体或 Octocat 构形。

3. **Mailchimp**
   - 官方来源：[Brand Assets](https://mailchimp.com/about/brand-assets/)。
   - 页面以 Freddie 固定的眨眼表达性格，并强调留出呼吸空间。
   - 本轮借鉴：一个稳定的小表情即可承担人格，亲和力也可以来自姿态和留白；不借猩猩面部、帽子或眨眼构图。

## 内部三概念的取舍

- **壁虎接步：保留。**四肢、宽指端与渐细尾巴共同定义真实生物轮廓；一只前掌向下探出、另一掌贴定，让身体处于正在接续下一步的状态。
- **跳蛛交替探足：淘汰。**多足轮廓容易挤成放射团块；依赖大眼睛补救又容易变成无差别可爱虫子。
- **尺蠖伸身接步：淘汰。**拱身容易重复已否定的门洞，过度简化容易再变成弯带；还有迟缓与虫害联想。

## 图形依据

- 角色是完整壁虎，辨识来自头、躯干、四肢和尾巴的有机解剖关系，不是给某个几何图案添加眼睛。
- 主体采用向右下方接步的不对称姿态；向下伸出的前掌在头部前方形成清楚的动作端点。
- 头部比例受控，使用单个可见的小眼部细节，没有 Q 版双大眼、露齿笑脸、配饰或儿童手游装饰。
- 使用深蓝绿与暖浅底，保持图形的单色轮廓成立，避开 Duolingo 的鲜绿与眼罩组合。
- 尾巴开放向上后方舒展，没有盘成圆环；四肢之间保留显著留白，不借屏幕、纸页或框线说明产品。

## 唯一一次角色细节修改

第一张生成稿的趾端数量偏多，可能在缩小时变碎。仅要求把趾端简化为宽瓣结构，并保留壁虎姿态、尾巴、头部、配色和展示板版式。没有增加新方向或额外应用。

生成结果没有严格把每一掌都做成三个瓣：前掌较清楚，部分后掌只有两个主要瓣。作为品牌方向图可以呈现简化效果，但后续若获选，应在矢量主稿中统一掌形规则，而不能把这份栅格展示板当作精确生产母版。

## 最大风险与自评

**最大误读：**仍可能先被归为爬宠、除虫、户外或动物游戏品牌。角色与截图工具之间的联系由“接步”姿态提供，但不能靠长故事消除品类联想。

自评：

- 本轮领域约束成立：实际输出是完整生物角色，没有回到纸张、屏幕或抽象块面。
- 主体动作清楚，圆角图标与单色版保留相同姿态；神态克制，没有依赖大眼卖萌。
- 这枚壁虎仍带有通用动物标识的风险，不能宣称已经具有足够独特性或商标可用性。
- 尾巴较长，当前轮廓有较强流动感；未来应检查观者是否先看到弯曲带而后才看到动物。
- 主体下方字标偏粗，后续可比较中等字重以获得更克制的文字关系；本轮没有用第二次修改处理这个次要问题。
- 单色版说明主体可以移除颜色，但不能据此宣称 24 px 成立；真实小尺寸、细尾尖和掌形都需下一阶段检验。
- 三个应用中的主要轮廓一致，但生成式绘图不保证局部像素或掌形严格同一；获选后应锁定一个矢量主标识再派生应用。
- 此稿仅供方向比较，不适合直接作为发布用 Logo 资产。

## 本轮排除形状

没有复用 S/Z 折带、闪电、粗体汉字主标识、门洞、分段 0、三条递增条纹、手机边框、重叠纸页。没有添加相机、屏幕、取景框、纸张、字母/汉字入形、运动说明图或几何块面上的眼睛。

应用图标的圆角方形仅为图标容器，不参与壁虎的主体构形。名字“连页”仅作为主体旁的字标。

## 初始生成 prompt

使用 built-in imagegen；未使用 CLI/API fallback，也未读取现成品牌角色图作为输入参考。

```text
Use case: logo-brand
Asset type: one polished original brand direction presentation board for the Chinese Android utility named “连页”.
Primary request: Create a finished exploratory logo direction based on a genuine original small gecko in an asymmetric downward stepping pose. This is a biological character logo, never paper, a screen, a device, lettering, or geometry with eyes.
Scene/backdrop: wide landscape presentation board, approximately 3:2 aspect ratio, pure clean very pale warm background, generous whitespace, precise graphic-design layout.
Subject: an entire gecko seen from a slightly three-quarter overhead angle. Its compact low torso follows the downward diagonal. Its modest-sized head looks toward the next step, facing down and slightly right. One foreleg reaches clearly downward into the next contact point while the other forepaw is planted; two hind legs remain visible. Rounded broad toe pads are simplified into a few spacious lobes, never tiny claws. The long tail tapers naturally and extends back toward the upper left in ONE gentle open arc, never forming an S, Z, ring, or loop. The role must be identifiable from the organic anatomy and stepping pose even without the facial details.
Style/medium: exceptionally clean flat vector-like brand artwork with smooth confident organic curves, considered negative spaces between limbs, mature friendly tool-brand character, restrained calm alert expression, small simple eyes, moderate head-to-body proportion, no oversized cute eyes, no chibi or cartoon-game styling. No scales, skin texture, tiny joints, teeth, spines, or decorative face details.
Color palette: the principal mark is a muted deep petrol teal with at most a small warm pale accent for a necessary facial detail; no lime green, no Duolingo color scheme. The application icon is a sophisticated rounded square in the same deep petrol family with the same gecko rendered pale and perfectly spaced. Flat finish, no glossy bevel, no realistic 3D.
Composition/framing: EXACTLY THREE appearances of the SAME gecko logo, same anatomy, pose, tail and proportions. Left: one large complete primary mark, generously sized, full tail and all four limbs visible. Right: one highly finished rounded-square app icon using that exact same mark, with adequate padding. Bottom: one pure BLACK monochrome version of the exact same mark on the pale board, not a separate animal or new pose. No other variants, no favicon tests, no dimension labels, no extra marks.
Text (verbatim): tiny tasteful top label “C · 接步”; brand name “连页” displayed once beside or below the large primary mark, in restrained clean Chinese sans-serif type, dark near-black. These are the ONLY text elements. No English brand name, no slogan.
Design principles: establish the full silhouette first; communicate character through a specific controlled pose instead of abundant facial detail; keep the logo simpler than a mascot illustration. This is original artwork, do not resemble or copy Duolingo's owl, Mailchimp's chimp, GitHub's Octocat, or any existing mascot.
Avoid: camera, phone, screen, viewfinder, phone outlines, crop corners, paper, overlapping sheets, folded ribbon, lightning, S/Z logo, letters or Chinese characters integrated into the creature, arched doorway shape, segmented zero/ring, three escalating stripes, ordinary animal head avatar, gratuitous smile, accessories, props, motion lines, icons explaining the action, watermark.
Output intent: a brand design exploration board with convincing visual craft. Show only these three applications of one logo, do not claim or fabricate 24px validation.
```

初始默认文件：
C:/Users/wgx/.codex/generated_images/01a101b7-3a60-74d0-9ad9-98e6020a9982/exec-f1c2f78c-9763-4c09-80ff-84257cc36519.png

## 唯一编辑 prompt

编辑目标为已经用 view_image 读取的初始 PNG。使用 built-in imagegen 的 referenced_image_paths，不创建新角色方向。

```text
Use case: precise-object-edit
Input image: the supplied image is the edit target, an original gecko logo brand board.
Primary request: Make ONE targeted refinement only: simplify the toe pads on all four feet in each of the three appearances of the same gecko logo. Each paw should now have exactly THREE broad rounded toe lobes, separated by TWO spacious shallow rounded notches. Keep the pads integrated with a sturdy wrist and leg; no separate tiny claws or fine fingers. The gecko still has four limbs and the downward-reaching forepaw remains unmistakable.
Preserve every other aspect: exact gecko head, eye, torso, limbs and pose, limb attachment points, long tapering open-arc tail, colors, rounded square app icon, mark sizes, presentation layout, pale background, Chinese sans-serif brand name “连页”, top label “C · 接步”, and the three applications only. Do not add anything or change pose. Do not crop the tail. Maintain the same refined organic gecko identity and mature flat brand finish, never cartoon-game styling.
The exact same paw simplification must appear in the left large primary mark, right app icon, and bottom black monochrome mark. No extra variants, no labels, no new text.
```

趾端简化后、删标题前的默认文件：
C:/Users/wgx/.codex/generated_images/01a101b7-3a60-74d0-9ad9-98e6020a9982/exec-c95356e8-5c5c-40e2-8f28-f048d5f7d9db.png

默认原文件保留，趾端简化图已原样复制到当前目录。未改应用代码，未提交 git。

## 排版纠错例外：仅删除错误方向标题

根 agent 查看展示板后发现顶部“接”字被生成为别的字；此前自审遗漏了这项错误。品牌字标“连页”本身正确。

按明确指令，以已查看的本地 gamma-gecko-step-board.png 为编辑目标，仅删除顶部整条小标题，让该位置恢复为浅底留白；不增加替换文字，也不再次修改角色或添加方向。原版保留。外围对比展示会用原生文字写“C · 接步”。

已使用 view_image 读取纠错后的实际本地 PNG 检查：顶部标题消失；主标识、圆角应用图标、黑色单色稿与正确的“连页”字标均保留，未发现姿态、四肢或尾巴结构的变化。

最终本地文件：gamma-gecko-step-board-v3.png，1536 × 1024。源文件与复制稿 SHA-256 相同，原版存在性检查通过。本段替代此前关于顶部文字已经正确的判断。

### 纠错 prompt

```text
Use case: precise-object-edit
Input image: the supplied local image is the edit target, the selected gecko logo presentation board.
Primary request: Remove ONLY the entire small centered direction heading at the very top of the board, the line starting with “C ·” and the two following Chinese characters. Fill its exact former area with the same clean very pale warm background so that the top header area is blank.
Preserve absolutely every other part of the image: the exact full gecko silhouettes, anatomy, toe shapes, stepping pose, eyes, tail curves, colors, positions and sizes; the large primary gecko on the left; the correct Chinese brand name “连页” below it; the rounded-square application icon on the right; the black monochrome gecko at the bottom; their current spacing and proportions; the 3:2 canvas and background.
This is purely a presentation typography correction. Do not redraw, simplify, restyle, rotate, resize, recrop, or alter any logo or the correct brand wordmark. Do not add any new title, letter, character, label, slogan, variant, or extra element. The heading will be rendered externally as native text. Change only the erroneous top heading area.
```

纠错默认文件：
C:/Users/wgx/.codex/generated_images/01a101b7-3a60-74d0-9ad9-98e6020a9982/exec-91ad4b33-ec27-4c92-b31c-6064bb3fd77b.png

本次使用 built-in imagegen；未使用 CLI/API fallback，没有在代码中绘制或修改图片。

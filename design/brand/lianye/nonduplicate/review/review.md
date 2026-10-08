# 连页：三份互不重复的 Logo 方向

日期：2026-10-05。阶段：品牌方向探索。

用户要求多个 agent 不允许重复，并允许参考成熟 Logo 的设计风格。本轮采用三个互斥领域，各 agent 独立研究来源、登记主构形、通过概念去重后出图。只保留三个主方向；每张展示板里的大标识、应用图标和单色结构是同一方向的不同应用，不额外计数。

## 原图与完整提示

- **A · 贯面**：[原图](E:/Documents/scroll-loom/design/brand/lianye/nonduplicate/alpha/alpha-guanmian-board.png) · [参考来源、初始与编辑 prompt、自评](E:/Documents/scroll-loom/design/brand/lianye/nonduplicate/alpha/notes.md)
- **B · 延尺**：[原图](E:/Documents/scroll-loom/design/brand/lianye/nonduplicate/beta/brand-board-yanchi.png) · [参考来源、初始与编辑 prompt、自评](E:/Documents/scroll-loom/design/brand/lianye/nonduplicate/beta/notes.md)
- **C · 接步**：[原图](E:/Documents/scroll-loom/design/brand/lianye/nonduplicate/gamma/gamma-gecko-step-board-v3.png) · [参考来源、初始与编辑 prompt、自评](E:/Documents/scroll-loom/design/brand/lianye/nonduplicate/gamma/notes.md)

三组均使用 built-in imagegen 生成和编辑，没有使用 CLI fallback，也没有转用现成品牌图形。C 额外移除生成错误的方向小标题，未改角色；主品牌字标均为「连页」。最终三张原图均为 1536 × 1024 PNG。

## 去重结论

| 对比 | 外轮廓与部件关系 | 结论 |
| --- | --- | --- |
| A / B | A 是两块大小不同的抽象切面共享整体外轮廓，白色弧缝贯通；B 是真实卷体接出宽尺带并以端头收束 | 不同主体与构形，差异不依赖材质 |
| A / C | A 无生物特征或具象工具；C 的躯干、四肢、头部和开放尾巴构成完整动物 | 不同主体与轮廓，差异不依赖眼睛或容器 |
| B / C | B 的卷绕结构、矩形带面、刻痕和端头形成物件身份；C 依赖肢体连接和探掌姿态 | 物件与生物结构不同 |

Alpha 的首个齐平双柱概念因为像暂停符号或双括号，在出图前退回。Gamma 的尺蠖因为拱形会重复此前门洞，在内部概念阶段淘汰。没有将相同轮廓的配色或圆角变化展示为新方向。

## 根 agent 的判断

当前倾向继续细化 **A**：它的主体最少，白色通道与整体轮廓较清楚，适合建立精确矢量和光学版本。风险是偏抽象、帆形或一般软件徽记联想；下一阶段要验证其具体比例与品牌记忆，不能靠说明文字补救。

B 的质感与物件可辨性最完整，但测量/裁缝 App 的品类联想明显，当前不作为优先推荐。C 的动物身份最直接，可以探索亲和的品牌性格，但长尾、趾端与文字重量仍需精简，还需要判断角色是否符合产品气质。

本轮通过的是三组之间的方向去重，不是最终 Logo 品质、颜色或生产资产确认。生成式应用与单色版本有局部几何差异，尤其 B 的卷绕线和 C 的掌形；正式母稿和 24 px 检查在方向确认后进行。

## 显示资源

原图保留。为了直接比较，`a-preview.jpg`、`b-preview.jpg`、`c-preview.jpg` 是等比例缩略的显示副本；只做尺寸与格式导出，不修改构形或颜色。三份显示资源嵌入 `distinct-logo-options.html`，提供概览和各方向大图。

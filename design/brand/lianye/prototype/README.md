# 连页交互原型

当前版本：V5，手动截图增加起点与松手位置指导；首次接图后只收起文字，保留淡色路线。名称与 Logo 01 精密取景保持已确认方案。默认评审情境为手动，自动主路径可从原型外的情境选择器体验。

2026-10-07 用户确认整体原型方向，V5 作为后续实现的交互基线；具体视觉布局按实机体验调整。

产品只围绕截取、检查与裁剪、保存或系统分享。一个当前草稿；普通返回保留它，只在新截图会覆盖未保存内容时确认。遮盖、裁剪百分比滑条、常驻编辑工具排和成功后的确认弹层已从原型移除。

产品自动仍为默认路径。首次准备与拒绝无障碍后均可选手动：本次屏幕捕获授权后待开始；打开目标页面、找到起点，点悬浮球开始；有效首屏到达后才显示已截屏数和结束。首次用起点、终点和短文字说明向上滑动与松手稍停；第一次有效新增内容后文字收起，淡色路径继续保留。用户可关闭提示，本次不重现，截图继续；结束及接续失败时路线退出。已有学习记录的下一轮直接显示简化路线，关闭状态仅本次有效。停留、回滑和重复不增加数量；接续失败当场说明并保留可靠内容。结束先收尾已到达的有效内容，再生成共同结果并释放授权；首屏回调前结束会等原回调，不提前制造成功画面。

悬浮能力与无障碍独立。通知栏是条件可用的替代控制；两种控制都不可用时留在准备，引导开启一种。新增通知、控制未开启、首屏失败和系统中断情境。通知情境在原型中点击整个状态栏模拟下拉，蓝色屏捕标记只表示状态。悬浮授权用系统设置页、开关及返回模拟。手动帮助默认展开，自动截图厂商帮助按需展开。

现有六类安卓帮助路径直接复用：小米 / Redmi，OPPO / 一加 / realme，vivo / iQOO，华为 / 荣耀，三星，原生 / 其他安卓。产品内只展示本机分支；原型外的“模拟手机”选择器用于评审分支。应用信息入口保留，电池与 ADB 排障默认折叠。这些是项目已有厂商分组，不代表各系统最新版本均完成实机验证。

## 阅读与预览

- `lianye-interaction-prototype.html`：最终可点击片段，供对话中展示。
- `manual-swipe-guide.html`：聚焦「完整指导 → 简化路线 → 主动关闭」的局部示意。
- `review/manual-swipe-guide.png` 与 `review/manual-swipe-guide-compact.png`：局部示意的完整和简化状态。
- `review/overview.png`：当前自动路径六屏总览。
- `review/manual-overview.png`：手动兜底六屏总览。
- `review/overview-v2.png` 与 `review/prototype-v2.html`：加入手动之前的精简版本快照。
- `review/manual-overview-v3.png` 与 `review/prototype-v3.html`：原返回结束交互快照。
- `review/independent-manual-floating-audit.md`：三个独立 agent 的手动评审、V4 复核与修正记录。
- `review/overview-v1.png` 与 `review/prototype-v1.html`：原版快照，便于对照。
- `review/independent-visual-audit.md`：独立视觉评审及新版复核。
- `review/independent-ux-audit.md`：独立交互评审及新版复核。
- `review/independent-minimal-audit.md`：独立产品边界及信息评审。

旧版单页截图可能仍保留在 review 中；当前流程以新生成片段、两个总览及 V5 验收报告为准。原独立视觉、UX 和精简评审保留 V1/V2 历史范围；三个手动 agent 的复核为 V4 历史记录。通知、阻塞、失败、中断、窄屏拖动和深色状态另有 `manual-13` 至 `manual-22` 截图；`manual-23-guide-retained.png` 与 `manual-24-guide-dismissed.png` 展示连续接图后保留路线及主动关闭后继续截图。

## 构建与检查

```powershell
node design/brand/lianye/prototype/build.mjs
node --test design/brand/lianye/prototype/model.test.cjs design/brand/lianye/prototype/device-help.test.cjs
node design/brand/lianye/prototype/verify-browser.mjs
node design/brand/lianye/prototype/verify-save-race.mjs
node design/brand/lianye/prototype/verify-manual.mjs
```

`verify-host.mjs` 另检查 visualize 预览包装内的 iframe、宿主图标、操作目标与文字对比度，并生成总览。运行前用 visualize 的 `scripts/render.py` 将片段包装到 `build/lianye-prototype/host-preview.html`。QA 使用环境已安装的 Playwright、Chrome 和 Lucide，无需新增依赖。

V5 本轮运行 25 个状态/厂商路径测试、98 项自动浏览器检查、86 项手动流程检查、10 项宿主检查；浏览器无 JS 异常。证据在 `review/verification.json`、`review/manual-verification.json`、`review/host-verification.json`。局部示意另通过 8 项交互与布局检查。检查覆盖首次完整位置指导、首次接图后保留路线、连续接图、主动关闭不影响截图、本次不重现、结束清除、下一轮恢复、换侧、320/390/736 px、浅深色、通知及原有共同结果流程。窄屏终点标签越界先实际复现，再加入最小边距，用原检查验证修复。

V4 原六个状态边界测试、首屏提前结束及通知面板底部漏出问题的先复现再修复记录，保留在上一版计划与独立手动评审中。保存竞态回归在 V4 已通过，本轮未改动导出逻辑。

独立 UX 复核发现保存期间替换草稿的竞态。已先用 `verify-save-race.mjs` 实际复现失败，再增加保存期的新建守卫，并把回调绑定原草稿；修复后同一脚本通过，独立 UX agent 另行运行也通过。普通返回仍可使用，无新增弹窗。精简评审的两处收尾建议也已落实：三步帮助补回手动结束与检查结果，ADB 面板补回复制命令的模拟动作。

所有系统页面、截图内容、保存和分享均为交互模拟。手动路径由用户滚动示例页驱动计数，预览使用预置示例内容，没有执行 Android 屏幕采集或真实图像匹配。控件只在示例目标页绘制，不能据此认为正式实现可自动识别任意目标应用；正式悬浮控制应随会话可用，而不依赖这种识别。手机的通知授权与截图通知渠道是否可见、单 App 共享、控件不入图、真实末帧与持续滚动均待 Android 实机验证。此次修改范围为原型与设计文档。

# 连页交互原型评审

2026-10-06。名称与首选精修 Logo 沿用用户选择，只发展这一条方向。当前交付可点击设计原型，不修改 Android 实现。

## 查看

- `../lianye-interaction-prototype.html`：对话内交互片段，从「开始长截图」进入完整流程。
- `overview.png`：六个关键状态的完整流程图。
- `01-home.png`、`06-result.png`、`07-crop.png`、`08-mask.png`：可单独放大检查的页面。
- `15-home-320.png`、`16-home-dark.png`、`17-result-dark.png`：窄屏及深色。
- `18-host-home.png`：使用宿主 CSS、iframe 与提供的 Lucide 后的实际渲染。

情境选择位于产品画面之外：首次使用、已准备好、未保存草稿、权限被拒、截取中断、保存失败、Android 10。宿主微调只允许同方向的圆角与外观调整。

## 实际可操作

首次授权（允许/拒绝）、去截图、打开示例目标页面、移动侧边按钮、自动截取与提前结束、长图滚动/缩放/顶部底部定位、拖动或滑块裁剪、裁剪放大、移动浏览和绘制遮盖、添加/调整/移除遮盖、撤销重做、保存、失败重试、分享选择与取消、草稿恢复、新截图与完成时的未保存保护、隐藏侧边按钮及停用服务。

UI 图标是宿主 Lucide，Logo 是同一光学几何的石墨/烧橙彩色 PNG；`assets/ui-symbol-small.svg` 只把已有单色光学源的长图路径改为已确认的橙色，其余路径、线重及位置完全一致。

## 验证与边界

- 模型：6 tests passed，0 failures。
- 浏览器：63 checks passed，0 JavaScript errors；覆盖 320/390/736 px、深色、手势和键盘、延迟的宿主状态回传、重新挂载后的草稿恢复。
- 宿主预览：6 checks passed；结果页目标至少 44×44 px，浅深主题正文与次级文字对比度至少 4.5:1。
- 最终片段 68,564 bytes，包含自身样式和逻辑；不调用网络、真实捕获、授权或导出 API。

报告为 `verification.json` 与 `host-verification.json`。PNG 已人工查看，没有可见的控件溢出或遮挡。浏览器系统页是明确标注的模拟，图库导出与分享也是模拟。超长图导出分片、厂商限制、真实系统服务与手机手势仍须在应用实施阶段实机验证。

## 复现

在项目根目录执行，使用已有的本地 Playwright、Lucide 与 Chrome，不下载依赖：

```powershell
node design/brand/lianye/prototype/render-ui-mark.mjs
node design/brand/lianye/prototype/build.mjs
node --test design/brand/lianye/prototype/model.test.cjs
node design/brand/lianye/prototype/verify-browser.mjs
& 'C:\Users\wgx\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe' 'C:\Users\wgx\.codex\plugins\cache\openai-bundled\visualize\1.0.37\skills\visualize\scripts\render.py' 'E:\Documents\scroll-loom\design\brand\lianye\prototype\lianye-interaction-prototype.html' 'E:\Documents\scroll-loom\build\lianye-prototype\host-preview.html' --force
node design/brand/lianye/prototype/verify-host.mjs
```

更换机器时，设置 `LY_NODE_RUNTIME` 指向含 Playwright/Lucide 的 node_modules，设置 `LY_CHROME` 为 Chrome 可执行文件。私有 QA 的完整 HTML 放在 `build/lianye-prototype/`；对话内原型保持片段形式。

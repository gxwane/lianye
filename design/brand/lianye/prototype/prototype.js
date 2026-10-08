(function () {
  'use strict';
  const root=document.getElementById('ly-interaction-prototype');if(!root)return;
  const screen=root.querySelector('#ly-screen'),overlay=root.querySelector('#ly-overlay');
  const scenarioSelect=root.querySelector('#ly-scenario'),vendorSelect=root.querySelector('#ly-vendor');
  const M=globalThis.LianyePrototypeModel,H=globalThis.LianyeDeviceHelp,copy=v=>JSON.parse(JSON.stringify(v));
  let s=M.create(scenarioSelect.value,vendorSelect.value),editing=false,temp=null,cropOverview=false,cropHistory=[],cropIndex=0;
  let zoom=1,viewMenu=false,captureTimer=null,exportTimer=null,manualTimer=null,firstFrameTimer=null,finishTimer=null,pulseTimer=null,toastTimer=null,pendingManualPosition=null,drag=null,returnFocus=null;
  let bubbleY=245,bubbleSide='right',lastWidth=0,sequence=0,lastStamp=null;
  const instance=Math.random().toString(36).slice(2),design={radius:22,appearance:'system'};
  const esc=v=>String(v).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
  const icon=n=>`<i data-lucide="${n}" aria-hidden="true"></i>`;
  const button=(action,label,cls='ly-primary',extra='')=>`<button type="button" class="${cls}" data-action="${action}" ${extra}>${label}</button>`;
  const ib=(a,n,label,extra='')=>button(a,icon(n),'ly-icon-button',`aria-label="${label}" ${extra}`);
  const announce=text=>{root.querySelector('#ly-announcement').textContent=text;};
  const icons=()=>{globalThis.lucide?.createIcons({attrs:{width:16,height:16}});};
  function remember(){
    const api=window.openai;if(!api?.setWidgetState)return;
    const experience=copy(s);
    if(experience.draft&&JSON.stringify(experience).length>11000){experience.draft.history=[M.current(s)];experience.draft.index=0;}
    const privateContent={version:5,stamp:instance+':'+(++sequence),experience,appearance:design.appearance,zoom,bubbleY,bubbleSide};
    const value={modelContent:{product:'连页',scenario:s.scenario,vendor:s.vendor,mode:s.mode,screen:s.screen,hasDraft:!!s.draft},privateContent};
    if(JSON.stringify(value).length>15000)return;lastStamp=privateContent.stamp;
    try{api.setWidgetState(value)?.catch?.(()=>{});}catch{}
  }
  function stopTimers(){clearInterval(captureTimer);[exportTimer,manualTimer,firstFrameTimer,finishTimer,pulseTimer,toastTimer].forEach(clearTimeout);captureTimer=null;exportTimer=null;manualTimer=null;firstFrameTimer=null;finishTimer=null;pulseTimer=null;toastTimer=null;pendingManualPosition=null;}
  function article(){return `<article class="ly-article" aria-label="示例内容：周末散步指南">
    <div class="ly-article-kicker">慢一点 · 周末提案</div><h2>留一个下午，<br>沿着河边走走。</h2>
    <p class="ly-article-intro">不用安排很满。带一瓶水，穿一双舒服的鞋，把时间留给路边的风景。</p>
    <div class="ly-landscape" role="img" aria-label="河畔丘陵与落日插画"><span class="ly-sun"></span><span class="ly-hill"></span><span class="ly-hill second"></span><span class="ly-path"></span></div>
    <div class="ly-article-section"><h3><span>01</span> 从旧桥出发</h3><p>穿过桥头的小广场，沿河向东走。树荫下的石板路很平缓，适合边走边看。</p><p>第一段不必赶路。听听水声，看一看桥下的倒影，在想停下的地方停一会儿。</p></div>
    <div class="ly-article-section"><h3><span>02</span> 找一处树荫</h3><p>经过第三张长椅，路边会出现一片低矮的树林。带来的水和小点心，刚好可以在这里派上用场。</p><div class="ly-article-note">随手记：走走停停，比一路赶到终点更适合这个下午。</div></div>
    <div class="ly-article-section"><h3><span>03</span> 等一束傍晚的光</h3><p>河湾开阔处没有遮挡。太阳低下来时，水面上会出现一条长长的光带。</p><p>可以拍一张照片，也可以把手机收起来。让这一刻只是一个安静的傍晚。</p></div>
    <div class="ly-article-section"><h3><span>04</span> 慢慢走回去</h3><p>回程沿原路走，熟悉的风景换了光线，又像一条新的路。趁天色还亮，回家吃一顿简单的晚饭。</p></div>
    <p class="ly-article-end">一个下午，一段不急着走完的路。</p>
  </article>`;}
  function mini(){return `<b>周末散步指南</b><div class="ly-mini-visual"></div>${Array.from({length:6},(_,i)=>`<div class="ly-mini-lines ${i===5?'short':''}"></div>`).join('')}`;}
  function sourceHTML(scale){return `<div class="ly-preview-source" style="width:320px;height:${s.draft.height}px;transform:scale(${scale})"><div style="transform:translateY(-${s.draft.sourceOffset||0}px)">${article()}</div></div>`;}
  function draftThumb(){const c=M.current(s).crop,scale=Math.min(86/(320*c.w),126/(s.draft.height*c.h));
    return `<div class="ly-draft-thumb" aria-label="当前长图缩略图"><div class="ly-thumb-image" style="width:${320*c.w*scale}px;height:${s.draft.height*c.h*scale}px"><div class="ly-preview-clip">${sourceHTML(scale)}</div></div></div>`;}
  function home(){const d=s.draft;return `<div class="ly-page ${d?'ly-home-draft':''}">
    <header class="ly-header"><div class="ly-brand"><span class="ly-logo" role="img" aria-label="连页标识"></span>连页</div>${button('settings','帮助','ly-text-button')}</header>
    <main class="ly-content ly-home-content"><h1>${d?'继续上次截图':'一屏之外，<br>完整留下。'}</h1>
    ${d?`<div class="ly-draft-card">${draftThumb()}<div><h3>当前长图</h3><p class="ly-draft-meta">${d.frames} 屏 · ${d.saved?'已保存':'未保存'}</p></div></div>`:
      `<p class="ly-description">${s.mode==='manual'?'自己滑动':'自动滚动'}，接成一张长图。</p><div class="ly-illustration" aria-label="长图越过单屏取景边界的示意" role="img"><div class="ly-mini-page">${mini()}</div><div class="ly-frame"><span class="ly-corner a"></span><span class="ly-corner b"></span><span class="ly-corner c"></span><span class="ly-corner d"></span></div></div>`}
    ${s.captureMessage?'<p class="ly-denied" role="alert">'+esc(s.captureMessage)+'</p>':''}
    ${s.denied&&s.mode!=='manual'?'<p class="ly-denied" role="alert">截图服务未开启</p>':''}</main>
    <footer class="ly-footer">${button(d?'resume':'start',icon(d?'file-image':s.mode==='manual'?'hand':'scan-line')+(d?'继续编辑':s.mode==='manual'?'手动长截图':s.permission?'去截图':'开始长截图'))}
    ${d?button('new','新建截图','ly-text-button',`style="width:100%;margin-top:6px" ${s.exportStatus==='saving'?'disabled':''}`):s.denied&&s.mode!=='manual'?button('manual','手动截图','ly-text-button','style="width:100%;margin-top:6px"'):''}</footer></div>`;}
  function system(){return `<div class="ly-page ly-system"><header class="ly-header">${ib('system-back','arrow-left','返回连页')}<h3>无障碍 · 连页</h3></header>
    <main class="ly-content"><span class="ly-logo-app ly-logo" style="margin:18px 0"></span><h2>使用连页</h2>
    <label class="ly-system-row"><span>允许连页使用无障碍服务</span><input id="ly-system-enable" type="checkbox" ${s.permission?'checked':''} aria-label="开启连页无障碍服务"></label>
    <p class="ly-description">此服务可截取屏幕画面并执行滚动。</p></main></div>`;}
  function desktop(){return `<div class="ly-page ly-desktop"><div><div class="ly-desktop-time">9:41</div><p class="ly-description">10 月 6 日 · 星期二</p><div class="ly-app-grid">
    ${button('open-browser','<span class="ly-app-square">'+icon('globe-2')+'</span>浏览器','ly-app')}
    ${button('home','<span class="ly-logo-app"></span>连页','ly-app')}
    ${button('desktop-settings','<span class="ly-app-square">'+icon('settings')+'</span>系统设置','ly-app')}
    </div></div><div><div class="ly-desktop-hint">${s.mode==='manual'?(M.manualControl(s)==='notification'?'打开要截的页面，在通知栏点「开始」。':'打开要截的页面，用悬浮按钮开始。'):'打开页面，点侧边「截取」。'}</div>${s.mode==='manual'?button('manual-cancel','取消准备','ly-text-button','style="margin:8px auto 0"'):''}</div></div>`;}
  function manualStatus(){return s.manualEnding?'正在收尾':s.manualPhase==='taking'?'正在截取':s.manualPhase==='failed'?'未截到画面':s.manualPhase==='finishing'?'正在生成':'已截 '+s.frames+' 屏';}
  function manualTip(){
    if(s.manualPhase==='idle')return {kind:'prepare',text:'找到起点后，点「开始」。'};
    if(s.manualPhase==='failed')return {kind:'error',text:'<strong>未截到画面</strong><span>可点「重试」，或取消本次截图。</span>'};
    if(s.manualGap)return {kind:'error',text:'<strong>未能接上</strong><span>已保留前段，可点「结束」查看。</span>'};
    return null;
  }
  function manualGuide(){
    if(s.mode!=='manual'||s.screen!=='capturing'||M.manualControl(s)!=='overlay'||s.manualPhase!=='recording'||s.manualEnding||s.manualGap||s.manualGuideDismissed)return '';
    return `<div class="ly-swipe-guide ${bubbleSide==='right'?'left':'right'} ${s.manualHintSeen?'compact':''}" role="group" aria-label="滑动提示"><div class="ly-swipe-route" role="img" aria-label="参考路线：从内容区中下方向上滑至中上方，松手稍停；位置不必精确。"><span class="ly-swipe-rail"></span><span class="ly-swipe-arrow">${icon('arrow-up')}</span><span class="ly-swipe-dot end"></span><span class="ly-swipe-dot start"></span><span class="ly-swipe-caption end">到这里<br>松手稍停</span><span class="ly-swipe-caption start">从这里<br>向上滑</span></div>${button('dismiss-swipe-guide','关闭提示','ly-guide-close','aria-label="关闭滑动提示，本次不再显示"')}</div>`;
  }
  function manualButton(){
    const phase=s.manualPhase,ready=phase==='idle',failed=phase==='failed',busy=phase==='finishing'||s.manualEnding;
    return button(ready||failed?'capture':'stop',ready?icon('scan-line')+'开始':failed?icon('rotate-cw')+'重试':`<span class="ly-manual-status" aria-live="polite">${manualStatus()}</span>${busy?'':'<span class="ly-manual-divider"></span><span>结束</span>'}`,
      'ly-bubble ly-manual-button '+(ready||failed?'':'recording'),`aria-label="${ready?'开始截取，可拖动位置':failed?'重试首屏截取':busy?'正在生成长图':'结束截取，'+manualStatus()}" ${busy?'disabled':''}`);
  }
  function manualFloat(){const tip=manualTip();return `<div class="ly-manual-float ${bubbleSide}" style="top:${bubbleY}px;${bubbleSide==='left'?'left:8px':'right:8px'}"><div class="ly-manual-cluster">${manualButton()}${['idle','failed'].includes(s.manualPhase)?button('manual-cancel',icon('x'),'ly-icon-button ly-manual-cancel','aria-label="取消截图"'):''}</div>${tip?`<div class="ly-float-tip ${tip.kind} ${bubbleY>470?'above':''}" ${tip.kind==='error'?'role="alert"':''}>${tip.text}</div>`:''}</div>`;}
  function notificationCard(){const active=s.screen==='capturing',phase=s.manualPhase,busy=phase==='finishing'||s.manualEnding,failed=phase==='failed';
    return `<div class="ly-notification-shade" role="dialog" aria-modal="true" aria-label="系统通知栏"><div class="ly-notification-header"><span>通知</span>${ib('notifications-close','chevron-up','收起通知栏')}</div><section class="ly-capture-notification"><div class="ly-notification-brand"><span class="ly-logo" aria-hidden="true"></span><span>连页 · 手动长截图</span></div><h3>${active?manualStatus():'准备好了'}</h3><p>${s.manualGap?'未能接上，已保留前段。':!s.manualHintSeen?'每次向上滑半屏，稍停；截完在此点结束。':'自己滑动，连页自动拼接。'}</p><div class="ly-notification-actions">${button(active&&!failed?'stop':'capture',busy?'正在生成':active&&!failed?'结束':failed?'重试':'开始','ly-system-action',busy||s.screen==='desktop'?'disabled':'')}${!active||failed?button('manual-cancel','取消','ly-system-action'):''}</div></section>${s.screen==='desktop'?'<p class="ly-notification-guide">先打开要截的页面，再开始。</p>':''}</div>`;
  }
  function target(){const capturing=s.screen==='capturing';return `<div class="ly-page"><header class="ly-browser-header">${ib('target-back','arrow-left','返回桌面')}<div class="ly-address">${icon('lock-keyhole')}周末散步指南</div>${ib('home','house',capturing&&s.mode==='manual'?'返回连页，保留已截内容':'返回连页')}</header>
    <div class="ly-target-scroll">${article()}</div>
    ${s.mode==='manual'?(M.manualControl(s)==='overlay'?manualFloat():''):s.bubble?`<button type="button" class="ly-bubble ${capturing?'recording':''}" data-action="${capturing?'stop':'capture'}" aria-label="${capturing?'结束截取':'开始截取，可拖动位置'}" style="top:${bubbleY}px;${bubbleSide==='left'?'left:8px;right:auto':''}">${capturing?'<span class="ly-live-dot"></span><span>'+s.frames+' 屏</span>':icon('scan-line')}${capturing?'结束':'截取'}</button>`:
      `<div class="ly-hidden-button">${button('show-bubble','显示截图按钮','ly-secondary')}</div>`}${manualGuide()}</div>`;}
  function result(){const c=M.current(s).crop,d=s.draft,scale=(screen.clientWidth-32)/(320*c.w)*zoom;
    const busy=s.exportStatus==='saving',failed=s.exportStatus==='failed';
    return `<div class="ly-page"><header class="ly-header ly-result-header">${ib('draft-home','arrow-left','返回首页，保留草稿')}<h3>长图</h3>${button('view',icon('scan-search')+'查看','ly-text-button',`aria-expanded="${viewMenu}"`)}</header>
    <div class="ly-result-meta" role="${failed?'alert':'status'}">${busy?'正在保存…':failed?'保存失败，草稿已保留':d.saved?'已保存至相册':'未保存'}</div>
    ${d.interrupted?'<div class="ly-result-notice" role="status">'+icon('info')+(d.gap?'未能接上':'截图已中断')+'，已保留 '+d.frames+' 屏</div>':''}
    <main class="ly-result-area"><div class="ly-result-scroll" aria-label="长图预览，可滚动查看"><div class="ly-preview-space" style="width:${320*c.w*scale}px;height:${d.height*c.h*scale}px"><div class="ly-preview-clip">${sourceHTML(scale)}</div></div></div></main>
    ${viewMenu?`<div class="ly-view-menu" aria-label="查看长图">${button('zoom-in',icon('zoom-in')+'放大','ly-menu-item')}${button('fit',icon('maximize')+'适合宽度','ly-menu-item')}${button('top',icon('arrow-up-to-line')+'回到顶部','ly-menu-item')}${button('bottom',icon('arrow-down-to-line')+'跳至底部','ly-menu-item')}</div>`:''}
    <footer class="ly-result-actions">${button('crop',icon('crop')+'裁剪','ly-secondary',busy?'disabled':'')}${button('save',icon(busy?'loader-circle':d.saved?'check':'download')+(busy?'保存中…':d.saved?'已保存':failed?'重试':'保存图片'),'ly-primary',busy||d.saved?'disabled':'')}${button('share',icon('share-2')+'分享','ly-secondary',busy?'disabled':'')}</footer></div>`;}
  function editor(){const c=temp.crop,width=(screen.clientWidth-58)/320,scale=cropOverview?Math.min(width,(screen.clientHeight-204)/s.draft.height):width;
    return `<div class="ly-page"><header class="ly-editor-header">${button('cancel-edit','取消','ly-text-button')}<h3>裁剪</h3>${button('apply-edit','应用','ly-apply')}</header>
    <div class="ly-editor-stage ${cropOverview?'ly-overview':''}"><div class="ly-edit-image" style="width:${320*scale}px;height:${s.draft.height*scale}px" data-scale="${scale}">${sourceHTML(scale)}
    <div class="ly-crop-shade" style="left:${c.x*100}%;top:${c.y*100}%;width:${c.w*100}%;height:${c.h*100}%">${['tl','tr','bl','br'].map(corner=>`<button type="button" class="ly-crop-handle" data-corner="${corner}" aria-label="${({tl:'左上',tr:'右上',bl:'左下',br:'右下'})[corner]}裁剪角，方向键调整"></button>`).join('')}</div>
    </div></div><footer class="ly-editor-controls"><div class="ly-editor-hint">拖动四角裁剪</div><div class="ly-control-row"><div class="ly-crop-history">${cropHistory.length>1?ib('crop-undo','undo-2','撤销裁剪',cropIndex===0?'disabled':'')+ib('crop-redo','redo-2','重做裁剪',cropIndex===cropHistory.length-1?'disabled':''):''}${button('crop-reset','还原','ly-text-button')}</div>${button('crop-zoom',icon(cropOverview?'zoom-in':'maximize')+(cropOverview?'放大调整':'查看全图'),'ly-text-button')}</div></footer></div>`;}
  function pathBlock(full=false){const device=H.get(s.vendor);return `<div class="ly-device-path"><span>${esc(device.label)}</span><p>${esc(full?device.full:device.short)}</p></div>`;}
  function fullPath(){return `<details class="ly-details"><summary>完整设置路径</summary><p>${esc(H.get(s.vendor).full)}</p><p>不同系统版本的菜单名称可能略有差异。</p></details>`;}
  function settings(){return `<div class="ly-page"><header class="ly-header">${ib('home','arrow-left','返回首页')}<h3 style="flex:1">帮助与设置</h3></header><main class="ly-content">
    <label class="ly-setting-row"><span>侧边截图按钮</span><input type="checkbox" id="ly-bubble-toggle" ${s.bubble?'checked':''} ${s.permission?'':'disabled'} aria-label="显示侧边截图按钮"></label>
    ${button('disable','停用截图服务'+icon('chevron-right'),'ly-setting-row',s.permission?'':'disabled')}
    <p class="ly-inline-hint">${s.mode==='manual'?'手动截图由你滑动，点「结束」生成长图。':'隐藏按钮后，截图服务仍开启。'}</p>
    ${button('help','如何截图 / 开启帮助'+icon('chevron-right'),'ly-setting-row')}
    <details class="ly-details"><summary>隐私与关于</summary><p>图片在本机处理，无需账号，不联网。自动截图使用无障碍；手动截图使用本次屏幕捕获。</p><p>连页 · 开源 · MIT 许可</p></details></main></div>`;}
  function help(){return `<div class="ly-page"><header class="ly-header">${ib('settings','arrow-left','返回帮助与设置')}<h3 style="flex:1">如何截图</h3></header><main class="ly-content">
    <ol class="ly-steps">${s.mode==='manual'?'<li>允许本次屏幕捕获。</li><li>打开要截的页面，点「开始」。<br>自己滑动，截完点「结束」。</li>':'<li>打开要截取的页面。</li><li>点「截取」，随时点「结束」。</li>'}<li>检查长图，保存或分享。</li></ol>
    ${s.mode==='manual'?button('manual','开始手动截图','ly-secondary')+'<details class="ly-details ly-auto-help"><summary>自动截图设置</summary>':''}
    ${pathBlock()}${fullPath()}${button('help-system','前往设置','ly-secondary')}${button('restricted','无法开启？','ly-text-button','style="width:100%"')}
    ${s.mode==='manual'?'</details>':button('manual','手动截图','ly-text-button','style="width:100%"')}
    <details class="ly-details ly-advanced"><summary>仍有问题</summary>${button('app-info','应用信息'+icon('chevron-right'),'ly-setting-row')}${button('battery','电池与后台运行'+icon('chevron-right'),'ly-setting-row')}${button('adb','ADB 排障'+icon('chevron-right'),'ly-setting-row')}</details></main></div>`;}
  function appInfo(){return `<div class="ly-page ly-system"><header class="ly-header">${ib('help','arrow-left','返回帮助')}<h3>应用信息</h3></header><main class="ly-content"><span class="ly-logo-app ly-logo" style="margin:18px 0"></span><h2>连页</h2><details class="ly-details"><summary>更多</summary>${button('allow-restricted','允许受限制的设置','ly-setting-row')}</details>${button('help-system','返回无障碍设置','ly-secondary','style="margin-top:24px"')}</main></div>`;}
  function battery(){return `<div class="ly-page ly-system"><header class="ly-header">${ib('help','arrow-left','返回帮助')}<h3>电池设置</h3></header><main class="ly-content"><h2>连页</h2><label class="ly-system-row"><span>允许后台运行</span><input type="checkbox" aria-label="允许连页后台运行"></label><p class="ly-description">${esc(H.get(s.vendor).label)}的选项名称可能随系统版本变化。</p></main></div>`;}
  function sheetFrame(title,body,actions=''){return `<div class="ly-scrim"><section class="ly-sheet" role="dialog" aria-modal="true" aria-label="${title}"><div class="ly-sheet-handle"></div><div class="ly-sheet-title"><h2>${title}</h2>${ib('close-sheet','x','关闭')}</div>${body}${actions}</section></div>`;}
  function sheet(){switch(s.sheet){
    case 'notifications':return notificationCard();
    case 'control':return sheetFrame('准备手动截图','<p class="ly-description">需要悬浮球或通知来开始和结束。</p>',button('enable-overlay','开启悬浮球')+button('enable-notifications','使用通知栏','ly-secondary'));
    case 'overlay-permission':return `<div class="ly-system-settings ly-system" role="dialog" aria-modal="true" aria-label="悬浮窗系统设置"><header class="ly-header">${ib('control-settings-back','arrow-left','返回连页准备')}<h3>显示在其他应用上层</h3></header><main class="ly-content"><span class="ly-logo-app ly-logo"></span><h2>连页</h2><label class="ly-system-row"><span>允许显示在其他应用上层</span><input type="checkbox" id="ly-manual-overlay" ${s.overlayAllowed?'checked':''} aria-label="允许连页显示悬浮球"></label><p class="ly-description">用于显示开始和结束按钮。</p></main></div>`;
    case 'notification-permission':return `<div class="ly-scrim"><section class="ly-system-dialog" role="dialog" aria-modal="true" aria-label="通知授权"><h2>允许连页发送通知？</h2><p>在通知栏控制本次截图。</p><div class="ly-system-actions">${button('control-deny','取消','ly-system-action')}${button('notifications-allow','允许','ly-system-action')}</div></section></div>`;
    case 'prepare':return sheetFrame('开启自动截图','<p class="ly-description">开启无障碍后自动滚动，图片在本机处理。</p>'+pathBlock(),button('to-system','前往设置')+button('manual','<span>手动截图</span><small>自己滑动，无需无障碍</small>','ly-secondary ly-manual-choice')+button('restricted','找不到入口？','ly-text-button','style="width:100%"'));
    case 'consent':return `<div class="ly-scrim"><section class="ly-system-dialog" role="dialog" aria-modal="true" aria-label="系统授权"><h2>允许连页使用此服务？</h2><p>连页将能够截取屏幕画面并执行滚动。请仅为信任的应用开启。</p><div class="ly-system-actions">${button('system-deny','取消','ly-system-action')}${button('system-allow','允许','ly-system-action')}</div></section></div>`;
    case 'projection':return `<div class="ly-scrim"><section class="ly-system-dialog" role="dialog" aria-modal="true" aria-label="屏幕捕获授权"><h2>开始屏幕捕获？</h2><p>连页可以访问本次共享的屏幕内容。</p><div class="ly-system-actions">${button('projection-deny','取消','ly-system-action')}${button('projection-allow','开始','ly-system-action')}</div></section></div>`;
    case 'restricted':return sheetFrame('开启帮助',pathBlock()+fullPath()+'<p class="ly-description">开关受限时：应用信息 → 更多 → 允许受限制的设置（若有）。</p>',button('app-info','打开应用信息')+button('help-system','返回无障碍设置','ly-secondary'));
    case 'protect':return sheetFrame('当前长图未保存','<p class="ly-description">新截图会替换当前草稿。</p>',button('save-new','保存并新建')+button('discard-new','放弃并新建','ly-secondary'));
    case 'share':return sheetFrame('分享图片',`<div class="ly-share-apps">${[['message-circle','聊天'],['mail','邮件'],['folder','文件']].map(([n,name])=>button('share-to','<span>'+icon(n)+'</span>'+name,'',`aria-label="分享到${name}"`)).join('')}</div>`);
    case 'adb':return sheetFrame('ADB 排障','<p class="ly-description">连接电脑后，可用命令解除受限制设置。具体包名由安装版本提供。</p><pre class="ly-command"><code>adb shell appops set org.scrollloom ACCESS_RESTRICTED_SETTINGS allow</code></pre>',button('copy-adb','复制命令','ly-secondary'));
    default:return '';
  }}
  function render({focus=true}={}){
    const oldSheet=overlay.querySelector('[role="dialog"]')?.getAttribute('aria-label');
    const active=document.activeElement,focusKey=active?.dataset.action,corner=active?.dataset.corner;
    const scroll=screen.querySelector('.ly-result-scroll')?.scrollTop||screen.querySelector('.ly-editor-stage')?.scrollTop||0;
    const targetScroll=screen.querySelector('.ly-target-scroll')?.scrollTop||0;
    const pages={home,system,desktop,target,capturing:target,result,settings,help,'app-info':appInfo,battery};
    screen.innerHTML=editing?editor():(pages[s.screen]||home)();overlay.innerHTML=sheet();
    root.querySelector('.ly-statusbar>span').innerHTML='9:41'+(s.mode==='manual'&&s.projection?'<span class="ly-projection-indicator" role="img" aria-label="系统屏幕捕获进行中">'+icon('cast')+'</span>':'');
    root.querySelector('.ly-notification-toggle')?.remove();if(s.mode==='manual'&&s.projection&&M.manualControl(s)==='notification')root.querySelector('.ly-statusbar').insertAdjacentHTML('beforeend',button('notifications-open','','ly-notification-toggle','aria-label="下拉通知栏（原型用点击模拟）"'));
    if(s.draft&&!editing){const c=M.current(s).crop;screen.querySelectorAll('.ly-preview-source').forEach(el=>{const scale=Number(el.style.transform.match(/scale\(([^)]+)/)?.[1]||1);el.style.left=-c.x*320*scale+'px';el.style.top=-c.y*s.draft.height*scale+'px';});}
    const viewport=screen.querySelector(editing?'.ly-editor-stage':'.ly-result-scroll');if(viewport)viewport.scrollTop=scroll;
    if(s.screen==='capturing'){const el=screen.querySelector('.ly-target-scroll');el.scrollTop=s.mode==='manual'?targetScroll:(s.frames-1)*310;}
    icons();
    const dialog=overlay.querySelector('[role="dialog"]');
    if(focus&&dialog&&oldSheet!==dialog.getAttribute('aria-label')){returnFocus=focusKey||returnFocus;dialog.querySelector('button')?.focus();}
    else if(focus&&corner)screen.querySelector(`[data-corner="${corner}"]`)?.focus();
    else if(focus&&focusKey)root.querySelector(`[data-action="${focusKey}"]`)?.focus();
  }
  function closeSheet(){const was=s.sheet;if(s.mode==='manual'&&['control','overlay-permission','notification-permission'].includes(was))M.cancelManual(s);else{s.sheet=null;if(['prepare','projection'].includes(was))s.pendingStart=false;}render();if(returnFocus)root.querySelector(`[data-action="${returnFocus}"]`)?.focus();returnFocus=null;remember();}
  function goCapture(){
    if(s.mode==='manual'){if(!s.projection)M.requestManual(s);else{s.pendingStart=false;s.screen='desktop';s.sheet=null;}}
    else if(!s.permission){s.pendingStart=true;s.sheet='prepare';}
    else if(s.legacy&&!s.projection){s.pendingStart=true;s.sheet='projection';}
    else{s.pendingStart=false;s.bubble=true;s.screen='desktop';s.sheet=null;}
    render();
  }
  function beginManual(){if(s.exportStatus==='saving')return;s.mode='manual';s.projection=false;if(M.needsProtection(s)){s.sheet='protect';render();}else{if(s.draft)M.clearDraft(s);goCapture();}}
  function refreshManual(pulse=false){
    if(drag?.type==='bubble')return;
    const focused=screen.querySelector('.ly-manual-float')?.contains(document.activeElement);
    const old=screen.querySelector('.ly-manual-float');if(old)old.outerHTML=manualFloat();
    const previousGuide=screen.querySelector('.ly-swipe-guide'),nextGuide=manualGuide();
    if(previousGuide){if(nextGuide)previousGuide.outerHTML=nextGuide;else previousGuide.remove();}
    else if(nextGuide)screen.querySelector('.ly-target-scroll')?.parentElement.insertAdjacentHTML('beforeend',nextGuide);
    if(s.sheet==='notifications')overlay.innerHTML=notificationCard();icons();
    if(focused)screen.querySelector('.ly-manual-button')?.focus();
    if(pulse){const status=screen.querySelector('.ly-manual-status');status?.classList.add('updated');clearTimeout(pulseTimer);pulseTimer=setTimeout(()=>status?.classList.remove('updated'),500);}
  }
  function manualToast(text){
    if(M.manualControl(s)!=='notification'||s.sheet)return;
    overlay.innerHTML='<div class="ly-system-toast" role="status"><span class="ly-logo" aria-hidden="true"></span><span>'+esc(text)+'</span></div>';clearTimeout(toastTimer);
    toastTimer=setTimeout(()=>{toastTimer=null;overlay.querySelector('.ly-system-toast')?.remove();},4000);
  }
  function confirmFirstFrame(){
    const success=!(s.scenario==='manual-first-failure'&&!s.failConsumed);s.failConsumed=true;
    if(!M.confirmManualFrame(s,success))return;if(s.manualEnding){finishCapture(false,true);return;}refreshManual();announce(success?'已截 1 屏':'未截到画面，可以重试');manualToast(success?'已截 1 屏，截完在通知栏点结束。':'未截到画面，请在通知栏重试。');remember();
  }
  function flushManualScroll(){
    clearTimeout(manualTimer);manualTimer=null;if(pendingManualPosition===null)return;
    const position=pendingManualPosition;pendingManualPosition=null;const frames=s.frames,gap=s.manualGap;
    M.observeManualScroll(s,position);refreshManual(s.frames!==frames);
    if(s.manualGap&&!gap){announce('未能接上，已保留前段，可以结束查看');manualToast('未能接上，已保留前段。');}else if(s.frames!==frames)announce('已截 '+s.frames+' 屏');remember();
  }
  function finishCapture(interrupted=false,confirmed=false){
    if(s.mode==='manual'&&(s.manualPhase==='finishing'||s.manualEnding&&!confirmed&&!interrupted))return;
    clearInterval(captureTimer);captureTimer=null;
    if(s.mode==='manual'){
      if(!interrupted&&s.manualPhase==='taking'){s.manualEnding=true;refreshManual();remember();return;}
      if(!interrupted)flushManualScroll();else{clearTimeout(manualTimer);manualTimer=null;pendingManualPosition=null;}
      clearTimeout(firstFrameTimer);firstFrameTimer=null;
      if(!s.frames){M.finishCapture(s,interrupted);render();remember();return;}
      const session=s;s.manualPhase='finishing';s.manualEnding=false;s.sheet=s.sheet==='notifications'?'notifications':null;if(interrupted)s.projection=false;render();remember();
      finishTimer=setTimeout(()=>{finishTimer=null;if(s!==session)return;M.finishCapture(s,interrupted);zoom=1;viewMenu=false;render();announce(interrupted?'已保留有效画面':'长图已生成');remember();},350);return;
    }
    M.finishCapture(s,interrupted);zoom=1;viewMenu=false;render();announce(interrupted?'已保留有效画面':'长图已生成');remember();
  }
  function startCapture(){
    const focused=screen.querySelector('.ly-manual-button')===document.activeElement;
    const position=screen.querySelector('.ly-target-scroll')?.scrollTop||0;if(!M.startCapture(s,position))return;pendingManualPosition=null;render();if(focused)screen.querySelector('.ly-manual-button')?.focus();remember();
    if(s.mode==='manual'){const session=s;firstFrameTimer=setTimeout(()=>{firstFrameTimer=null;if(s!==session)return;confirmFirstFrame();},320);return;}
    captureTimer=setInterval(()=>{M.stepCapture(s);if(s.scenario==='interrupt'&&s.frames===2)finishCapture(true);else if(s.frames===4)finishCapture();else{render({focus:false});remember();}},850);
  }
  function replaceDraft(){if(s.exportStatus==='saving')return;M.clearDraft(s);editing=false;zoom=1;viewMenu=false;goCapture();}
  function save(nextNew=false){
    if(!s.draft||s.exportStatus==='saving'||s.draft.saved)return;
    const exportingState=s,exportingDraft=s.draft;
    s.sheet=null;s.exportStatus='saving';if(nextNew)s.screen='result';render();remember();
    exportTimer=setTimeout(()=>{if(s!==exportingState||s.draft!==exportingDraft)return;const success=!(s.scenario==='savefail'&&!s.failConsumed);s.failConsumed=true;M.exportResult(s,'save',success);exportTimer=null;
      if(success&&nextNew)replaceDraft();else render();announce(success?'已保存至相册':'保存失败，草稿已保留');remember();},700);
  }
  function openEdit(){editing=true;temp=M.current(s);cropOverview=false;cropHistory=s.draft.history.map(copy);cropIndex=s.draft.index;viewMenu=false;render();}
  function pushCrop(){const clean=M.cleanEdit(temp);if(JSON.stringify(clean)!==JSON.stringify(cropHistory[cropIndex])){cropHistory=cropHistory.slice(0,cropIndex+1);cropHistory.push(copy(clean));cropIndex++;}temp=clean;}
  function finishEdit(apply){if(apply&&JSON.stringify(temp)!==JSON.stringify(M.current(s))){M.commit(s,temp);s.exportStatus='idle';}editing=false;temp=null;render();screen.querySelector('[data-action="crop"]')?.focus();}
  function updateCrop(){const c=temp.crop,node=screen.querySelector('.ly-crop-shade');if(node)Object.assign(node.style,{left:c.x*100+'%',top:c.y*100+'%',width:c.w*100+'%',height:c.h*100+'%'});}
  function moveCorner(corner,dx,dy,original){const right=original.x+original.w,bottom=original.y+original.h,c={...original};
    if(corner.includes('l')){c.x=Math.max(0,Math.min(right-.1,original.x+dx));c.w=right-c.x;}else c.w=Math.max(.1,Math.min(1-c.x,original.w+dx));
    if(corner.includes('t')){c.y=Math.max(0,Math.min(bottom-.1,original.y+dy));c.h=bottom-c.y;}else c.h=Math.max(.1,Math.min(1-c.y,original.h+dy));
    temp=M.cleanEdit({crop:c});updateCrop();
  }
  root.addEventListener('click',event=>{
    const el=event.target.closest('[data-action]');if(!el||el.disabled||drag?.moved)return;const a=el.dataset.action;
    switch(a){
      case 'start':goCapture();break;
      case 'manual':beginManual();break;
      case 'resume':s.screen='result';s.sheet=null;render();break;
      case 'home':if(s.screen==='capturing'&&s.mode==='manual'){finishCapture(true);break;}if(s.screen==='capturing')finishCapture(true);if(s.mode==='manual')M.cancelManual(s);s.screen='home';s.sheet=null;render();break;
      case 'draft-home':s.screen='home';viewMenu=false;render();break;
      case 'new':if(s.exportStatus==='saving')break;if(M.needsProtection(s)){s.sheet='protect';render();}else replaceDraft();break;
      case 'save-new':save(true);break;
      case 'discard-new':replaceDraft();break;
      case 'to-system':s.sheet=null;s.screen='system';render();break;
      case 'help-system':s.mode='auto';s.sheet=null;s.screen='system';render();break;
      case 'system-back':if(s.pendingStart&&s.permission)goCapture();else{s.screen='home';render();}break;
      case 'system-allow':{const pending=s.pendingStart;M.grant(s,true);s.pendingStart=pending;s.screen='system';render();break;}
      case 'system-deny':M.grant(s,false);s.pendingStart=false;render();break;
      case 'projection-allow':if(s.mode==='manual'){M.grantProjection(s,true);render();}else{s.projection=true;goCapture();}break;
      case 'projection-deny':if(s.mode==='manual')M.grantProjection(s,false);else{s.sheet=null;s.pendingStart=false;s.screen='home';}render();break;
      case 'enable-overlay':s.sheet='overlay-permission';render();break;
      case 'enable-notifications':s.sheet='notification-permission';render();break;
      case 'control-deny':s.sheet='control';render();break;
      case 'control-settings-back':s.sheet=M.manualControl(s)?null:'control';s.screen=M.manualControl(s)?'desktop':'home';render();break;
      case 'notifications-allow':s.notificationsAllowed=true;s.sheet=null;s.screen='desktop';render();break;
      case 'notifications-open':s.sheet='notifications';render();break;
      case 'notifications-close':s.sheet=null;render();break;
      case 'manual-cancel':stopTimers();M.cancelManual(s);render();break;
      case 'dismiss-swipe-guide':s.manualGuideDismissed=true;refreshManual();screen.querySelector('.ly-manual-button')?.focus();announce('滑动提示已关闭，截图继续');break;
      case 'close-sheet':closeSheet();return;
      case 'restricted':s.sheet='restricted';render();break;
      case 'desktop-settings':s.screen='system';s.pendingStart=false;render();break;
      case 'open-browser':s.screen='target';render();break;
      case 'target-back':if(s.screen==='capturing')finishCapture(s.mode==='manual'?s.manualGap:true);else{s.screen='desktop';render();}break;
      case 'show-bubble':s.bubble=true;render();break;
      case 'capture':startCapture();break;
      case 'stop':finishCapture();break;
      case 'crop':openEdit();break;
      case 'cancel-edit':finishEdit(false);break;
      case 'apply-edit':finishEdit(true);break;
      case 'crop-zoom':cropOverview=!cropOverview;render();screen.querySelector('.ly-editor-stage').scrollTop=0;break;
      case 'crop-reset':temp={crop:{x:0,y:0,w:1,h:1}};pushCrop();render();break;
      case 'crop-undo':if(cropIndex>0)temp=copy(cropHistory[--cropIndex]);render();break;
      case 'crop-redo':if(cropIndex<cropHistory.length-1)temp=copy(cropHistory[++cropIndex]);render();break;
      case 'view':viewMenu=!viewMenu;render();break;
      case 'zoom-in':zoom=Math.min(2.5,zoom+.5);viewMenu=false;render();break;
      case 'fit':zoom=1;viewMenu=false;render();break;
      case 'top':case 'bottom':viewMenu=false;render();{const e=screen.querySelector('.ly-result-scroll');e.scrollTop=a==='top'?0:e.scrollHeight;}break;
      case 'save':save();break;
      case 'share':s.sheet='share';render();break;
      case 'share-to':M.exportResult(s,'share',true);s.sheet=null;render();announce('图片已交给所选应用');break;
      case 'settings':s.screen='settings';s.sheet=null;render();break;
      case 'disable':M.disable(s);render();announce('截图服务已停用，草稿已保留');break;
      case 'help':s.screen='help';s.sheet=null;render();break;
      case 'app-info':s.screen='app-info';s.sheet=null;render();break;
      case 'allow-restricted':announce('模拟系统验证；返回无障碍设置检查开关');break;
      case 'battery':s.screen='battery';s.sheet=null;render();break;
      case 'adb':s.sheet='adb';render();break;
      case 'copy-adb':el.textContent='已复制';announce('命令已复制（模拟）');break;
    }remember();
  });
  root.addEventListener('change',event=>{const el=event.target;
    if(el===scenarioSelect){stopTimers();s=M.create(el.value,vendorSelect.value);editing=false;temp=null;viewMenu=false;zoom=1;render();remember();}
    else if(el===vendorSelect){s.vendor=el.value;render();remember();}
    else if(el.id==='ly-system-enable'){if(el.checked){s.sheet='consent';render();}else{M.disable(s);render();}remember();}
    else if(el.id==='ly-bubble-toggle'){s.bubble=el.checked;remember();}
    else if(el.id==='ly-manual-overlay'){s.overlayAllowed=el.checked;remember();}
  });
  root.addEventListener('scroll',event=>{const el=event.target;if(!el.classList?.contains('ly-target-scroll')||s.mode!=='manual'||s.screen!=='capturing')return;
    if(s.manualPhase!=='recording'||s.manualGap||s.manualEnding)return;
    clearTimeout(manualTimer);const session=s;pendingManualPosition=el.scrollTop;
    manualTimer=setTimeout(()=>{manualTimer=null;if(s!==session||s.screen!=='capturing')return;flushManualScroll();if(s.scenario==='manual-interrupt'&&s.frames===2)finishCapture(true);},180);
  },true);
  root.addEventListener('pointerdown',event=>{if(event.button!==0)return;
    const bubble=event.target.closest('.ly-bubble');if(bubble){drag={type:'bubble',node:bubble,startX:event.clientX,startY:event.clientY,y:bubbleY,moved:false};bubble.setPointerCapture(event.pointerId);return;}
    const corner=event.target.closest('[data-corner]');if(corner&&editing){drag={type:'crop',node:corner,corner:corner.dataset.corner,original:{...temp.crop},startX:event.clientX,startY:event.clientY,rect:screen.querySelector('.ly-edit-image').getBoundingClientRect(),moved:false};corner.setPointerCapture(event.pointerId);event.preventDefault();}
  });
  root.addEventListener('pointermove',event=>{if(!drag)return;
    if(drag.type==='bubble'){const dx=event.clientX-drag.startX,dy=event.clientY-drag.startY;if(Math.abs(dx)+Math.abs(dy)>6)drag.moved=true;
      if(drag.moved){bubbleY=Math.max(70,Math.min(590,drag.y+dy));bubbleSide=event.clientX<screen.getBoundingClientRect().left+screen.clientWidth/2?'left':'right';const floating=drag.node.closest('.ly-manual-float')||drag.node;Object.assign(floating.style,{top:bubbleY+'px',left:bubbleSide==='left'?'8px':'auto',right:bubbleSide==='right'?'8px':'auto'});if(floating.classList.contains('ly-manual-float')){floating.classList.toggle('left',bubbleSide==='left');floating.classList.toggle('right',bubbleSide==='right');floating.querySelector('.ly-float-tip')?.classList.toggle('above',bubbleY>470);const guide=screen.querySelector('.ly-swipe-guide');guide?.classList.toggle('left',bubbleSide==='right');guide?.classList.toggle('right',bubbleSide==='left');}}}
    else{drag.moved=true;moveCorner(drag.corner,(event.clientX-drag.startX)/drag.rect.width,(event.clientY-drag.startY)/drag.rect.height,drag.original);}
  });
  function endDrag(){if(!drag)return;const old=drag;if(old.type==='crop'&&old.moved){pushCrop();render();}setTimeout(()=>{drag=null;if(s.mode==='manual')refreshManual();remember();},0);}
  root.addEventListener('pointerup',endDrag);root.addEventListener('pointercancel',endDrag);
  root.addEventListener('keydown',event=>{
    const corner=event.target.dataset.corner;if(corner&&editing&&['ArrowLeft','ArrowRight','ArrowUp','ArrowDown'].includes(event.key)){
      event.preventDefault();moveCorner(corner,event.key==='ArrowLeft'?-.01:event.key==='ArrowRight'?.01:0,event.key==='ArrowUp'?-.01:event.key==='ArrowDown'?.01:0,{...temp.crop});pushCrop();render();remember();return;
    }
    if(event.key==='Escape'){if(s.sheet){event.preventDefault();closeSheet();}else if(editing){event.preventDefault();finishEdit(false);remember();}else if(viewMenu){viewMenu=false;render();remember();}}
    if(event.key==='Tab'&&s.sheet){const controls=[...overlay.querySelectorAll('button:not(:disabled),input:not(:disabled),summary')].filter(e=>e.getClientRects().length);const first=controls[0],last=controls.at(-1);
      if(event.shiftKey&&document.activeElement===first){event.preventDefault();last.focus();}else if(!event.shiftKey&&document.activeElement===last){event.preventDefault();first.focus();}}
  });
  new ResizeObserver(()=>{const width=screen.clientWidth;if(Math.abs(width-lastWidth)>1){lastWidth=width;if(s.screen==='result'||editing)render({focus:false});}}).observe(screen);
  function applyDesign(){root.style.setProperty('--ly-radius',design.radius+'px');root.style.colorScheme=design.appearance==='system'?'inherit':design.appearance;}
  function restore(value){const state=value?.privateContent;if(!state||![2,3,4,5].includes(state.version)||state.stamp===lastStamp||state.stamp?.startsWith(instance+':'))return;
    const restored=state.experience;if(!restored||![...scenarioSelect.options].some(o=>o.value===restored.scenario)||!H.valid(restored.vendor))return;
    try{const next=copy(restored);next.mode=next.mode==='manual'?'manual':'auto';next.manualPosition=Number.isFinite(next.manualPosition)?next.manualPosition:0;next.manualOrigin=Number.isFinite(next.manualOrigin)?next.manualOrigin:0;next.manualGap=!!next.manualGap;next.manualHintSeen=!!next.manualHintSeen;next.manualGuideDismissed=!!next.manualGuideDismissed;next.overlayAllowed=next.overlayAllowed!==false;next.notificationsAllowed=!!next.notificationsAllowed;next.manualPhase=['idle','taking','recording','failed','finishing'].includes(next.manualPhase)?next.manualPhase:'idle';if(next.draft){if(!Array.isArray(next.draft.history)||!next.draft.history.length)return;next.draft.history=next.draft.history.map(M.cleanEdit);next.draft.index=Math.max(0,Math.min(next.draft.history.length-1,Math.round(next.draft.index)));next.draft.frames=Math.max(1,Math.min(4,next.draft.frames));next.draft.height=540+(next.draft.frames-1)*360;}
      stopTimers();s=next;s.sheet=null;editing=false;temp=null;viewMenu=false;s.pendingStart=false;
      if(s.screen==='capturing')M.finishCapture(s,true);if(s.exportStatus==='saving')s.exportStatus='idle';
      if(s.mode==='manual'){s.projection=false;s.manualPhase='idle';s.manualEnding=false;if(['desktop','target'].includes(s.screen))s.screen='home';}
      if(!['home','system','desktop','target','result','settings','help','app-info','battery'].includes(s.screen)||s.screen==='result'&&!s.draft)s.screen='home';
      M.refreshSaved(s);scenarioSelect.value=s.scenario;vendorSelect.value=s.vendor;
      if(['system','light','dark'].includes(state.appearance))design.appearance=state.appearance;
      zoom=Math.max(1,Math.min(2.5,state.zoom||1));bubbleY=Math.max(70,Math.min(590,state.bubbleY||245));bubbleSide=state.bubbleSide==='left'?'left':'right';lastStamp=state.stamp;applyDesign();render({focus:false});
    }catch{}
  }
  render();applyDesign();restore(window.openai?.widgetState);
  window.addEventListener('openai:set_globals',event=>{if(event.detail?.globals?.widgetState)restore(event.detail.globals.widgetState);});
  if(globalThis.Tweak){const tweak=new Tweak({container:root,onChange:applyDesign});tweak.addSlider(design,'radius',{label:'内容圆角',min:12,max:28,step:2,unit:'px',reference:'--ly-radius'});tweak.addSelect(design,'appearance',{label:'外观',options:[{label:'跟随环境',value:'system'},{label:'浅色',value:'light'},{label:'深色',value:'dark'}]});}
})();

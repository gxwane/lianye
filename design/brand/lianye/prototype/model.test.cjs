const { test } = require('node:test');
const assert = require('node:assert/strict');
const M = require('./model.cjs');

test('拒绝授权不启用服务，显隐与停用独立', () => {
  const s = M.create('first');
  M.grant(s, false);
  assert.equal(s.permission, false);
  M.grant(s, true);
  s.bubble = false;
  assert.equal(s.permission, true);
  M.disable(s);
  assert.equal(s.permission, false);
  assert.equal(s.bubble, false);
});
test('中断保留有效部分，手动结束也有结果', () => {
  const s = M.create('interrupt');
  M.startCapture(s); M.stepCapture(s); M.finishCapture(s, true);
  assert.equal(s.draft.frames, 2);
  assert.equal(s.draft.interrupted, true);
  assert.equal(s.screen, 'result');
  const t = M.create('ready'); M.startCapture(t); M.finishCapture(t);
  assert.equal(t.draft.frames, 1);
});
test('编辑历史隔离、撤销重做及新分支', () => {
  const s = M.create('draft');
  const next = M.current(s); next.crop.y = .1; next.crop.h = .8;
  assert.equal(M.current(s).crop.y, 0);
  M.commit(s, next);
  assert.equal(M.current(s).crop.y, .1);
  M.undo(s); assert.equal(M.current(s).crop.y, 0);
  M.redo(s); assert.equal(M.current(s).crop.y, .1);
  M.undo(s);
  const nextCrop=M.current(s);nextCrop.crop.x=.2;nextCrop.crop.w=.7;
  M.commit(s,nextCrop);M.redo(s);
  assert.equal(s.draft.index, 1);
  assert.equal(s.draft.history.length, 2);
  assert.equal(M.current(s).crop.x,.2);
});
test('保存失败不破坏图，重试成功；编辑重新标记未保存', () => {
  const s = M.create('savefail');
  const before = JSON.stringify(s.draft.history);
  M.exportResult(s, 'save', false);
  assert.equal(s.draft.saved, false);
  assert.equal(JSON.stringify(s.draft.history), before);
  M.exportResult(s, 'save', true);
  assert.equal(s.draft.saved, true);
  const edit=M.current(s);edit.crop.y=.1;edit.crop.h=.9;
  M.commit(s, edit);
  assert.equal(s.draft.saved, false);
});
test('取消分享和分享交接都不会自动保存至相册', () => {
  const s = M.create('draft');
  assert.equal(s.draft.shared, false);
  M.exportResult(s, 'share', true);
  assert.equal(s.draft.shared, true);
  assert.equal(s.draft.saved, false);
  assert.equal(M.needsProtection(s), true);
});
test('越界编辑收敛到有效边界，草稿可清理', () => {
  const s = M.create('draft');
  M.commit(s,{crop:{x:-1,y:.95,w:2,h:3}});
  const e = M.current(s);
  assert.equal(e.crop.x, 0);
  assert.ok(e.crop.y + e.crop.h <= 1);
  M.clearDraft(s);
  assert.equal(s.draft, null);
  assert.equal(s.screen, 'home');
});
test('回到已经保存的裁剪版本不再要求重存',()=>{
  const s=M.create('draft');M.exportResult(s,'save',true);
  const next=M.current(s);next.crop.y=.1;next.crop.h=.9;M.commit(s,next);
  assert.equal(M.needsProtection(s),true);
  M.undo(s);assert.equal(s.draft.saved,true);assert.equal(M.needsProtection(s),false);
  M.redo(s);assert.equal(s.draft.saved,false);
});
test('当前编辑模型只处理裁剪',()=>{
  const edit=M.cleanEdit({crop:{x:0,y:0,w:1,h:1},masks:[{x:0,y:0,w:1,h:1}]});
  assert.deepEqual(Object.keys(edit),['crop']);
});
test('手动截取只获取本次捕获，拒绝后不启用无障碍',()=>{
  const s=M.create('denied');M.requestManual(s);
  assert.equal(s.mode,'manual');assert.equal(s.sheet,'projection');assert.equal(s.permission,false);
  assert.equal(M.startCapture(s),false);
  M.grantProjection(s,false);assert.equal(s.screen,'home');assert.equal(s.projection,false);assert.equal(s.permission,false);
  M.requestManual(s);M.grantProjection(s,true);assert.equal(s.screen,'desktop');assert.equal(s.permission,false);assert.equal(s.bubble,false);
  assert.equal(M.startCapture(s),true);assert.equal(s.frames,0);M.confirmManualFrame(s,true);assert.equal(s.frames,1);
});
test('手动仅稳定滚动推进，重复与回滚不重复拼接',()=>{
  const s=M.create('denied');M.requestManual(s);M.grantProjection(s,true);M.startCapture(s);M.confirmManualFrame(s,true);
  M.observeManualScroll(s,0);assert.equal(s.frames,1);
  M.observeManualScroll(s,310);assert.equal(s.frames,2);
  M.observeManualScroll(s,310);M.observeManualScroll(s,100);assert.equal(s.frames,2);
  M.observeManualScroll(s,620);assert.equal(s.frames,3);
  M.observeManualScroll(s,930);assert.equal(s.frames,4);assert.equal(s.manualGap,false);
});
test('手动缺少重叠保留可靠部分，结束后需重新捕获授权',()=>{
  const s=M.create('denied');M.requestManual(s);M.grantProjection(s,true);M.startCapture(s);M.confirmManualFrame(s,true);
  M.observeManualScroll(s,310);M.observeManualScroll(s,980);
  assert.equal(s.manualGap,true);assert.equal(s.frames,2);
  M.observeManualScroll(s,1000);assert.equal(s.frames,2);
  M.finishCapture(s,s.manualGap);assert.equal(s.draft.frames,2);assert.equal(s.draft.gap,true);assert.equal(s.projection,false);assert.equal(s.permission,false);
  M.clearDraft(s);M.requestManual(s);assert.equal(s.sheet,'projection');assert.equal(s.projection,false);
});
test('手动模式不得按自动计时器步进',()=>{
  const s=M.create('denied');M.requestManual(s);M.grantProjection(s,true);M.startCapture(s);M.confirmManualFrame(s,true);M.stepCapture(s);assert.equal(s.frames,1);
  M.finishCapture(s);assert.equal(s.draft.frames,1);assert.equal(s.draft.interrupted,false);
});
test('手动首屏必须确认成功，点击开始和授权均不虚构进度',()=>{
  const s=M.create('manual');M.requestManual(s);M.grantProjection(s,true);
  assert.equal(s.frames,0);assert.equal(s.manualPhase,'idle');
  M.startCapture(s,220);assert.equal(s.frames,0);assert.equal(s.manualPhase,'taking');
  M.observeManualScroll(s,530);assert.equal(s.frames,0);
  M.confirmManualFrame(s,true);assert.equal(s.frames,1);assert.equal(s.manualPhase,'recording');
  assert.equal(s.manualOrigin,220);
});
test('手动悬浮与通知能力独立，双入口不可用不允许开始',()=>{
  const s=M.create('manual-blocked');M.requestManual(s);M.grantProjection(s,true);
  assert.equal(s.sheet,'control');assert.equal(M.manualControl(s),null);assert.equal(M.startCapture(s),false);
  s.notificationsAllowed=true;s.sheet=null;s.screen='target';assert.equal(M.manualControl(s),'notification');
  assert.equal(M.startCapture(s),true);assert.equal(s.permission,false);
});
test('首次接图后进入简化指导，下一轮保留学习状态',()=>{
  const s=M.create('manual');M.requestManual(s);M.grantProjection(s,true);M.startCapture(s);M.confirmManualFrame(s,true);
  assert.equal(s.manualHintSeen,false);M.observeManualScroll(s,100);assert.equal(s.manualHintSeen,false);
  M.observeManualScroll(s,310);assert.equal(s.manualHintSeen,true);M.finishCapture(s);M.clearDraft(s);
  M.requestManual(s);M.grantProjection(s,true);M.startCapture(s);M.confirmManualFrame(s,true);assert.equal(s.manualHintSeen,true);
});
test('首屏失败保留重试入口，未有画面结束不能伪造草稿',()=>{
  const s=M.create('manual');M.requestManual(s);M.grantProjection(s,true);M.startCapture(s);M.confirmManualFrame(s,false);
  assert.equal(s.manualPhase,'failed');assert.equal(s.frames,0);assert.equal(s.draft,null);
  assert.equal(M.finishCapture(s),false);assert.equal(s.draft,null);assert.equal(s.projection,false);assert.equal(s.screen,'home');
  assert.equal(s.captureMessage,'还未截到画面');
});
test('手动开始点是接续基准，结束生成期间不追加内容',()=>{
  const s=M.create('manual');M.requestManual(s);M.grantProjection(s,true);M.startCapture(s,310);M.confirmManualFrame(s,true);
  M.observeManualScroll(s,620);assert.equal(s.frames,2);s.manualPhase='finishing';M.observeManualScroll(s,930);assert.equal(s.frames,2);
  M.finishCapture(s);assert.equal(s.draft.frames,2);
});
test('取消手动准备释放会话，不启用无障碍，不留下虚构结果',()=>{
  const s=M.create('manual');M.requestManual(s);M.grantProjection(s,true);M.cancelManual(s);
  assert.equal(s.projection,false);assert.equal(s.permission,false);assert.equal(s.screen,'home');assert.equal(s.draft,null);
});

(function (scope) {
  'use strict';
  const copy = value => JSON.parse(JSON.stringify(value));
  const clamp = (value, min, max) => Math.max(min, Math.min(max, value));
  function cleanEdit(edit) {
    const c = edit.crop;
    const x = clamp(c.x, 0, .9), y = clamp(c.y, 0, .9);
    return {crop:{x,y,w:clamp(c.w,.1,1-x),h:clamp(c.h,.1,1-y)}};
  }
  function draft(frames, interrupted=false) {
    return {frames,height:540+(frames-1)*360,interrupted,saved:false,savedEdit:null,shared:false,
      history:[{crop:{x:0,y:0,w:1,h:1}}],index:0};
  }
  function create(scenario='first',vendor='xiaomi') {
    const manual=scenario.startsWith('manual'),ready = !manual&&!['first','denied','android10'].includes(scenario);
    return {scenario,vendor,mode:manual?'manual':'auto',manualPosition:0,manualOrigin:0,manualGap:false,manualPhase:'idle',manualEnding:false,manualHintSeen:false,manualGuideDismissed:false,captureMessage:null,
      overlayAllowed:!['manual-notification','manual-blocked'].includes(scenario),notificationsAllowed:scenario==='manual-notification',screen:'home',permission:ready,bubble:ready,legacy:scenario==='android10',pendingStart:false,
      projection:false,denied:scenario==='denied',frames:0,draft:['draft','savefail'].includes(scenario)?draft(4):null,
      failConsumed:false,exportStatus:'idle',sheet:null};
  }
  function grant(s, allowed) {
    s.permission=allowed; s.bubble=allowed; s.denied=!allowed;
    if(allowed)s.mode='auto';
    s.screen='home'; s.sheet=null;
  }
  function disable(s) { s.permission=false; s.bubble=false; s.projection=false; }
  function requestManual(s) {
    if(s.exportStatus==='saving')return false;
    s.mode='manual';s.projection=false;s.manualPhase='idle';s.manualEnding=false;s.manualGuideDismissed=false;s.captureMessage=null;s.frames=0;s.pendingStart=true;s.sheet='projection';return true;
  }
  function manualControl(s){return s.overlayAllowed?'overlay':s.notificationsAllowed?'notification':null;}
  function grantProjection(s, allowed) {
    s.projection=allowed;s.sheet=allowed&&!manualControl(s)?'control':null;s.pendingStart=false;s.screen=allowed&&manualControl(s)?'desktop':'home';
  }
  function startCapture(s,position=0) {
    if(s.mode==='manual'&&(!s.projection||!manualControl(s)||['taking','recording','finishing'].includes(s.manualPhase)))return false;
    s.manualPosition=s.manualOrigin=Math.max(0,Number.isFinite(position)?position:0);s.manualGap=false;s.manualEnding=false;s.captureMessage=null;
    s.frames=s.mode==='manual'?0:1;s.manualPhase=s.mode==='manual'?'taking':'idle';s.screen='capturing';s.sheet=null;s.exportStatus='idle';
    return true;
  }
  function confirmManualFrame(s,success=true){
    if(s.mode!=='manual'||!s.projection||s.manualPhase!=='taking')return false;
    s.manualPhase=success?'recording':'failed';s.frames=success?1:0;return true;
  }
  function cancelManual(s){s.projection=false;s.pendingStart=false;s.manualPhase='idle';s.manualEnding=false;s.frames=0;s.captureMessage=null;s.sheet=null;s.screen='home';}
  function stepCapture(s) { if(s.screen==='capturing'&&s.mode!=='manual') s.frames=Math.min(4,s.frames+1); }
  function observeManualScroll(s, position) {
    if(s.mode!=='manual'||s.screen!=='capturing'||s.manualPhase!=='recording'||!s.projection||s.manualGap||!Number.isFinite(position))return;
    const next=Math.max(0,position);
    if(next<=s.manualPosition)return;
    // A large missing-overlap jump is a prototype failure simulation, not a capture algorithm.
    if(next-s.manualPosition>480){s.manualGap=true;return;}
    s.manualPosition=next;s.frames=Math.min(4,1+Math.floor((next-s.manualOrigin)/310));
    if(s.frames>1)s.manualHintSeen=true;
  }
  function finishCapture(s, interrupted=false) {
    if(s.mode==='manual'&&!s.frames){s.projection=false;s.manualPhase='idle';s.manualEnding=false;s.captureMessage='还未截到画面';s.screen='home';s.sheet=null;return false;}
    s.draft=draft(Math.max(1,s.frames),interrupted||s.manualGap); s.screen='result'; s.sheet=null;
    s.draft.gap=s.mode==='manual'&&s.manualGap;
    if(s.mode==='manual'){s.projection=false;s.manualPhase='idle';s.manualEnding=false;s.draft.sourceOffset=s.manualOrigin;}
    return true;
  }
  function current(s) { return s.draft?copy(s.draft.history[s.draft.index]):null; }
  const signature=edit=>JSON.stringify(cleanEdit(edit));
  function refreshSaved(s){if(s.draft)s.draft.saved=s.draft.savedEdit===signature(current(s));}
  function commit(s, edit) {
    if(!s.draft) return;
    s.draft.history=s.draft.history.slice(0,s.draft.index+1);
    s.draft.history.push(cleanEdit(edit)); s.draft.index++;
    refreshSaved(s);s.draft.shared=false;
  }
  function undo(s) { if(s.draft&&s.draft.index>0){s.draft.index--;refreshSaved(s);s.draft.shared=false;} }
  function redo(s) { if(s.draft&&s.draft.index<s.draft.history.length-1){s.draft.index++;refreshSaved(s);s.draft.shared=false;} }
  function exportResult(s, type, success) {
    s.exportStatus=success?'success':'failed';
    if(success&&s.draft){if(type==='save'){s.draft.savedEdit=signature(current(s));s.draft.saved=true;}else s.draft.shared=true;}
  }
  function needsProtection(s) { return !!s.draft&&!s.draft.saved; }
  function clearDraft(s) { s.draft=null;s.screen='home';s.sheet=null;s.exportStatus='idle'; }
  const api={create,grant,disable,requestManual,grantProjection,manualControl,startCapture,confirmManualFrame,cancelManual,stepCapture,observeManualScroll,finishCapture,current,commit,undo,redo,exportResult,needsProtection,clearDraft,cleanEdit,refreshSaved};
  if(typeof module!=='undefined'&&module.exports) module.exports=api;
  else scope.LianyePrototypeModel=api;
})(globalThis);

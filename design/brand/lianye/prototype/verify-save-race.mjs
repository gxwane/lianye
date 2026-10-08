import {createRequire} from 'node:module';
import {fileURLToPath,pathToFileURL} from 'node:url';
import assert from 'node:assert/strict';
const runtime='C:/Users/wgx/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/';
const {chromium}=createRequire(runtime+'package.json')('playwright');
const browser=await chromium.launch({headless:true,executablePath:'C:/Users/wgx/.cache/puppeteer/chrome/win64-151.0.7922.71/chrome-win64/chrome.exe'});
const page=await browser.newPage();
await page.addInitScript(()=>{window.openai={widgetState:null,setWidgetState:async v=>{window.openai.widgetState=v;}};});
const click=async a=>page.locator(`[data-action="${a}"]`).first().click();
const state=()=>page.evaluate(()=>window.openai.widgetState.privateContent.experience);
try{
  await page.goto(pathToFileURL(fileURLToPath(new URL('../../../../build/lianye-prototype/preview.html',import.meta.url))).href);
  await page.locator('#ly-scenario').selectOption('draft');await click('resume');
  // Execute a rapid sequence in one browser task, before the 700 ms export finishes.
  await page.evaluate(()=>{const click=a=>document.querySelector(`[data-action="${a}"]`).click();click('save');click('draft-home');click('new');document.querySelector('[data-action="discard-new"]')?.click();document.querySelector('[data-action="open-browser"]')?.click();document.querySelector('[data-action="capture"]')?.click();document.querySelector('[data-action="stop"]')?.click();});
  await page.waitForTimeout(850);const after=await state();
  console.log('After rapid replacement attempt:',JSON.stringify({screen:after.screen,frames:after.draft?.frames,saved:after.draft?.saved}));
  assert.equal(after.draft?.frames,4,'保存中不可用新建替换原四屏草稿');assert.equal(after.draft.saved,true);
  await click('resume');await click('crop');await page.locator('[data-corner="tl"]').focus();await page.keyboard.press('ArrowDown');await click('apply-edit');await click('draft-home');await click('new');await click('save-new');await click('draft-home');
  assert.equal(await page.locator('[data-action="new"]').isDisabled(),true,'保存并新建期间也阻止二次新建');
  await page.waitForTimeout(850);assert.equal((await state()).screen,'desktop');assert.equal((await state()).draft,null);
  await click('open-browser');await click('capture');await click('stop');await page.waitForTimeout(850);
  assert.equal((await state()).draft.frames,1);assert.equal((await state()).draft.saved,false,'旧保存回调不能误标后来截取的图');
  console.log('Save race regression passed: original draft saved; later draft retained and unsaved.');
}finally{await browser.close();}

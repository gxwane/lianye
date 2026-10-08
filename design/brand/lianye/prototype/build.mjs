import {readFile,writeFile,mkdir} from 'node:fs/promises';
import {fileURLToPath} from 'node:url';
const dir=new URL('./',import.meta.url);
const [template,css,model,help,runtime,png]=await Promise.all([
  readFile(new URL('prototype.template.html',dir),'utf8'),
  readFile(new URL('prototype.css',dir),'utf8'),
  readFile(new URL('model.cjs',dir),'utf8'),
  readFile(new URL('device-help.cjs',dir),'utf8'),
  readFile(new URL('prototype.js',dir),'utf8'),
  readFile(new URL('assets/ui-symbol-small-64.png',dir))
]);
const fragment=template.replace('/* LY_STYLES */',css.replaceAll('__LOGO__','data:image/png;base64,'+png.toString('base64')))
  .replace('/* LY_MODEL */',model).replace('/* LY_DEVICE_HELP */',help).replace('/* LY_RUNTIME */',runtime);
if(Buffer.byteLength(fragment)>=1_000_000)throw new Error('Fragment exceeds 1 MB');
if(/<(?:html|head|body)\b|<!doctype|\bfetch\s*\(|XMLHttpRequest|new\s+WebSocket/.test(fragment))throw new Error('Unexpected document or network API in fragment');
const output=new URL('lianye-interaction-prototype.html',dir);
await writeFile(output,fragment);
await mkdir(new URL('review/',dir),{recursive:true});
// Private browser QA wrapper. The deliverable remains the fragment above.
const preview=`<!doctype html><html lang="zh-CN" style="color-scheme:light dark"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>连页原型 QA</title><style>body{margin:0;padding:16px;background:light-dark(#fff,#121614);font:14px system-ui;color:light-dark(#252B2A,#F7F4EE)}.form-label{display:block;margin-bottom:6px}.form-select{width:100%;font:inherit;padding:10px;border:1px solid light-dark(#ddd,#45514B);border-radius:8px;background:light-dark(#fff,#29322F);color:inherit}.text-small{font-size:12px}.text-muted{opacity:.7}.sr-only{position:absolute;width:1px;height:1px;overflow:hidden;clip:rect(0,0,0,0)}</style></head><body>${fragment}</body></html>`;
await mkdir(new URL('../../../../build/lianye-prototype/',dir),{recursive:true});
await writeFile(new URL('../../../../build/lianye-prototype/preview.html',dir),preview);
console.log(`${fileURLToPath(output)} (${Buffer.byteLength(fragment)} bytes)`);

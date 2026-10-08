import {createRequire} from 'node:module';
import {readFile,writeFile,mkdir} from 'node:fs/promises';
import {fileURLToPath} from 'node:url';
const runtime=process.env.LY_NODE_RUNTIME||'C:/Users/wgx/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/';
const {chromium}=createRequire(runtime+'package.json')('playwright');
const dir=new URL('./',import.meta.url);
const [source,geometry]=await Promise.all([readFile(new URL('../refined-01/assets/symbol-small.svg',dir),'utf8'),readFile(new URL('../refined-01/geometry.json',dir),'utf8').then(JSON.parse)]);
// Preserve the approved optical geometry; restore the approved two-color pairing.
const svg=source.replace(/(<path data-role="page"[^>]*stroke=")[^"]+("[^>]*>)/,`$1${geometry.palette.accent}$2`);
if(svg===source)throw new Error('Page color was not replaced');
await mkdir(new URL('assets/',dir),{recursive:true});
await writeFile(new URL('assets/ui-symbol-small.svg',dir),svg);
const browser=await chromium.launch({headless:true,executablePath:process.env.LY_CHROME||'C:/Users/wgx/.cache/puppeteer/chrome/win64-151.0.7922.71/chrome-win64/chrome.exe'});
try {
  const page=await browser.newPage({viewport:{width:64,height:64},deviceScaleFactor:1});
  await page.setContent(`<style>body{margin:0;background:transparent}svg{display:block;width:64px;height:64px}</style>${svg}`);
  await page.locator('svg').screenshot({path:fileURLToPath(new URL('assets/ui-symbol-small-64.png',dir)),omitBackground:true});
  console.log('Approved optical symbol rendered in ink + orange (64 px)');
}finally{await browser.close();}

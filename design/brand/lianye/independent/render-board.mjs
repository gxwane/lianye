import { readFile, writeFile, mkdir } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';

const directory = dirname(fileURLToPath(import.meta.url));
const allEntries = [
  ['A1', 'Alpha', '续幅', 'alpha/alpha-01-continuous-form.svg', '错位宽幅接成单一轮廓', '容易被看成 Z'],
  ['B1', 'Beta', '咬合续幅', 'beta/beta-01-continuous-joint.svg', '两段宽形共享连接部', '容易被看成 S 或积木'],
  ['C1', 'Gamma', '续面', 'gamma/gamma-01-continuous-mass.svg', '上下宽面连续转接', '容易被看成闪电或 Z'],
  ['A2', 'Alpha', '连字', 'alpha/alpha-02-lian-character.svg', '重绘名字中的「连」', '车部件容易带来交通联想'],
  ['B2', 'Beta', '延伸的页', 'beta/beta-02-extended-page.svg', '重绘名字中的「页」', '容易被看成小人或机器人'],
  ['C2', 'Gamma', '连势', 'gamma/gamma-02-lian-motion.svg', '把「连」画成紧凑字形', '小尺寸笔画可能拥挤'],
  ['A3', 'Alpha', '下展口', 'alpha/alpha-03-open-field.svg', '取景范围从下方延伸', '容易被看成门洞或帐篷'],
  ['B3', 'Beta', '三片合围', 'beta/beta-03-three-part-aperture.svg', '三个模块围出完整长视口', '容易被看成数字 0 或夹具'],
  ['C3', 'Gamma', '生长拍', 'gamma/gamma-03-growth-beats.svg', '三段体量形成向下增长节奏', '容易被看成速度条纹或排序'],
];
const mode = process.argv[2] ?? 'all';
if (!['all', 'round-1', 'round-2'].includes(mode)) throw new Error('Choose all, round-1, or round-2');
const entries = mode === 'round-1' ? allEntries.slice(0, 6) : mode === 'round-2' ? allEntries.slice(6) : allEntries;
const outputName = mode === 'all' ? 'all-directions' : mode;

const htmlEscape = value => value.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;');
const symbols = await Promise.all(entries.map(async ([id, agent, name, filename, premise, risk]) => {
  const source = await readFile(join(directory, filename), 'utf8');
  const match = source.match(/<symbol\b([^>]*)>([\s\S]*?)<\/symbol>/);
  if (!match) throw new Error(`${filename}: no symbol`);
  const viewBox = match[1].match(/viewBox="([^"]+)"/)?.[1];
  if (!viewBox) throw new Error(`${filename}: no viewBox`);
  const fill = match[1].match(/\bfill="([^"]+)"/)?.[1] ?? 'currentColor';
  const mark = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="${viewBox}" fill="${fill}" aria-label="${htmlEscape(name)}">${match[2]}</svg>`;
  return {id, agent, name, premise, risk, mark};
}));

const cards = symbols.map(item => `<article>
  <div class="label"><b>${item.id}</b><span>${item.agent} 独立稿</span></div>
  <h2>${item.name}</h2><p class="premise">${item.premise}</p>
  <div class="large">${item.mark}</div>
  <div class="proof"><div class="app-icon">${item.mark}</div><div class="size size48">${item.mark}<span>48 px</span></div><div class="size size32">${item.mark}<span>32 px</span></div><div class="size size24">${item.mark}<span>24 px</span></div></div>
  <p class="risk">误读风险：${item.risk}</p>
</article>`).join('\n');

const page = `<!doctype html>
<html lang="zh-CN"><head><meta charset="utf-8"><title>连页 · 三位 agent 独立探索</title>
<style>
*{box-sizing:border-box}body{margin:0;background:#f4f4f3;color:#161616;font-family:'Microsoft YaHei','PingFang SC',sans-serif}
main{width:1680px;padding:58px 60px 44px}header{height:154px}h1{margin:0 0 14px;font-size:44px;letter-spacing:1px;font-weight:700}header p{margin:0;font-size:22px;color:#575757;line-height:1.7}.grid{display:grid;grid-template-columns:repeat(3,1fr);gap:24px}article{background:white;border:1px solid #dcdcd9;border-radius:20px;padding:28px 30px 22px;height:560px}.label{display:flex;justify-content:space-between;align-items:center;font-size:17px;color:#686868}.label b{color:#111;font-size:22px}h2{font-size:28px;margin:15px 0 9px;font-weight:650}.premise{margin:0;color:#575757;font-size:18px}.large{height:224px;display:flex;justify-content:center;align-items:center}.large svg{width:164px;height:186px;display:block;color:#161616}.proof{height:126px;display:flex;align-items:center;justify-content:space-between;border-top:1px solid #ededeb;padding-top:14px}.app-icon{height:90px;width:90px;border-radius:23px;display:flex;align-items:center;justify-content:center;background:#161616;color:white;flex-shrink:0}.app-icon svg{height:58px;width:58px;display:block}.size{display:flex;flex-direction:column;align-items:center;justify-content:center;gap:10px;min-width:62px;height:90px}.size48 svg{width:48px;height:48px}.size32 svg{width:32px;height:32px}.size24 svg{width:24px;height:24px}.size span{font-size:14px;color:#777}.risk{border-top:1px solid #ededeb;padding-top:16px;margin:10px 0 0;font-size:17px;color:#666;line-height:1.4}footer{font-size:17px;color:#666;padding-top:22px;line-height:1.7}
</style></head><body><main><header><h1>连页 · 三位 agent 独立探索</h1><p>${mode === 'round-2' ? '追加轮：分别从取景延伸、片段拼合、滚动节奏出发，进一步拉开结构差异。' : '首轮使用相同产品简报，互不查看旧稿与彼此方案；追加轮分三个切入点探索。'}<br>统一黑白呈现，比较形状本身。这些是方向草案，颜色与细节比例尚未定稿。</p></header><section class="grid">${cards}</section><footer>每列来自一位 agent：Alpha / Beta / Gamma。每稿包含统一容器和实际 48 / 32 / 24 px 对照。</footer></main></body></html>`;

await mkdir(join(directory, 'review'), {recursive:true});
await writeFile(join(directory, `review/${outputName}.html`), page, 'utf8');
console.log(`Rendered HTML for ${symbols.length} original marks: review/${outputName}.html`);

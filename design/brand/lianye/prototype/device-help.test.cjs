const {test}=require('node:test');
const assert=require('node:assert/strict');
const {readFileSync}=require('node:fs');
const {resolve}=require('node:path');
const H=require('./device-help.cjs');
const resources=readFileSync(resolve(__dirname,'../../../../app/src/main/res/values-zh/strings.xml'),'utf8');
for(const [key,device]of Object.entries(H.devices))test(`${key} 沿用现有厂商帮助路径`,()=>{
  const full=resources.match(new RegExp(`<string name="vendor_path_${device.resource}">([^<]+)</string>`))[1].replace('%1$s','连页');
  assert.equal(device.full,full);
  assert.ok(full.includes(device.short));
});
test('未知厂商使用通用回退',()=>{assert.equal(H.get('unknown'),H.devices.aosp);});

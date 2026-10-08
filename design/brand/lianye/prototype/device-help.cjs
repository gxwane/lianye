(function(scope){
  'use strict';
  const devices={
    xiaomi:{label:'小米 / Redmi',resource:'xiaomi',short:'无障碍 → 通用 → 已下载的服务 → 连页',full:'设置 → 更多设置 → 无障碍 → 通用 → 已下载的服务 → 连页'},
    oppo:{label:'OPPO / 一加 / realme',resource:'oppo',short:'无障碍 → 通用 → 已下载的应用 → 连页',full:'设置 → 其他设置 → 无障碍 → 通用 → 已下载的应用 → 连页'},
    vivo:{label:'vivo / iQOO',resource:'vivo',short:'无障碍 → 已下载的服务 → 连页',full:'设置 → 快捷与辅助 → 无障碍 → 已下载的服务 → 连页'},
    huawei:{label:'华为 / 荣耀',resource:'huawei',short:'无障碍 → 已安装的服务 → 连页',full:'设置 → 辅助功能 → 无障碍 → 已安装的服务 → 连页'},
    samsung:{label:'三星',resource:'samsung',short:'辅助功能 → 已安装的应用程序 → 连页',full:'设置 → 辅助功能 → 已安装的应用程序 → 连页'},
    aosp:{label:'原生 / 其他安卓',resource:'aosp',short:'无障碍 → 已下载的应用 → 连页',full:'设置 → 无障碍 → 已下载的应用 → 连页'}
  };
  const api={devices,get:vendor=>devices[vendor]||devices.aosp,valid:vendor=>Object.hasOwn(devices,vendor)};
  if(typeof module!=='undefined'&&module.exports)module.exports=api;else scope.LianyeDeviceHelp=api;
})(globalThis);

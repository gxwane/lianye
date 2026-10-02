package org.scrollloom.platform

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.StringRes
import org.scrollloom.R
import org.scrollloom.service.LoomAccessibilityService
import java.util.Locale

enum class DeviceVendor {
    XIAOMI,
    OPPO,
    VIVO,
    HUAWEI,
    SAMSUNG,
    AOSP
}

/**
 * 宿主设备品牌静默感知引擎。
 * 零 UI 依赖，负责推导设备厂商分类，并映射行前微提示及生成焦点高亮 Intent。
 */
object DeviceVendorDetector {

    val currentVendor: DeviceVendor by lazy {
        resolveVendor(Build.MANUFACTURER, Build.BRAND)
    }

    fun resolveVendor(manufacturer: String?, brand: String?): DeviceVendor {
        val m = manufacturer?.lowercase(Locale.ROOT).orEmpty()
        val b = brand?.lowercase(Locale.ROOT).orEmpty()
        return when {
            m.contains("xiaomi") || m.contains("redmi") || b.contains("blackshark") -> DeviceVendor.XIAOMI
            m.contains("oppo") || m.contains("oneplus") || m.contains("realme") -> DeviceVendor.OPPO
            m.contains("vivo") || m.contains("iqoo") -> DeviceVendor.VIVO
            m.contains("huawei") || m.contains("honor") || b.contains("honor") -> DeviceVendor.HUAWEI
            m.contains("samsung") -> DeviceVendor.SAMSUNG
            else -> DeviceVendor.AOSP
        }
    }

    @StringRes
    fun getPreFlightHintRes(vendor: DeviceVendor = currentVendor): Int {
        return when (vendor) {
            DeviceVendor.XIAOMI -> R.string.vendor_hint_xiaomi
            DeviceVendor.OPPO -> R.string.vendor_hint_oppo
            DeviceVendor.VIVO -> R.string.vendor_hint_vivo
            DeviceVendor.HUAWEI -> R.string.vendor_hint_huawei
            DeviceVendor.SAMSUNG -> R.string.vendor_hint_samsung
            DeviceVendor.AOSP -> R.string.vendor_hint_aosp
        }
    }

    /**
     * 生成安全焦点高亮 Intent：
     * 以标准的 [Settings.ACTION_ACCESSIBILITY_SETTINGS] 为基底，
     * 叠加 AOSP 标准参数以使支持列表自动滚动并闪烁定位至目标服务。
     */
    fun createAccessibilityIntent(context: Context): Intent {
        val componentName = ComponentName(context, LoomAccessibilityService::class.java).flattenToString()
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(":settings:fragment_args_key", componentName)
            putExtra("extra_fragment_arg_key", componentName)
            putExtra(":settings:show_fragment_args", Bundle().apply {
                putString(":settings:fragment_args_key", componentName)
                putString("extra_fragment_arg_key", componentName)
            })
        }
    }
}

package org.scrollloom.platform

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.os.Process

/**
 * 宿主系统受限制设置 (Restricted Settings) 安全探针。
 *
 * 核心安全规则：
 * 1. Android 10..12 无此机制，严格短路返回 false，杜绝底层抛出 IllegalArgumentException；
 * 2. Android 13+ (API 33) 探测 AppOp 状态；
 * 3. 任何非受信异常均安全降级返回 false。
 */
object RestrictedSettingsHelper {

    private const val OPSTR_ACCESS_RESTRICTED_SETTINGS = "android:access_restricted_settings"

    fun isRestrictedBlocked(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return false
        }
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
                ?: return false
            val mode = appOps.unsafeCheckOpNoThrow(
                OPSTR_ACCESS_RESTRICTED_SETTINGS,
                Process.myUid(),
                context.packageName
            )
            mode != AppOpsManager.MODE_ALLOWED
        } catch (_: Throwable) {
            false
        }
    }
}

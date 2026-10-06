package com.ghostlock.app.ui

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import java.io.File
import java.util.Locale

data class ManagerDetection(
    val installed: Boolean = false,
    val name: String = "No Manager",
    val packageName: String = "",
    val recognized: Boolean = false,
    val spoofed: Boolean = false,
    val identityVerified: Boolean = false,
)

object ManagerDetector {
    private const val RESUKISU_PACKAGE = "com.resukisu.resukisu"
    private const val RESUKISU_PREFIX = "com.resukisu.resukisu."

    fun detect(context: Context): ManagerDetection {
        val pm = context.packageManager

        fun inspect(app: ApplicationInfo): ManagerDetection? {
            val pkg = app.packageName ?: return null
            val label = runCatching { app.loadLabel(pm)?.toString()?.trim().orEmpty() }.getOrDefault("")
            val lower = label.lowercase(Locale.ROOT)
            val resukisu = pkg == RESUKISU_PACKAGE || pkg.startsWith(RESUKISU_PREFIX)
            val named = lower.contains("bakasu") || lower.contains("resukisu")
            val ksud = hasKsud(app)
            if (!resukisu && !named && !ksud) return null

            val name = when {
                lower.contains("bakasu") -> "BakaSU"
                lower.contains("resukisu") -> "ReSukiSU"
                resukisu -> "ReSukiSU / BakaSU"
                label.isNotEmpty() -> label
                else -> "BakaSU / ReSukiSU"
            }
            return ManagerDetection(
                installed = true,
                name = name,
                packageName = pkg,
                recognized = true,
                // Unknown package + libksud.so is treated as a repackaged/spoofed manager.
                spoofed = !resukisu && !named && ksud,
            )
        }

        runCatching {
            listOf(
                "com.resukisu.resukisu",
                "me.weishu.kernelsu",
                "me.weishu.kernelsu.pr",
                "com.kowx712.supermanager",
            ).forEach { pkg ->
                val app = pm.getApplicationInfo(pkg, 0)
                inspect(app)?.let { return it }
            }
        }

        runCatching {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val apps = if (Build.VERSION.SDK_INT >= 33) {
                pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()))
            } else {
                pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            }
            for (info in apps) {
                val app = info.activityInfo?.applicationInfo ?: continue
                inspect(app)?.let { return it }
            }
        }

        return ManagerDetection()
    }

    private fun hasKsud(app: ApplicationInfo): Boolean {
        val dir = app.nativeLibraryDir?.takeIf { it.isNotEmpty() } ?: return false
        val base = File(dir)
        return File(base, "libksud.so").isFile ||
            File(base, "arm64/libksud.so").isFile ||
            File(base, "arm/libksud.so").isFile
    }
}

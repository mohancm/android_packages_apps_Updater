package org.lineageos.updater.controller

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import java.io.*
import java.security.GeneralSecurityException
import java.util.*


@RequiresApi(Build.VERSION_CODES.Q)
fun install(context: Context, apkFile: File) {
   // installApk(context, File("${apkFile.path}.apk"))
    installApks(context, listOf(File("${apkFile.path}.apk")))
}

private fun installApk(context: Context, apkFile: File) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
    intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)
    intent.setDataAndType(FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", apkFile), "application/vnd.android.package-archive")
    startActivity(context, intent, null)
}


@RequiresApi(Build.VERSION_CODES.Q)
@Throws(GeneralSecurityException::class)
private fun installApks(context: Context, files: List<File>): Int {
    val packageInstaller: PackageInstaller = context.packageManager.packageInstaller
    val nameSizeMap = HashMap<String, Long>()
    val filenameToPathMap = HashMap<String, String>()
    var totalSize: Long = 0
    for (file in files) {
        val listOfFile = file
        if (listOfFile.isFile) {
            nameSizeMap[listOfFile.name] = listOfFile.length()
            filenameToPathMap[listOfFile.name] = file.path
            totalSize += listOfFile.length()
        }
    }
    val sessionParams = SessionParams(SessionParams.MODE_FULL_INSTALL)
    sessionParams.setSize(totalSize)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        sessionParams.setRequireUserAction(SessionParams.USER_ACTION_NOT_REQUIRED)
    }
    try {
        val sessionId = packageInstaller.createSession(sessionParams)
        val session = packageInstaller.openSession(sessionId)
        for ((splitName, sizeBytes) in nameSizeMap) {
            val inPath = filenameToPathMap[splitName]
            var inputStream: InputStream? = null
            var out: OutputStream? = null
            try {
                if (inPath != null) {
                    inputStream = FileInputStream(inPath)
                }
                out = session.openWrite(splitName, 0, sizeBytes)
                val buffer = ByteArray(65536)
                var c: Int
                if (inputStream != null) {
                    while (inputStream.read(buffer).also { c = it } != -1) {
                        out.write(buffer, 0, c)
                    }
                }
                session.fsync(out)
            } catch (ignored: IOException) {
                throw ignored
            } finally {
                try {
                    out?.close()
                    inputStream?.close()
                    session.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        val callbackIntent = Intent(context.applicationContext, APKInstallReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(
                context.applicationContext, 0, callbackIntent,
                PendingIntent.FLAG_MUTABLE
            )
        session.commit(pendingIntent.intentSender)
        session.close()
        return sessionId
    } catch (e: IOException) {
        e.printStackTrace()
        return -999
    }
}

fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
    return getPackageVersionCode(packageName, packageManager) != null
}


fun getAppName(packageName: String, packageManager: PackageManager): String? {
    return try {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }
}

fun getPackageVersionCode(packageName: String, packageManager: PackageManager): Long? {
    return try {
        val pkgInfo = packageManager.getPackageInfo(packageName, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pkgInfo.longVersionCode
        } else {
            pkgInfo.versionCode.toLong()
        }

    } catch (e: PackageManager.NameNotFoundException) {
        null
    }
}

fun getPackageInfo(packageManager: PackageManager): List<ApplicationInfo> {
    return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
}

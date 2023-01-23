package org.lineageos.updater.controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log
import org.lineageos.updater.R

class APKInstallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return

        val sessionId = intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -999)
        val app = context.applicationContext
        val unknownCode = -999

        val extraStatus = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, unknownCode)

        Log.d("Install", "$sessionId , $extraStatus ${intent.action}")

        when (extraStatus) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmationIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                confirmationIntent?.let {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    (context).startActivity(it)
                }
            }
            PackageInstaller.STATUS_FAILURE_ABORTED -> {
                apkInstallListener?.onInstallFailure(
                    InstallCallBack.FailureAborted(
                        sessionId, context.getString(
                            R.string.icAborted
                        )
                    )
                )
            }
            PackageInstaller.STATUS_SUCCESS -> {
                apkInstallListener?.onInstallSuccess(sessionId)
            }
            PackageInstaller.STATUS_FAILURE -> {
                apkInstallListener?.onInstallFailure(
                    InstallCallBack.Failure(
                        sessionId, context.getString(
                            R.string.icUnknownError
                        )
                    )
                )
            }
            PackageInstaller.STATUS_FAILURE_STORAGE -> {
                apkInstallListener?.onInstallFailure(
                    InstallCallBack.FailureStorage(
                        sessionId, context.getString(
                            R.string.icStorage
                        )
                    )
                )
            }
            PackageInstaller.STATUS_FAILURE_INVALID -> {
                apkInstallListener?.onInstallFailure(
                    InstallCallBack.FailureInvalid(
                        sessionId, context.getString(
                            R.string.icInvalid
                        )
                    )
                )
            }
            PackageInstaller.STATUS_FAILURE_INCOMPATIBLE -> {
                apkInstallListener?.onInstallFailure(
                    InstallCallBack.Incompatible(
                        sessionId, context.getString(
                            R.string.icIncompatible
                        )
                    )
                )
            }
            PackageInstaller.STATUS_FAILURE_CONFLICT -> {
                apkInstallListener?.onInstallFailure(
                    InstallCallBack.Conflict(
                        sessionId, context.getString(
                            R.string.icConflict
                        )
                    )
                )
            }
            PackageInstaller.STATUS_FAILURE_BLOCKED -> {
                apkInstallListener?.onInstallFailure(
                    InstallCallBack.Blocked(
                        sessionId, context.getString(
                            R.string.icBlocked
                        )
                    )
                )
            }
            unknownCode -> {
                //ignore it
            }
            else -> {
                apkInstallListener?.onInstallFailure(
                    InstallCallBack.Failure(
                        sessionId, context.getString(
                            R.string.icUnknownError
                        )
                    )
                )
            }
        }
    }

    companion object {
        private var apkInstallListener: APKInstallListener? = null
        fun setListener(apkInstallListener: APKInstallListener?) {
            Companion.apkInstallListener = apkInstallListener
        }

    }

    interface APKInstallListener {
        fun onInstallSuccess(sessionId: Int)
        fun onInstallFailure(installCallBack: InstallCallBack)
    }
}
package org.lineageos.updater.controller

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class InstallCallBack(
    open val sessionId: Int,
    open val description: String,
    val unresolvableError: Boolean = true
) : Parcelable {

    @Parcelize
    data class Success(override val sessionId: Int, override val description: String) :
        InstallCallBack(
            description = description,
            unresolvableError = false,
            sessionId = sessionId
        )

    @Parcelize
    data class Failure(override val sessionId: Int, override val description: String) :
        InstallCallBack(
            sessionId = sessionId,
            description = description
        )

    @Parcelize
    data class FailureAborted(override val sessionId: Int, override val description: String) :
        InstallCallBack(
            sessionId = sessionId,
            description = description
        )

    @Parcelize
    data class FailureStorage(override val sessionId: Int, override val description: String) :
        InstallCallBack(
            sessionId = sessionId,
            description = description
        )

    @Parcelize
    data class FailureInvalid(override val sessionId: Int, override val description: String) :
        InstallCallBack(
            sessionId = sessionId,
            description = description
        )

    @Parcelize
    data class Conflict(override val sessionId: Int, override val description: String) :
        InstallCallBack(
            sessionId = sessionId,
            description = description
        )

    @Parcelize
    data class Blocked(override val sessionId: Int, override val description: String) :
        InstallCallBack(
            sessionId = sessionId,
            description = description
        )

    @Parcelize
    data class Incompatible(override val sessionId: Int, override val description: String) :
        InstallCallBack(
            sessionId = sessionId,
            description = description
        )

    @Parcelize
    data class UserActionPending(override val sessionId: Int, override val description: String) :
        InstallCallBack(
            sessionId = sessionId,
            description = description
        )

}
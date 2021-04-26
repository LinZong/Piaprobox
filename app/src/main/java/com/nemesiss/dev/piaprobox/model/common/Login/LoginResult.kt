package com.nemesiss.dev.piaprobox.model.common.Login

import android.os.Parcel
import android.os.Parcelable

/**
 * @author <a href="yingyin.lsy@alibaba-inc.com">萤音</a>
 * @date 2020/11/27
 * @time 5:28 PM
 * @description
 */

enum class LoginStatus {
    SUCCESS,
    FAILED
}

data class LoginResult(val loginUser: String, val status: LoginStatus) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        LoginStatus.valueOf(parcel.readString() ?: "FAILED")
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(loginUser)
        parcel.writeString(status.name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LoginResult> {
        override fun createFromParcel(parcel: Parcel): LoginResult {
            return LoginResult(parcel)
        }

        override fun newArray(size: Int): Array<LoginResult?> {
            return arrayOfNulls(size)
        }
    }
}
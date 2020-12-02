package com.nemesiss.dev.piaprobox.Model.User

import android.os.Parcel
import android.os.Parcelable

/**
 * @param userName 用户登录名
 * @param nickName 用户昵称
 * @param avatarImage 用户头像Url (会带上http头)
 */
data class UserInfo(var userName: String, var nickName: String, var avatarImage: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userName)
        parcel.writeString(nickName)
        parcel.writeString(avatarImage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserInfo> {
        override fun createFromParcel(parcel: Parcel): UserInfo {
            return UserInfo(parcel)
        }

        override fun newArray(size: Int): Array<UserInfo?> {
            return arrayOfNulls(size)
        }
    }
}
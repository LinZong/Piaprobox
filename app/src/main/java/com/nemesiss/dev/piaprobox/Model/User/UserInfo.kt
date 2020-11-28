package com.nemesiss.dev.piaprobox.Model.User

import android.os.Parcel
import android.os.Parcelable

/**
 * @param UserName 用户登录名
 * @param NickName 用户昵称
 * @param AvatarImage 用户头像Url (会带上http头)
 */
data class UserInfo(val UserName: String, val NickName: String, val AvatarImage: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(UserName)
        parcel.writeString(NickName)
        parcel.writeString(AvatarImage)
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
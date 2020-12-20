package com.nemesiss.dev.piaprobox.Model.User

import android.os.Parcel
import android.os.Parcelable
import com.alibaba.fastjson.annotation.JSONCreator

/**
 * @param userName 用户登录名
 * @param nickName 用户昵称
 * @param avatarImage 用户头像Url (会带上http头)
 */

class UserInfo : Parcelable {

    @JvmField
    var userName: String = ""

    @JvmField
    var nickName: String = ""

    @JvmField
    var avatarImage: String = ""

    @JSONCreator
    constructor()

    constructor(parcel: Parcel) {
        userName = parcel.readString()
        nickName = parcel.readString()
        avatarImage = parcel.readString()
    }

    override fun toString(): String {
        return "UserInfo(userName='$userName', nickName='$nickName', avatarImage='$avatarImage')"
    }

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
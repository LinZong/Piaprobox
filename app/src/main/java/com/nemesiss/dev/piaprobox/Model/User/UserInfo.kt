package com.nemesiss.dev.piaprobox.Model.User

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @param userName 用户登录名
 * @param nickName 用户昵称
 * @param avatarImage 用户头像Url (会带上http头)
 */

@Parcelize
class UserInfo : Parcelable {
    lateinit var userName: String
    lateinit var nickName: String
    lateinit var avatarImage: String
    override fun toString(): String {
        return "UserInfo(userName='$userName', nickName='$nickName', avatarImage='$avatarImage')"
    }
}
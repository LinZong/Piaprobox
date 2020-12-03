package com.nemesiss.dev.piaprobox.Model.User

/**
 * @param userName 用户登录名
 * @param nickName 用户昵称
 * @param avatarImage 用户头像Url (会带上http头)
 */
class UserInfo {
    lateinit var userName: String
    lateinit var nickName: String
    lateinit var avatarImage: String
    override fun toString(): String {
        return "UserInfo(userName='$userName', nickName='$nickName', avatarImage='$avatarImage')"
    }
}
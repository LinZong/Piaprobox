package com.nemesiss.dev.piaprobox.Model.Resources

/**
 * @author <a href="yingyin.lsy@alibaba-inc.com">萤音</a>
 * @date 2020/11/27
 * @time 5:34 PM
 * @description
 */
object Constants {
    object Login {
        const val REQUEST_CODE = 3939
        const val RESULT_CODE = 2727

        /**
         * 登录态缓存有效期 (以秒为单位)
         * 默认5天
         */
        const val LOGIN_CACHE_VALID_TIME_INTERVAL_SEC = 5 * 24 * 60 * 60
        const val LOGIN_RESULT_KEY = "LOGIN_RESULT_KEY"
        const val LOGIN_RESULT_USERINFO_PAYLOAD_KEY = "LOGIN_RESULT_PAYLOAD_KEY"
    }

    object Url {
        const val MAIN_DOMAIN = "https://piapro.jp"
        const val LOGIN_PAGE = "https://piapro.jp/login/"

        fun getUserProfileUrl(username: String) = "${MAIN_DOMAIN}/${username}"
    }
}
package com.nemesiss.dev.piaprobox.Service

import android.content.Context
import android.content.SharedPreferences

class Persistence {

    companion object {
        @JvmStatic
        val MUSIC_PLAYER_LOOP_STATUS = "MUSIC_PLAYER_LOOP_STATUS"

        @JvmStatic
        lateinit var SharedPref : SharedPreferences

        @JvmStatic
        lateinit var SharedPrefEditor : SharedPreferences.Editor


        @JvmStatic
        fun Init(context: Context) {
            SharedPref = android.preference.PreferenceManager.getDefaultSharedPreferences(context)
        }

        @JvmStatic
        fun SetMusicPlayerLoopStatus(Loop : Boolean) {
            SharedPrefEditor = SharedPref.edit()
            SharedPrefEditor.putBoolean(MUSIC_PLAYER_LOOP_STATUS, Loop)
            SharedPrefEditor.apply()
        }

        @JvmStatic
        fun GetMusicPlayerLoopStatus() : Boolean {
            return SharedPref.getBoolean(MUSIC_PLAYER_LOOP_STATUS,false)
        }
    }


}
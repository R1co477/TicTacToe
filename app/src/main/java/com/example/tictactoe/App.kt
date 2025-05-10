package com.example.tictactoe

import android.app.Application
import android.content.SharedPreferences
import com.example.tictactoe.data.Profile
import com.example.tictactoe.extensions.getObject
import com.example.tictactoe.fragments.APP_PREFERENCES
import com.example.tictactoe.fragments.KEY_PROFILE

class App : Application() {
    private lateinit var sharedPref: SharedPreferences
    lateinit var profile: Profile
    override fun onCreate() {
        super.onCreate()
        sharedPref = this.getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        profile = sharedPref.getObject(KEY_PROFILE, Profile.default(this))
    }
}
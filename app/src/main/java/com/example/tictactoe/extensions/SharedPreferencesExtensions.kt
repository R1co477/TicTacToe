package com.example.tictactoe.extensions

import android.content.SharedPreferences
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder


val gson: Gson = GsonBuilder().registerTypeAdapter(Uri::class.java, UriAdapter()).create()

fun <T> SharedPreferences.Editor.putObject(key: String, value: T) {
    val json = gson.toJson(value)
    putString(key, json)
}

inline fun <reified T> SharedPreferences.getObject(key: String, defaultValue: T): T {
    val jsonString = getString(key, null)
    return if (jsonString != null) {
        gson.fromJson(jsonString, T::class.java) ?: defaultValue
    } else {
        defaultValue
    }
}
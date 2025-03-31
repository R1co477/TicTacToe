package com.example.tictactoe.extensions

import android.content.SharedPreferences
import android.net.Uri
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter


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

class UriAdapter : TypeAdapter<Uri>() {
    override fun write(out: JsonWriter, value: Uri?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.toString())
        }
    }

    override fun read(reader: JsonReader): Uri? {
        return if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            null
        } else {
            reader.nextString().toUri()
        }
    }
}
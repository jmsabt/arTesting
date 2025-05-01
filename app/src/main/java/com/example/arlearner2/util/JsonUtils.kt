package com.example.arlearner2.util

import android.content.Context
import com.example.arlearner2.ui.theme.screens.ModelInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun loadModelInfoFromJson(context: Context): List<ModelInfo> {
    val jsonString = context.assets.open("models.json").bufferedReader().use { it.readText() }
    val listType = object : TypeToken<List<ModelInfo>>() {}.type
    return Gson().fromJson(jsonString, listType)
}

package com.example.arlearner2.ui.theme.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeScreen

@Serializable
object AboutScreen

@Serializable
data class ARScreen(val model: String)

@Serializable
object GalleryScreen
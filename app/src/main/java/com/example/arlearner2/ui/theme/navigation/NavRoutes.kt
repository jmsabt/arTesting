package com.example.arlearner2.ui.theme.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeScreen

@Serializable
data object GalleryScreen

@Serializable
data class ARScreen(val modelPath: String)

@Serializable
data object ARCameraScreen

@Serializable
data object ModelSelectionScreen

@Serializable
data object RoofInpotScreen

@Serializable
data object AboutScreen
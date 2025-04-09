package com.example.arlearner2.ui.theme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.arlearner2.ui.theme.navigation.ARScreen
import io.github.sceneview.Scene
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes

data class ModelInfo(
    val name: String,
    val dimensions: String,
    val description: String
)

@Composable
fun GalleryScreen(navController: NavController) {
    val modelPaths = listOf(
        "models/chair1.glb",
        "models/chair2.glb",
        "models/cabinet1.glb",
        "models/Duck.glb",
        "models/1_meter_cube.glb"
    )
    val modelInfos = listOf(
        ModelInfo("Chair 1", "0.5 x 0.5 x 0.9 m", "A modern minimalist chair."),
        ModelInfo("Chair 2", "0.6 x 0.5 x 1.0 m", "A cushioned dining chair."),
        ModelInfo("Cabinet 1", "1.2 x 0.4 x 0.8 m", "A sleek storage cabinet."),
        ModelInfo("Duck", "1.2 x 0.4 x 0.8 m", "A duck."),
        ModelInfo("Cube", "1 x 1 x 1 m", "Just a cube."),
    )

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val currentIndex = remember { mutableStateOf(0) }
    val totalModels = modelInfos.size

    // Preload all models
    val modelNodes = remember(modelPaths) {
        modelPaths.map { path ->
            ModelNode(
                modelInstance = modelLoader.createModelInstance(path),
                autoAnimate = true,
                scaleToUnits = 0.35f
            ).apply { centerOrigin() }
        }
    }

    val childNodes = rememberNodes()
    childNodes.clear()
    childNodes += modelNodes[currentIndex.value]

    // Functions for navigation
    fun nextModel() {
        currentIndex.value = (currentIndex.value + 1) % totalModels
    }

    fun prevModel() {
        currentIndex.value = if (currentIndex.value - 1 < 0) totalModels - 1 else currentIndex.value - 1
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Model display
        Scene(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            engine = engine,
            childNodes = childNodes,
            isOpaque = false
        )

        // Model info card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = modelInfos[currentIndex.value].name,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Dimensions: ${modelInfos[currentIndex.value].dimensions}",
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = modelInfos[currentIndex.value].description,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Navigation buttons with "View in AR" in between
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { prevModel() }) {
                Text("Previous")
            }
            Button(onClick = { navController.navigate(ARScreen(modelPaths[currentIndex.value])) }) {
                Text("View in AR")
            }
            Button(onClick = { nextModel() }) {
                Text("Next")
            }
        }
    }
}
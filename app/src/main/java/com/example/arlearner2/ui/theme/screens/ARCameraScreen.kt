package com.example.arlearner2.ui.theme.screens

import android.util.Log
import android.os.Build
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.arlearner2.ui.theme.navigation.HomeScreen
import com.example.arlearner2.util.GlobalKit
import com.example.arlearner2.util.Utils
import com.google.ar.core.*
import io.github.sceneview.ar.*
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.Node
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView
import kotlinx.coroutines.delay
import java.nio.FloatBuffer
import kotlin.math.floor
import kotlin.math.sqrt
import kotlin.math.min


// Data class for solar panel models
data class SolarPanelModel(val name: String, val modelPath: String)

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun ARCameraScreen(navController: NavController) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine = engine)
    val materialLoader = rememberMaterialLoader(engine = engine)
    val cameraNode = rememberARCameraNode(engine = engine)
    val childNodes = rememberNodes()
    val view = rememberView(engine = engine)
    val collisionSystem = rememberCollisionSystem(view = view)
    val planeRenderer = remember { mutableStateOf(true) }
    val modelInstance = remember { mutableListOf<ModelInstance>() }
    val trackingFailureReason = remember { mutableStateOf<TrackingFailureReason?>(null) }
    val frame = remember { mutableStateOf<Frame?>(null) }
    val isPlaneFound = remember { mutableStateOf(false) }
    val selectedPlaneHit = remember { mutableStateOf<HitResult?>(null) }
    val planeArea = remember { mutableStateOf<Float?>(null) }
    val showSelectPrompt = remember { mutableStateOf(false) }
    val showPlacePrompt = remember { mutableStateOf(false) }
    val hasPlacedModels = remember { mutableStateOf(false) }
    val placementFeedback = remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val area = planeArea.value ?: 0f
    // List of available solar panel models
    val solarPanelModels = listOf(
        SolarPanelModel("AE CMER-132BDS-610", "models/panel1.glb"),
        SolarPanelModel("AE CMER-132BDS-605", "models/panel2.glb"),
        SolarPanelModel("AE CMER-132BDS-600", "models/panel3.glb"),
        SolarPanelModel("AE CMER-132BDS-595", "models/panel4.glb"),
        SolarPanelModel("AE CMER-132BDS-590", "models/panel5.glb")
    )

    // Currently selected panel state
    val selectedPanel = remember { mutableStateOf(solarPanelModels.first()) }
    val modelSize = 0.2f
    val spacing = 0.1f
    val totalModelSize = modelSize + spacing

    LaunchedEffect(hasPlacedModels.value) {
        if (hasPlacedModels.value) {
            delay(2000L)
            planeRenderer.value = false
            delay(1000L)
            placementFeedback.value = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            childNodes = childNodes,
            view = view,
            planeRenderer = planeRenderer.value,
            modelLoader = modelLoader,
            materialLoader = materialLoader,
            collisionSystem = collisionSystem,
            onTrackingFailureChanged = { reason ->
                trackingFailureReason.value = reason
                isPlaneFound.value = (reason == null && frame.value?.hasDetectedPlanes() == true)
                if (isPlaneFound.value && selectedPlaneHit.value == null) {
                    showSelectPrompt.value = true
                }
            },
            onSessionUpdated = { _, updatedFrame ->
                frame.value = updatedFrame
                if (!isPlaneFound.value) {
                    isPlaneFound.value = updatedFrame.hasDetectedPlanes()
                    if (isPlaneFound.value && selectedPlaneHit.value == null) {
                        showSelectPrompt.value = true
                    }
                }
            },
            sessionConfiguration = { session, config ->
                config.depthMode = if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    Config.DepthMode.AUTOMATIC
                } else {
                    Config.DepthMode.DISABLED
                }
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            },
            onGestureListener = rememberOnGestureListener(
                onSingleTapConfirmed = { e: MotionEvent, node: Node? ->
                    if (node == null && frame.value != null) {
                        val hitTestResults = frame.value?.hitTest(e.x, e.y)
                        val hitResult = hitTestResults?.firstOrNull {
                            it.isValid(depthPoint = false, point = false)
                        }
                        when {
                            selectedPlaneHit.value == null && hitResult != null -> {
                                selectedPlaneHit.value = hitResult
                                showSelectPrompt.value = false
                                val plane = hitResult.trackable as? Plane
                                plane?.let {
                                    planeArea.value = calculatePlaneArea(it.polygon)
                                    showPlacePrompt.value = true
                                }
                            }
                            selectedPlaneHit.value != null && !hasPlacedModels.value && hitResult != null -> {
                                val selectedPlane = selectedPlaneHit.value!!.trackable as Plane
                                if (hitResult.trackable == selectedPlane) {
                                    val anchor = hitResult.createAnchorOrNull()
                                    anchor?.let {
                                        val area = planeArea.value ?: 0f
                                        val maxModelsByArea = floor(area / (totalModelSize * totalModelSize)).toInt()
                                        val maxModels = minOf(maxModelsByArea, GlobalKit.bestLimitedCount)
                                        println("GlobalKit.bestLimitedCount = ${GlobalKit.bestLimitedCount}")
                                        Log.d("----ARCameraScreen", "GlobalKit.bestLimitedCount = ${GlobalKit.bestLimitedCount}")
                                        Log.d("------ARCameraScreen", "Max models by area = $maxModelsByArea")
                                        Log.d("--------ARCameraScreen", "Using maxModels = $maxModels")
                                        val modelsPlaced = placeModels(
                                            engine,
                                            modelLoader,
                                            materialLoader,
                                            modelInstance,
                                            it,
                                            selectedPanel.value.modelPath,
                                            modelSize,
                                            spacing,
                                            maxModels
                                        )
                                        childNodes += modelsPlaced
                                        hasPlacedModels.value = true
                                        showPlacePrompt.value = false
                                        placementFeedback.value = "Placed ${modelsPlaced.size} models"
                                    }
                                }
                            }
                        }
                    }
                }
            )
        )

        if (!isPlaneFound.value) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                Text("Searching for a plane...", modifier = Modifier.padding(top = 16.dp))
            }
        }

        if (showSelectPrompt.value) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(8.dp)
            ) {
                Text("Tap to select plane", color = Color.White, fontSize = 16.sp)
            }
        }

        if (showPlacePrompt.value) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(8.dp)
            ) {
                Text("Tap to place models", color = Color.White, fontSize = 16.sp)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            planeArea.value?.let { area ->
                Text(
                    "Surface Area: %.2f m²".format(area),
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(8.dp)
                )
            }
            placementFeedback.value?.let { feedback ->
                Text(
                    feedback,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(8.dp)
                )
            }

            // Panel selection buttons row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                solarPanelModels.forEach { panel ->
                    Button(
                        onClick = {
                            selectedPanel.value = panel
                            hasPlacedModels.value = false // Allow placement again
                            childNodes.clear() // Remove previously placed models
                            modelInstance.clear() // Clear model instances
                            Log.d("ARCameraScreen", "Selected panel changed to: ${selectedPanel}")
                            // Or println("Selected panel changed to: ${panel.name}")
                        },
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(vertical = 4.dp),
                        colors = if (panel == selectedPanel.value) {
                            ButtonDefaults.buttonColors(containerColor = Color.Green)
                        } else {
                            ButtonDefaults.buttonColors()
                        }
                    ) {
                        Text(text = panel.name, fontSize = 12.sp)
                    }
                }
            }

            Button(onClick = { navController.navigate(HomeScreen) }) {
                Text("Back to Main")
            }
        }
    }
}

fun calculatePlaneArea(polygon: FloatBuffer): Float {
    val vertices = mutableListOf<Pair<Float, Float>>()
    polygon.rewind()
    while (polygon.hasRemaining()) {
        val x = polygon.get()
        val z = polygon.get()
        vertices.add(Pair(x, z))
    }
    if (vertices.size < 3) return 0f
    var area = 0f
    for (i in vertices.indices) {
        val j = (i + 1) % vertices.size
        area += vertices[i].first * vertices[j].second
        area -= vertices[j].first * vertices[i].second
    }
    return kotlin.math.abs(area) / 2f
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun placeModels(
    engine: com.google.android.filament.Engine,
    modelLoader: io.github.sceneview.loaders.ModelLoader,
    materialLoader: io.github.sceneview.loaders.MaterialLoader,
    modelInstance: MutableList<ModelInstance>,
    anchor: Anchor,
    modelPath: String,
    scale: Float,
    spacing: Float,
    maxModels: Int
): List<AnchorNode> {
    val anchorNode = AnchorNode(engine = engine, anchor = anchor)
    val nodes = mutableListOf<AnchorNode>()
    val gridSize = sqrt(maxModels.toDouble()).toInt()
    val totalSize = scale + spacing
    for (i in 0 until gridSize) {
        for (j in 0 until gridSize) {
            val index = i * gridSize + j
            if (index >= maxModels) break
            val node = Utils.createAnchorNode(
                engine = engine,
                modelLoader = modelLoader,
                materialLoader = materialLoader,
                modelInstance = modelInstance,
                anchor = anchor,
                modelPath = modelPath,
                scale = scale
            )
            node.position = Position(x = i * totalSize, z = j * totalSize)
            anchorNode.addChildNode(node)
            nodes.add(node)
        }
    }
    return listOf(anchorNode)
}

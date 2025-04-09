package com.example.arlearner2.ui.theme.screens

import android.os.Build
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.arlearner2.ui.theme.navigation.HomeScreen
import com.example.arlearner2.util.Utils
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingFailureReason
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
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

    // Default model and size
    val defaultModelPath = "models/1_meter_cube.glb"
    val modelSize = 0.2f // 0.2m x 0.2m footprint
    val spacing = 0.1f // 0.1m spacing
    val totalModelSize = modelSize + spacing // 0.3m per model in each direction

    // Hide plane renderer 2 seconds after placement
    LaunchedEffect(hasPlacedModels.value) {
        if (hasPlacedModels.value) {
            delay(2000L)
            planeRenderer.value = false
            delay(1000L) // Show feedback for 1 more second
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
                config.depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    true -> Config.DepthMode.AUTOMATIC
                    else -> Config.DepthMode.DISABLED
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
                            // First tap: Select the plane
                            selectedPlaneHit.value == null && hitResult != null -> {
                                selectedPlaneHit.value = hitResult
                                showSelectPrompt.value = false
                                val plane = hitResult.trackable as? Plane
                                plane?.let {
                                    planeArea.value = calculatePlaneArea(it.polygon)
                                    showPlacePrompt.value = true
                                }
                            }
                            // Second tap: Place multiple models
                            selectedPlaneHit.value != null && !hasPlacedModels.value && hitResult != null -> {
                                val selectedPlane = selectedPlaneHit.value!!.trackable as Plane
                                if (hitResult.trackable == selectedPlane) {
                                    val anchor = hitResult.createAnchorOrNull()
                                    anchor?.let {
                                        val area = planeArea.value ?: 0f
                                        val maxModels = floor(area / (totalModelSize * totalModelSize)).toInt()
                                        val gridSize = floor(sqrt(maxModels.toDouble())).toInt()
                                        val modelsPlaced = placeModels(
                                            engine = engine,
                                            modelLoader = modelLoader,
                                            materialLoader = materialLoader,
                                            modelInstance = modelInstance,
                                            anchor = it,
                                            modelPath = defaultModelPath,
                                            scale = modelSize,
                                            spacing = spacing,
                                            maxModels = gridSize * gridSize
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

        // Loading indicator
        if (!isPlaneFound.value) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Searching for a plane...",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        // Select plane prompt
        if (showSelectPrompt.value) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "Tap to select plane",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }

        // Place models prompt
        if (showPlacePrompt.value) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "Tap to place models",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }

        // Surface area and feedback at bottom center
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            planeArea.value?.let { area ->
                Text(
                    text = "Surface Area: %.2f m²".format(area),
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(8.dp)
                )
            }
            placementFeedback.value?.let { feedback ->
                Text(
                    text = feedback,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(8.dp)
                )
            }
            Button(onClick = { navController.navigate(HomeScreen) }) {
                Text("Back to Main")
            }
        }
    }
}

// Helper functions
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
    for (i in 0 until vertices.size) {
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
    anchor: com.google.ar.core.Anchor,
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
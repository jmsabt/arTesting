    package com.example.arlearner2.ui.theme.screens

    import android.os.Build
    import android.view.MotionEvent
    import androidx.annotation.RequiresApi
    import androidx.compose.foundation.background
    import androidx.compose.foundation.layout.*
    import androidx.compose.material3.Button
    import androidx.compose.material3.CircularProgressIndicator
    import androidx.compose.material3.Text
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.navigation.NavController
    import com.example.arlearner2.ui.theme.navigation.GalleryScreen
    import com.example.arlearner2.util.Utils
    import com.example.arlearner2.util.loadModelInfoFromJson
    import com.google.ar.core.Config
    import com.google.ar.core.Frame
    import com.google.ar.core.TrackingFailureReason
    import io.github.sceneview.ar.ARScene
    import io.github.sceneview.ar.arcore.createAnchorOrNull
    import io.github.sceneview.ar.arcore.isValid
    import io.github.sceneview.ar.rememberARCameraNode
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

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    @Composable
    fun ARScreen(navController: NavController, modelPath: String) {
        val context = LocalContext.current
        val modelInfos = remember { loadModelInfoFromJson(context) }
        val selectedModel = modelInfos.firstOrNull { it.path == modelPath } ?: modelInfos.firstOrNull() // Fallback to first model if not found

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
        val showTapPrompt = remember { mutableStateOf(false) }
        val hasPlacedModel = remember { mutableStateOf(false) }

        // Hide plane renderer 2 seconds after model placement
        LaunchedEffect(hasPlacedModel.value) {
            if (hasPlacedModel.value) {
                delay(2000L)
                planeRenderer.value = false
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
                    if (isPlaneFound.value && !hasPlacedModel.value) {
                        showTapPrompt.value = true
                    }
                },
                onSessionUpdated = { _, updatedFrame ->
                    frame.value = updatedFrame
                    if (!isPlaneFound.value) {
                        isPlaneFound.value = updatedFrame.hasDetectedPlanes()
                        if (isPlaneFound.value && !hasPlacedModel.value) {
                            showTapPrompt.value = true
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
                        if (node == null && !hasPlacedModel.value && selectedModel != null) {
                            val hitTestResults = frame.value?.hitTest(e.x, e.y)
                            hitTestResults?.firstOrNull {
                                it.isValid(depthPoint = false, point = false)
                            }?.createAnchorOrNull()?.let {
                                val nodeModel = Utils.createAnchorNode(
                                    engine = engine,
                                    modelLoader = modelLoader,
                                    materialLoader = materialLoader,
                                    modelInstance = modelInstance,
                                    anchor = it,
                                    modelPath = selectedModel.path
                                )
                                childNodes += nodeModel
                                hasPlacedModel.value = true
                                showTapPrompt.value = false
                            }
                        }
                    }
                )
            )

            // Loading indicator while searching for a plane
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

            // Tap prompt as a pop-up at the top
            if (showTapPrompt.value) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.TopCenter)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Tap on the plane to place the model",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }

            // Error message if model not found
            if (selectedModel == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.Center)
                        .padding(16.dp)
                        .background(Color.Red.copy(alpha = 0.7f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Error: Model not found",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }

            // Back button
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                Button(onClick = { navController.navigate(GalleryScreen) }) {
                    Text("Back to Gallery")
                }
            }
        }
    }

    fun Frame.hasDetectedPlanes(): Boolean {
        return this.getUpdatedTrackables(com.google.ar.core.Plane::class.java).isNotEmpty()
    }
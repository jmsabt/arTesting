package com.example.arlearner2.ui.theme.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.arlearner2.ui.theme.navigation.ARScreen
import com.example.arlearner2.ui.theme.navigation.HomeScreen
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
        ModelInfo("Cube", "1 x 1 x 1 m", "Just a cube.")
    )

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    var currentIndex by remember { mutableStateOf(0) }
    val totalModels = modelInfos.size

    // Animation for model switch
    val scale by animateFloatAsState(targetValue = if (currentIndex % 2 == 0) 1f else 1.05f)

    // Preload all models
    val modelNodes = remember(modelPaths) {
        modelPaths.map { path ->
            ModelNode(
                modelInstance = modelLoader.createModelInstance(path),
                autoAnimate = true,
                scaleToUnits = 0.35f * scale // Reduced from 0.5f to prevent clipping
            ).apply { centerOrigin() }
        }
    }

    val childNodes = rememberNodes()
    childNodes.clear()
    childNodes += modelNodes[currentIndex]

    // Navigation functions
    fun nextModel() {
        currentIndex = (currentIndex + 1) % totalModels
    }

    fun prevModel() {
        currentIndex = if (currentIndex - 1 < 0) totalModels - 1 else currentIndex - 1
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Model Name (10%) - Light Blue background
        Column(
            modifier = Modifier
                .weight(0.1f) // Adjust this weight to change Model Name section size
                .fillMaxWidth()
                //.background(Color(0xFFBBDEFB)) // Light Blue for visualization
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = modelInfos[currentIndex].name,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20.sp
            )
        }

        // Model Preview (70%) - Light Green background
        Box(
            modifier = Modifier
                .weight(0.7f) // Adjust this weight to change Model Preview section size
                .fillMaxWidth()
                //.background(Color(0xFFC8E6C9)) // Light Green for visualization
        ) {
            Scene(
                modifier = Modifier
                    .fillMaxSize() // Use full 70% space, no spacer
                    .padding(top = 48.dp), // Lower by fixed amount (~10% of section)
                engine = engine,
                childNodes = childNodes,
                isOpaque = false
            )
        }

        // Description + Buttons (20%) - Light Yellow background
        Card(
            modifier = Modifier
                .weight(0.2f) // Adjust this weight to change Description + Buttons section size
                .fillMaxWidth()
                //.background(Color(0xFFFFF9C4)) // Light Yellow for visualization
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Dimensions: ${modelInfos[currentIndex].dimensions}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = modelInfos[currentIndex].description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = { prevModel() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Previous",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Button(
                        onClick = { navController.navigate(ARScreen(modelPaths[currentIndex])) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "View in AR",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            fontSize = 14.sp
                        )
                    }
                    IconButton(
                        onClick = { nextModel() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Next",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                Button(
                    onClick = { navController.navigate(HomeScreen) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Back to Home",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
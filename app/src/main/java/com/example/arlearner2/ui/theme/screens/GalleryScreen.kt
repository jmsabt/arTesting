package com.example.arlearner2.ui.theme.screens

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign


data class ModelInfo(
    val name: String,
    val dimensions: String,
    val description: String,
    val path: String
)

fun loadModelInfoFromJson(context: Context): List<ModelInfo> {
    val jsonString = context.assets.open("models.json").bufferedReader().use { it.readText() }
    val listType = object : TypeToken<List<ModelInfo>>() {}.type
    return Gson().fromJson(jsonString, listType)
}


@Composable
fun GalleryScreen(navController: NavController) {
    val context = LocalContext.current
    val modelInfos = remember { loadModelInfoFromJson(context) }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)

    var currentIndex by remember { mutableStateOf(0) }
    val totalModels = modelInfos.size

    val scale by animateFloatAsState(targetValue = if (currentIndex % 2 == 0) 1f else 1.05f)

    // Preload model nodes
    val modelNodes = remember(modelInfos) {
        modelInfos.map { info ->
            ModelNode(
                modelInstance = modelLoader.createModelInstance(info.path),
                autoAnimate = true,
                scaleToUnits = 0.35f * scale
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
        // Model Name
        Column(
            modifier = Modifier
                .weight(0.1f)
                .fillMaxWidth()
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

        // Model Preview
        Box(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxWidth()
        ) {
            Scene(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp),
                engine = engine,
                childNodes = childNodes,
                isOpaque = false
            )
        }

        // Description + Buttons
        Card(
            modifier = Modifier
                .weight(0.2f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Dimensions: ${modelInfos[currentIndex].dimensions}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                val scrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .height(80.dp) // Adjust height if needed
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(bottom = 2.dp),
                    contentAlignment = Alignment.Center


                )

                {
                    Text(
                        text = modelInfos[currentIndex].description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
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
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                    Button(
                        onClick = { navController.navigate(ARScreen(modelInfos[currentIndex].path)) },
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

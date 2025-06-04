package com.example.arlearner2.ui.theme.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.arlearner2.ui.theme.ARLearner2Theme
import kotlin.math.floor
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.nativeCanvas
import com.example.arlearner2.ui.theme.navigation.ARCameraScreen

fun filterDecimalInput(input: String): String {
    return input.filterIndexed { index, c ->
        c.isDigit() || (c == '.' && input.indexOf('.') == index)
    }
}

data class Panel(val model: String, val power: Int, val efficiency: Double, val area: Double, val price: Float)

@Composable
fun MultiLineChart(
    dataSets: List<List<Float>>,
    labels: List<String>,
    panelNames: List<String>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    if (dataSets.isEmpty() || panelNames.size != dataSets.size || colors.size != dataSets.size) return

    Canvas(modifier = modifier.padding(16.dp)) {
        val widthStep = if (labels.size > 1) size.width / (labels.size - 1) else size.width
        val maxValue = dataSets.flatten().maxOrNull() ?: 0f
        val minValue = dataSets.flatten().minOrNull() ?: 0f
        val heightScale = if (maxValue - minValue == 0f) 1f else size.height / (maxValue - minValue)

        dataSets.forEachIndexed { setIndex, dataPoints ->
            val color = colors.getOrNull(setIndex) ?: Color.Black
            val points = dataPoints.mapIndexed { index, value ->
                Offset(
                    x = index * widthStep,
                    y = size.height - (value - minValue) * heightScale
                )
            }

            for (i in 0 until points.size - 1) {
                drawLine(
                    color = color,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 3f
                )
            }

            points.forEach { point ->
                drawCircle(color = color, radius = 5f, center = point)
            }
        }

        val paint = android.graphics.Paint().apply {
            textSize = 30f
            color = android.graphics.Color.BLACK
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        labels.forEachIndexed { index, label ->
            val x = if (labels.size > 1) index * widthStep else size.width / 2f
            val y = size.height + paint.textSize + 2f
            drawContext.canvas.nativeCanvas.drawText(label, x, y, paint)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        panelNames.forEachIndexed { index, name ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp, 12.dp)
                        .background(colors.getOrNull(index) ?: Color.Black)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(name, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun PrettyAreaPercentageSlider(
    areaPercentage: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Roof Area to Use: ${areaPercentage.toInt()}%",
            style = MaterialTheme.typography.titleMedium
        )

        Slider(
            value = areaPercentage,
            onValueChange = onValueChange,
            valueRange = 20f..100f,
            steps = 10,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primaryContainer,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                activeTickColor = MaterialTheme.colorScheme.primary,
                inactiveTickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoofInpotScreen(navController: NavController, weatherViewModel: WeatherViewModel = viewModel()) {
    ARLearner2Theme {
        val scrollState = rememberScrollState()
        var showDialog by remember { mutableStateOf(false) }
        var dialogText by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { navController.popBackStack() }) {
                    Text("Back")
                }

                Button(onClick = { navController.navigate(ARCameraScreen) }) {
                    Text("Go to AR")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            var widthText by remember { mutableStateOf("") }
            var lengthText by remember { mutableStateOf("") }
            var location by remember { mutableStateOf("") }
            var expanded by remember { mutableStateOf(false) }
            var showError by remember { mutableStateOf(false) }

            var monthlyKwhText by remember { mutableStateOf("") }
            var areaPercentage by remember { mutableStateOf(50f) }

            var budgetText by remember { mutableStateOf("") }

            val locationOptions = listOf(
                "Caloocan", "Las Piñas", "Makati", "Malabon", "Mandaluyong",
                "Manila", "Marikina", "Muntinlupa", "Navotas", "Parañaque",
                "Pasay", "Pasig", "Quezon City", "San Juan", "Taguig", "Valenzuela", "Pateros"
            )

            val width = widthText.toFloatOrNull()
            val length = lengthText.toFloatOrNull()
            val area = if (width != null && length != null) width * length else null

            LaunchedEffect(location) {
                if (location.isNotEmpty()) weatherViewModel.fetchWeather(location)
            }

            val weatherState by weatherViewModel.weatherState.collectAsState()
            val errorState by weatherViewModel.errorState.collectAsState()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text("Location:")
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Location") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        locationOptions.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    location = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                weatherState?.main?.temp?.let { temp ->
                    Text(
                        "Temperature: ${"%.1f".format(temp)} °C",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (errorState != null) {
                    Text("Error: $errorState", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text("Roof Surface :")
                OutlinedTextField(
                    value = widthText,
                    onValueChange = { widthText = filterDecimalInput(it) },
                    label = { Text("Width (m)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = lengthText,
                    onValueChange = { lengthText = filterDecimalInput(it) },
                    label = { Text("Length (m)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                area?.let {
                    Text("Calculated Area: ${"%.2f".format(it)} m²")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Energy Usage:")

                OutlinedTextField(
                    value = monthlyKwhText,
                    onValueChange = { monthlyKwhText = filterDecimalInput(it) },
                    label = { Text("Monthly Energy Usage (kWh)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = filterDecimalInput(it) },
                    label = { Text("Budget (PHP)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                PrettyAreaPercentageSlider(
                    areaPercentage = areaPercentage,
                    onValueChange = { areaPercentage = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (showError) {
                    Text(
                        "Please fill all fields correctly",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (width != null && length != null && location.isNotBlank()) {
                    val roofArea = width * length
                    val monthlyKwh = monthlyKwhText.toFloatOrNull()
                    val budget = budgetText.toFloatOrNull()
                    val usableArea = roofArea * (areaPercentage / 100f)
                    val solarIrradiance = 5f
                    val systemLoss = 0.80f
                    val dailyKwhDemand = monthlyKwh?.div(30f)

                    val temp = (weatherState?.main?.temp ?: 25.0).toFloat()

                    val tempEffectFactor = if (temp > 25f) {
                        1f - 0.005f * (temp - 25f)
                    } else 1f

                    val panels = listOf(
                        Panel("AE CMER-132BDS-610", 610, 0.2260, 2.70, 6000f),
                        Panel("AE CMER-132BDS-605", 605, 0.2240, 2.70, 5800f),
                        Panel("AE CMER-132BDS-600", 600, 0.2220, 2.70, 5500f),
                        Panel("AE CMER-132BDS-595", 595, 0.2200, 2.70, 5300f),
                        Panel("AE CMER-132BDS-590", 590, 0.2180, 2.70, 5000f)
                    )

                    if (monthlyKwh != null && monthlyKwh > 0f &&
                        areaPercentage in 20f..100f &&
                        dailyKwhDemand != null && dailyKwhDemand > 0f &&
                        budget != null && budget > 0f
                    ) {
                        Text(
                            "🌡 Adjusted efficiency factor due to temperature: ${"%.2f".format(tempEffectFactor)}"
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        var bestPanel: Panel? = null
                        var bestOutputLimited = 0f
                        var bestLimitedCount = 0

                        val outputsLimitedPerPanel = mutableListOf<List<Float>>()
                        val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Magenta, Color.Cyan)

                        val detailedInfoBuilder = StringBuilder()

                        panels.forEachIndexed { index, panel ->
                            val panelCountFull = floor(roofArea / panel.area).toInt()
                            val panelCountLimited = floor(usableArea / panel.area).toInt()
                            val dailyOutputPerPanel =
                                panel.power * panel.efficiency.toFloat() * solarIrradiance * systemLoss * tempEffectFactor / 1000f
                            val totalOutputLimited = panelCountLimited * dailyOutputPerPanel

                            val maxAffordableCount = (budget / panel.price).toInt()

                            val recommendedCount = minOf(panelCountLimited, maxAffordableCount)
                            val recommendedOutput = recommendedCount * dailyOutputPerPanel

                            val percentages = listOf(0f, 25f, 50f, 75f, 100f, 125f, 150f)

                            val outputList = percentages.map { pct ->
                                val count = (recommendedCount * pct / 100f).toInt()
                                count * dailyOutputPerPanel
                            }

                            outputsLimitedPerPanel.add(outputList)

                            // Build the detailed info string here
                            detailedInfoBuilder.appendLine("Panel: ${panel.model}")
                            detailedInfoBuilder.appendLine("Area per panel: ${panel.area} m²")
                            detailedInfoBuilder.appendLine("Power rating: ${panel.power} W")
                            detailedInfoBuilder.appendLine("Efficiency: ${panel.efficiency}")
                            detailedInfoBuilder.appendLine("Price per panel: ₱${panel.price}")
                            detailedInfoBuilder.appendLine("Max panels (roof area): $panelCountFull")
                            detailedInfoBuilder.appendLine("Max panels (usable area): $panelCountLimited")
                            detailedInfoBuilder.appendLine("Max affordable panels (budget): $maxAffordableCount")
                            detailedInfoBuilder.appendLine("Recommended panel count: $recommendedCount")
                            detailedInfoBuilder.appendLine("Estimated daily output at recommended count: ${"%.2f".format(recommendedOutput)} kWh")
                            detailedInfoBuilder.appendLine()

                            if (recommendedOutput >= dailyKwhDemand && recommendedOutput > bestOutputLimited) {
                                bestPanel = panel
                                bestOutputLimited = recommendedOutput
                                bestLimitedCount = recommendedCount
                            }
                        }

                        // Show a button to open the detailed info dialog
                        Button(onClick = {
                            dialogText = detailedInfoBuilder.toString()
                            showDialog = true
                        }) {
                            Text("Show Details")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (bestPanel != null) {
                            Text(
                                "Recommended panel: ${bestPanel.model} with $bestLimitedCount panels"
                            )
                            Text(
                                "Estimated daily output: ${"%.2f".format(bestOutputLimited)} kWh (vs demand ${"%.2f".format(dailyKwhDemand)})"
                            )
                        } else {
                            Text(
                                "No panel setup meets your daily demand within your budget and roof constraints."
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        MultiLineChart(
                            dataSets = outputsLimitedPerPanel,
                            labels = listOf("0%", "25%", "50%", "75%", "100%", "125%", "150%"),
                            panelNames = panels.map { it.model },
                            colors = colors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        )
                    }
                }
            }
        }

        // Dialog to show detailed panel info
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Close")
                    }
                },
                title = { Text("Detailed Panel Info") },
                text = {
                    // Use vertical scroll if content is too long
                    Box(modifier = Modifier.heightIn(max = 400.dp)) {
                        androidx.compose.foundation.rememberScrollState().let { scrollState ->
                            androidx.compose.foundation.layout.Column(
                                modifier = Modifier.verticalScroll(scrollState)
                            ) {
                                Text(dialogText)
                            }
                        }
                    }
                }
            )
        }
    }
}


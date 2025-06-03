package com.example.arlearner2.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.arlearner2.ui.theme.ARLearner2Theme
import com.example.arlearner2.ui.theme.screens.WeatherViewModel
import com.example.arlearner2.R
import com.example.arlearner2.ui.theme.navigation.ARCameraScreen
import org.json.JSONObject

fun filterDecimalInput(input: String): String {
    return input.filterIndexed { index, c ->
        c.isDigit() || (c == '.' && input.indexOf('.') == index)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoofInpotScreen(navController: NavController, weatherViewModel: WeatherViewModel = viewModel()) {
    ARLearner2Theme {
        Box(modifier = Modifier.fillMaxSize()) {
            // Top 40% - Logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
                    .align(Alignment.TopCenter)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.solar_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.Center)
                )
            }

            // Bottom 60% - Inputs and buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var widthText by remember { mutableStateOf("") }
                var lengthText by remember { mutableStateOf("") }

                val locationOptions = listOf(
                    "Caloocan", "Las Piñas", "Makati", "Malabon", "Mandaluyong",
                    "Manila", "Marikina", "Muntinlupa", "Navotas", "Parañaque",
                    "Pasay", "Pasig", "Quezon City", "San Juan", "Taguig", "Valenzuela", "Pateros"
                )

                var location by remember { mutableStateOf("") }
                var expanded by remember { mutableStateOf(false) }

                var showError by remember { mutableStateOf(false) }

                val width = widthText.toFloatOrNull()
                val length = lengthText.toFloatOrNull()
                val area = if (width != null && length != null) width * length else null

                // Trigger weather fetch whenever location changes
                LaunchedEffect(location) {
                    if (location.isNotEmpty()) {
                        weatherViewModel.fetchWeather(location)
                    }
                }

                // Observe weather state from ViewModel
                val weatherState by weatherViewModel.weatherState.collectAsState()
                val errorState by weatherViewModel.errorState.collectAsState()

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

                // Show temperature if available
                weatherState?.let {
                    Text(
                        text = "Temperature: %.1f °C".format(it.main.temp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // Show error if any
                if (errorState != null) {
                    Text(
                        text = "Error: $errorState",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                OutlinedTextField(
                    value = widthText,
                    onValueChange = { widthText = filterDecimalInput(it) },
                    label = { Text("Width (m)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = lengthText,
                    onValueChange = { lengthText = filterDecimalInput(it) },
                    label = { Text("Length (m)") },
                    modifier = Modifier.fillMaxWidth()
                )

                area?.let {
                    Text(
                        text = "Calculated Area: %.2f m²".format(it),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (showError) {
                    Text(
                        "Please enter valid width, length and location",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (width != null && length != null && location.isNotBlank()) {
                            navController.currentBackStackEntry?.savedStateHandle?.apply {
                                set("width", width)
                                set("length", length)
                                set("location", location)
                            }
                            navController.navigate(ARCameraScreen)
                        } else {
                            showError = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "Go to AR Screen",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    }
}

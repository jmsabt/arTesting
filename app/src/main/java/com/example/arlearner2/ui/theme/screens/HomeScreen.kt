package com.example.arlearner2.ui.theme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.arlearner2.ui.theme.navigation.AboutScreen
import com.example.arlearner2.ui.theme.navigation.ARCameraScreen
import com.example.arlearner2.ui.theme.navigation.GalleryScreen

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { navController.navigate(ARCameraScreen) }) {
            Text(text = "AR Camera")
        }
        Button(onClick = { navController.navigate(GalleryScreen) }) {
            Text(text = "Gallery")
        }
        Button(onClick = { navController.navigate(AboutScreen) }) {
            Text(text = "About")
        }
    }
}
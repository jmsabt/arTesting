package com.example.arlearner2

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.arlearner2.ui.theme.ARLearner2Theme
import com.example.arlearner2.ui.theme.navigation.AboutScreen
import com.example.arlearner2.ui.theme.navigation.ARCameraScreen
import com.example.arlearner2.ui.theme.navigation.ARScreen
import com.example.arlearner2.ui.theme.navigation.GalleryScreen
import com.example.arlearner2.ui.theme.navigation.HomeScreen
import com.example.arlearner2.ui.theme.navigation.RoofInpotScreen
import com.example.arlearner2.ui.theme.navigation.SolarCalcScreen
import com.example.arlearner2.ui.theme.screens.AboutScreen
import com.example.arlearner2.ui.theme.screens.ARCameraScreen
import com.example.arlearner2.ui.theme.screens.ARScreen
import com.example.arlearner2.ui.theme.screens.GalleryScreen
import com.example.arlearner2.ui.theme.screens.HomeScreen
import com.example.arlearner2.ui.theme.screens.RoofInpotScreen


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ARLearner2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = HomeScreen,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable<HomeScreen> {
                            HomeScreen(navController)
                        }

                        composable<RoofInpotScreen> {
                            RoofInpotScreen(navController)
                        }
                        composable<AboutScreen> {
                            AboutScreen(navController)
                        }
                        composable<ARScreen> { backStackEntry ->
                            val args = backStackEntry.toRoute<ARScreen>()
                            ARScreen(navController, args.modelPath)
                        }
                        composable<GalleryScreen> {
                            GalleryScreen(navController)
                        }
                        composable<ARCameraScreen> {
                            ARCameraScreen(navController)
                        }

                    }
                }
            }
        }
    }
}
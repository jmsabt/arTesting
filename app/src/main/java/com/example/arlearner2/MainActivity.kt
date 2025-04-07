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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.arlearner2.ui.theme.ARLearner2Theme
import com.example.arlearner2.ui.theme.navigation.ARScreen
import com.example.arlearner2.ui.theme.navigation.AlphabetScreen
import com.example.arlearner2.ui.theme.navigation.HomeScreen
import com.example.arlearner2.ui.theme.navigation.QuizScreen
import com.example.arlearner2.ui.theme.screens.ARScreen
import com.example.arlearner2.ui.theme.screens.AlphabetScreen
import com.example.arlearner2.ui.theme.screens.HomeScreen

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
                        composable <HomeScreen> {
                            HomeScreen(navController)
                        }
                        composable <ARScreen> {
                            val alphabet = it.toRoute<ARScreen>().model
                            ARScreen(navController, alphabet)
                        }
                        composable <AlphabetScreen> {
                            AlphabetScreen(navController)
                        }

                        composable <QuizScreen> {

                        }
                    }
                }
            }
        }
    }
}
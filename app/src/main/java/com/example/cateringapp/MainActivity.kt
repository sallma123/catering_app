package com.example.cateringapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Scaffold
import com.example.cateringapp.ui.navigation.BottomNavBar
import com.example.cateringapp.ui.theme.CateringAppTheme
import com.example.cateringapp.ui.navigation.NavigationHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CateringAppTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = { BottomNavBar(navController) }
                ) { innerPadding ->
                    NavigationHost(navController = navController, padding = innerPadding)
                }
            }
        }
    }
}

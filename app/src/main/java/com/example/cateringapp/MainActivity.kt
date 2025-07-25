package com.example.cateringapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Scaffold
import com.example.cateringapp.ui.navigation.BottomNavBar
import com.example.cateringapp.ui.theme.CateringAppTheme
import com.example.cateringapp.ui.navigation.NavigationHost
import com.example.cateringapp.viewmodel.CommandeViewModel

class MainActivity : ComponentActivity() {

    // ✅ ViewModel partagé entre tous les écrans
    val commandeViewModel by viewModels<CommandeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CateringAppTheme {
                val navController = rememberNavController()

                Scaffold(
                    bottomBar = { BottomNavBar(navController) }
                ) { innerPadding ->
                    // ✅ On passe le ViewModel à NavigationHost
                    NavigationHost(
                        navController = navController,
                        padding = innerPadding,
                        commandeViewModel = commandeViewModel
                    )
                }
            }
        }
    }
}

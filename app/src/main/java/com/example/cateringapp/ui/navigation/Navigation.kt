package com.example.cateringapp.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cateringapp.ui.screen.commandes.CommandesScreen
import com.example.cateringapp.ui.screen.paiement.PaiementScreen
import com.example.cateringapp.ui.screen.calendrier.CalendrierScreen
import com.example.cateringapp.ui.screen.profil.ProfilScreen
import com.example.cateringcompose.ui.NavigationBarItems

@Composable
fun NavigationHost(navController: NavHostController, padding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = NavigationBarItems.Commandes.route
    ) {
        composable(NavigationBarItems.Commandes.route) { CommandesScreen() }
        composable(NavigationBarItems.Paiement.route) { PaiementScreen() }
        composable(NavigationBarItems.Calendrier.route) { CalendrierScreen() }
        composable(NavigationBarItems.Profil.route) { ProfilScreen() }
    }
}

package com.example.cateringapp.ui.navigation

import CommandeDTO
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.cateringapp.ui.screen.commandes.*
import com.example.cateringapp.ui.screen.paiement.PaiementScreen
import com.example.cateringapp.ui.screen.calendrier.CalendrierScreen
import com.example.cateringapp.ui.screen.profil.ProfilScreen
import com.example.cateringapp.ui.screen.profil.UploadHeaderFooterScreen
import com.example.cateringcompose.ui.NavigationBarItems

@Composable
fun NavigationHost(navController: NavHostController, padding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = NavigationBarItems.Commandes.route
    ) {
        // Onglets du menu principal
        composable(NavigationBarItems.Commandes.route) { CommandesScreen(navController) }
        composable(NavigationBarItems.Paiement.route) { PaiementScreen() }
        composable(NavigationBarItems.Calendrier.route) { CalendrierScreen() }
        composable(NavigationBarItems.Profil.route) {
            ProfilScreen(navController)
        }



        // ✅ Page 1 : Création commande (avec paramètre typeClient)
        composable("creerCommande/{typeClient}") { backStackEntry ->
            val typeClient = backStackEntry.arguments?.getString("typeClient") ?: "PARTICULIER"
            CreerCommandeScreen(typeClient = typeClient, navController = navController)
        }

        // ✅ Page 2 : Sélection des produits (via SavedStateHandle)
        composable("selectionProduits") {
            val commande = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<CommandeDTO>("commande")

            if (commande != null) {
                SelectionProduitsScreen(commandeDTO = commande, navController = navController)
            }
        }

        // ✅ Page 3 : Fiche commande
        composable(
            "ficheCommande/{id}",
            arguments = listOf(navArgument("id") { defaultValue = "0" })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: 0L
            FicheCommandeScreen(id = id)
        }
        composable("uploadHeaderFooter") {
            UploadHeaderFooterScreen()
        }


    }
}

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
        composable(NavigationBarItems.Calendrier.route) {
            CalendrierScreen(navController = navController)
        }
        composable(NavigationBarItems.Profil.route) {
            ProfilScreen(navController)
        }



        // ✅ Page 1 : Création commande (avec paramètre typeClient)
        composable("creerCommande/{typeClient}") { backStackEntry ->
            val typeClient = backStackEntry.arguments?.getString("typeClient") ?: "PARTICULIER"

            // 🆕 Nouvelle ligne : récupération de la commande existante
            val commandeExistante = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<CommandeDTO>("commandeExistante")

            // 🆕 On passe cette commande à l’écran
            CreerCommandeScreen(
                typeClient = typeClient,
                navController = navController,
                commandeInitiale = commandeExistante
            )
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
        composable("ficheCommande/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLong() ?: 0
            FicheCommandeScreen(id = id, navController = navController)
        }

        composable("uploadHeaderFooter") {
            UploadHeaderFooterScreen()
        }


    }
}

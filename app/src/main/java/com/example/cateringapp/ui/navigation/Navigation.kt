package com.example.cateringapp.ui.navigation

import CommandeDTO
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.example.cateringapp.ui.screen.auth.ForgotPasswordScreen
import com.example.cateringapp.ui.screen.auth.ResetPasswordScreen
import com.example.cateringapp.ui.screen.commandes.*
import com.example.cateringapp.ui.screen.paiements.PaiementsScreen
import com.example.cateringapp.ui.screen.calendrier.CalendrierScreen
import com.example.cateringapp.ui.screen.corbeille.CorbeilleScreen
import com.example.cateringapp.ui.screen.paiements.AvancesCommandeScreen
import com.example.cateringapp.ui.screen.profil.ChangerMotDePasseScreen
import com.example.cateringapp.ui.screen.profil.ProfilScreen
import com.example.cateringapp.ui.screen.profil.UploadHeaderFooterScreen
import com.example.cateringapp.viewmodel.CommandeViewModel
import com.example.cateringcompose.ui.NavigationBarItems

@Composable
fun NavigationHost(
    navController: NavHostController,
    padding: PaddingValues,
    commandeViewModel: CommandeViewModel // ✅ On reçoit le ViewModel partagé ici
) {
    NavHost(
        navController = navController,
        startDestination = NavigationBarItems.Commandes.route
    ) {
        // Onglets du menu principal
        composable(NavigationBarItems.Commandes.route) {
            CommandesScreen(navController)
        }
        composable(NavigationBarItems.Paiement.route) {
            PaiementsScreen(navController)
        }
        composable(NavigationBarItems.Calendrier.route) {
            // ✅ On passe le même ViewModel ici aussi
            CalendrierScreen(navController = navController, viewModel = commandeViewModel)
        }
        composable(NavigationBarItems.Profil.route) {
            ProfilScreen(navController)
        }

        // ✅ Page 1 : Création commande
        composable("creerCommande/{typeClient}") { backStackEntry ->
            val typeClient = backStackEntry.arguments?.getString("typeClient") ?: "PARTICULIER"

            // ✅ on le sort dans un remember pour ne pas perdre la valeur
            val commandeExistante = remember(backStackEntry) {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<CommandeDTO>("commandeExistante")
            }

            // ❌ NE PAS faire .remove ici

            CreerCommandeScreen(
                typeClient = typeClient,
                navController = navController,
                commandeInitiale = commandeExistante,
                commandeViewModel = commandeViewModel
            )
        }



        // ✅ Page 2 : Sélection des produits (avec ViewModel partagé)
        composable("selectionProduits") {
            val commande = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<CommandeDTO>("commande")

            if (commande != null) {
                SelectionProduitsScreen(
                    commandeDTO = commande,
                    navController = navController,
                    commandeViewModel = commandeViewModel // ✅ ViewModel partagé
                )
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
        composable("avancesCommande/{commandeId}") { backStackEntry ->
            val commandeId = backStackEntry.arguments?.getString("commandeId")?.toLongOrNull()
            if (commandeId != null) {
                AvancesCommandeScreen(navController = navController, commandeId = commandeId)
            }
        }
        composable("corbeille") {
            CorbeilleScreen(navController)
        }
        composable("changerMotDePasse") {
            ChangerMotDePasseScreen(navController)
        }




    }
}

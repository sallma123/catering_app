package com.example.cateringcompose.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationBarItems(val route: String, val label: String, val icon: ImageVector) {
    object Commandes : NavigationBarItems("commandes", "Commandes", Icons.Default.List)
    object Paiement : NavigationBarItems("paiement", "Paiement", Icons.Default.CreditCard)
    object Calendrier : NavigationBarItems("calendrier", "Calendrier", Icons.Default.DateRange)
    object Profil : NavigationBarItems("profil", "Profil", Icons.Default.Person)

    companion object {
        val items = listOf(Commandes, Paiement, Calendrier, Profil)
    }
}

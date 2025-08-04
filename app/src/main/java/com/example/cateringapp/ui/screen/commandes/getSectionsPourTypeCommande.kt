package com.example.cateringapp.ui.screen.commandes

import androidx.compose.runtime.toMutableStateList
import com.example.cateringapp.data.dto.ProduitCommande
import com.example.cateringapp.data.dto.SectionProduit

fun getSectionsPourTypeCommande(typeCommande: String): List<SectionProduit> {
    // Pour l'instant, on retourne toujours les sections de Mariage, pour tous les types
    val reception = mutableListOf(
        ProduitCommande("Dattes et lait", "Réception", 0.0),
        ProduitCommande("Amuses bouche", "Réception", 0.0),
        ProduitCommande("Bébé macaron et Mini cookies", "Réception", 0.0),
        ProduitCommande("Petits fours salés (2 types)", "Réception", 0.0),
        ProduitCommande("Amandes salés", "Réception", 0.0),
        ProduitCommande("Choux caramélisés", "Réception", 0.0),
        ProduitCommande("Sushis", "Réception", 0.0)
    )
    val cocktail = mutableListOf(
        ProduitCommande("Jus parfums nature (fraise, citron gingembre, avocat à orange)", "Cocktail d’accueil servi à table", 0.0),
        ProduitCommande("Gâteaux soirée", "Cocktail d’accueil servi à table", 0.0),
        ProduitCommande("Gâteaux prestige", "Cocktail d’accueil servi à table", 0.0)
    )
    val entremets = mutableListOf(
        ProduitCommande("Nems viande hachée", "Entremets", 0.0),
        ProduitCommande("Verrines crevette", "Entremets", 0.0),
        ProduitCommande("Briouates poulet épinard", "Entremets", 0.0),
        ProduitCommande("Coquillages poisson blanc", "Entremets", 0.0),
        ProduitCommande("Coquillages aigre doux", "Entremets", 0.0),
        ProduitCommande("Cake salé", "Entremets", 0.0)
    )
    val festival = mutableListOf(
        ProduitCommande("Thé", "Festival de gâteaux beldi", 0.0),
        ProduitCommande("4 Gâteaux amandes : k3ab, Kehk, ghraiba aux noix, la lune à l’orange", "Festival de gâteaux beldi", 0.0)
    )
    val diner = mutableListOf(
        ProduitCommande("Poulet dermira", "Diner", 0.0),
        ProduitCommande("Pastilla Poisson royale", "Diner", 0.0),
        ProduitCommande("Mechwi d'agneau avec garniture", "Diner", 0.0),
        ProduitCommande("Chwa avec garniture", "Diner", 0.0),
        ProduitCommande("Tajine de veau contemporain", "Diner", 0.0)
    )
    val dessert = mutableListOf(
        ProduitCommande("Corbeille de fruits", "Côté dessert", 0.0),
        ProduitCommande("Gâteau glacé", "Côté dessert", 0.0)
    )
    val apresdiner = mutableListOf(
        ProduitCommande("Café", "Après Diner", 0.0),
        ProduitCommande("Gâteaux au miel : briwat, mhencha", "Après Diner", 0.0)
    )
    val supplement = mutableListOf<ProduitCommande>()

    return listOf(
        SectionProduit("Réception", reception.toMutableStateList()),
        SectionProduit("Cocktail d’accueil servi à table", cocktail.toMutableStateList()),
        SectionProduit("Entremets", entremets.toMutableStateList()),
        SectionProduit("Festival de gâteaux beldi", festival.toMutableStateList()),
        SectionProduit("Diner", diner.toMutableStateList()),
        SectionProduit("Côté dessert", dessert.toMutableStateList()),
        SectionProduit("Après Diner", apresdiner.toMutableStateList()),
        SectionProduit("Supplément", supplement.toMutableStateList())
    )
}

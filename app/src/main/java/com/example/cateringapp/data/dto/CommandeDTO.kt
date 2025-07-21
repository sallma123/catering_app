package com.example.cateringapp.data.dto

data class CommandeDTO(
    val nomClient: String,
    val salle: String,
    val nombreTables: Int,
    val prixParTable: Double,
    val typeClient: String,       // PARTICULIER, ENTREPRISE, PARTENAIRE
    val typeCommande: String,     // MARIAGE, BUFFET, etc.
    val statut: String,           // PAYEE, NON_PAYEE, etc.
    val date: String,             // format ISO (ex : 2025-08-27)
    val produits: List<ProduitCommande> = emptyList()  // facultatif pour l'instant
)

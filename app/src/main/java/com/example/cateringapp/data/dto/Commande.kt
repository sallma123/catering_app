package com.example.cateringapp.data.dto
data class Commande(
    val id: Long?,
    val numeroCommande: String,
    val typeClient: String,
    val typeCommande: String,
    val statut: String,
    val nomClient: String,
    val salle: String,
    val nombreTables: Int,
    val prixParTable: Double,
    val total: Double,
    val date: String,
    val produits: List<ProduitCommande> = emptyList()  )

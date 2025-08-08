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
    val corbeille: Boolean = false,
    val dateSuppression: String? = null,
    val date: String,
    var objet: String? = null,
    val commentaire: String? = null,
    val produits: List<ProduitCommande> = emptyList(),
    var avances: List<Avance> = emptyList())
{
    val resteAPayer: Double
        get() = total - (avances?.sumOf { it.montant } ?: 0.0)

}

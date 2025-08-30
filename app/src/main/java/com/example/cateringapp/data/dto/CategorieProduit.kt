package com.example.cateringapp.data.dto

import java.io.Serializable

data class CategorieProduit(
    val id: Long? = null,
    val nom: String,
    var ordreAffichage: Int,
    val typeCommande: String, // ex: "MARIAGE"
    val produits: List<ProduitDefini> = emptyList()
) : Serializable

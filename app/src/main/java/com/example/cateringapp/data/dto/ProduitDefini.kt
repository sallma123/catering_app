package com.example.cateringapp.data.dto

import java.io.Serializable

data class ProduitDefini(
    val id: Long? = null,
    val nom: String,
    var ordreAffichage: Int,
    val categorieId: Long
) : Serializable
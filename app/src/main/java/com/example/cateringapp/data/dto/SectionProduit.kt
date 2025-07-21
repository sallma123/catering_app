package com.example.cateringapp.data.dto

import java.io.Serializable

data class SectionProduit(
    var titre: String,
    var produits: MutableList<ProduitCommande>
) : Serializable

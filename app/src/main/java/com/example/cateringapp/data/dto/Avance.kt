package com.example.cateringapp.data.dto
import kotlinx.serialization.Serializable

@Serializable
data class Avance(
    val id: Long? = null,
    val montant: Double,
    val date: String,
    val type: String? = null // ✅ nouveau champ
)

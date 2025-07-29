package com.example.cateringapp.data.dto
import kotlinx.serialization.Serializable

@Serializable
data class Avance(
    val id: Long? = null,
    val date: String,
    val montant: Double
)

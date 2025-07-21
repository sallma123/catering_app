package com.example.cateringapp.data.dto

data class LoginResponse(
    val id: Long,
    val email: String,
    val password: String? = null
)

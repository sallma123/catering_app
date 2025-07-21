package com.example.cateringapp.data.remote

import com.example.cateringapp.data.dto.Commande
import com.example.cateringapp.data.dto.CommandeDTO
import com.example.cateringapp.data.dto.LoginRequest
import com.example.cateringapp.data.dto.LoginResponse
import com.example.cateringapp.data.dto.RegisterRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>
    @POST("api/auth/register")
    fun registerUser(@Body request: RegisterRequest): Call<Void>
    @GET("api/commandes")
    suspend fun getCommandes(): List<Commande>
    @POST("api/commandes")
    suspend fun creerCommande(@Body commandeDTO: CommandeDTO): Response<Commande>



}


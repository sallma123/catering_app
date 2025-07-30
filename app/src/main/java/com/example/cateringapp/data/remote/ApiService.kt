package com.example.cateringapp.data.remote

import CommandeDTO
import com.example.cateringapp.data.dto.Avance
import com.example.cateringapp.data.dto.Commande
import com.example.cateringapp.data.dto.LoginRequest
import com.example.cateringapp.data.dto.LoginResponse
import com.example.cateringapp.data.dto.RegisterRequest
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    fun registerUser(@Body request: RegisterRequest): Call<Void>

    @GET("api/commandes")
    suspend fun getCommandes(): List<Commande>

    @POST("api/commandes")
    suspend fun creerCommande(@Body commandeDTO: CommandeDTO): Response<Commande>

    // ✅ Télécharger le PDF
    @GET("api/commandes/{id}/fiche")
    fun telechargerFiche(@Path("id") id: Long): Call<ResponseBody>

    // ✅ Upload combiné : entête + pied de page
    @Multipart
    @POST("api/profile/uploadHeaderFooter")
    suspend fun uploadHeaderAndFooter(
        @Part header: MultipartBody.Part,
        @Part footer: MultipartBody.Part
    ): Response<Void>

    // Upload uniquement l'entête
    @Multipart
    @POST("api/profile/uploadHeader")
    suspend fun uploadHeader(
        @Part file: MultipartBody.Part
    ): Response<Void>

    // Upload uniquement le pied de page
    @Multipart
    @POST("api/profile/uploadFooter")
    suspend fun uploadFooter(
        @Part file: MultipartBody.Part
    ): Response<Void>
    // Modifier une commande
    @PUT("api/commandes/{id}")
    suspend fun modifierCommande(
        @Path("id") id: Long,
        @Body commandeDTO: CommandeDTO
    ): Response<Commande>
    @POST("api/commandes/{id}/avances")
    suspend fun ajouterAvance(
        @Path("id") idCommande: Long,
        @Body avance: Avance
    ): Response<Void>

    @GET("api/commandes/{id}/avances")
    suspend fun getAvancesByCommande(
        @Path("id") idCommande: Long
    ): List<Avance>
    @GET("api/commandes/verifier-date")
    suspend fun verifierDate(@Query("date") date: String): Boolean



}

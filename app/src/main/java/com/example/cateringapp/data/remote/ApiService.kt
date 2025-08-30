package com.example.cateringapp.data.remote

import CommandeDTO
import com.example.cateringapp.data.dto.Avance
import com.example.cateringapp.data.dto.CategorieProduit
import com.example.cateringapp.data.dto.Commande
import com.example.cateringapp.data.dto.LoginRequest
import com.example.cateringapp.data.dto.LoginResponse
import com.example.cateringapp.data.dto.ProduitDefini
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

    @PUT("api/commandes/{id}/corbeille")
    suspend fun deplacerVersCorbeille(@Path("id") id: Long): Response<Unit>

    @GET("api/commandes/corbeille")
    suspend fun getCommandesDansCorbeille(): List<Commande>

    @PUT("api/commandes/{id}/restaurer")
    suspend fun restaurerCommande(@Path("id") id: Long): Response<Unit>

    @DELETE("api/commandes/{id}")
    suspend fun supprimerCommandeDefinitivement(@Path("id") id: Long): Response<Unit>

    @POST("api/auth/change-password")
    suspend fun changerMotDePasse(
        @Query("email") email: String,
        @Query("oldPassword") oldPassword: String,
        @Query("newPassword") newPassword: String
    ): Response<Void>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Query("email") email: String): Response<Void>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(
        @Query("email") email: String,
        @Query("token") token: String,
        @Query("newPassword") newPassword: String
    ): Response<Void>

    @DELETE("api/commandes/{commandeId}/avances/{avanceId}")
    suspend fun supprimerAvance(
        @Path("commandeId") commandeId: Long,
        @Path("avanceId") avanceId: Long
    ): Response<Void>

    // 🔹 Charger le catalogue par type de commande
    @GET("api/catalogue/{typeCommande}")
    suspend fun getCatalogue(@Path("typeCommande") typeCommande: String): List<CategorieProduit>

    // 🔹 Catégories
    @POST("api/catalogue")
    suspend fun creerCategorie(@Body categorie: CategorieProduit): CategorieProduit

    @PUT("api/catalogue/{id}")
    suspend fun modifierCategorie(@Path("id") id: Long, @Body categorie: CategorieProduit): CategorieProduit

    @DELETE("api/catalogue/{id}")
    suspend fun supprimerCategorie(@Path("id") id: Long): Response<Void>

    // 🔹 Produits définis
    @POST("api/catalogue/{categorieId}/produits")
    suspend fun creerProduit(
        @Path("categorieId") categorieId: Long,
        @Body produit: ProduitDefini
    ): ProduitDefini

    @PUT("api/catalogue/produits/{id}")
    suspend fun modifierProduit(
        @Path("id") id: Long,
        @Body produit: ProduitDefini
    ): ProduitDefini

    @DELETE("api/catalogue/produits/{id}")
    suspend fun supprimerProduit(
        @Path("id") id: Long
    ): Response<Void>




}

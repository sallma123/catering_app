package com.example.cateringapp.data.dto
import java.io.Serializable
data class ProduitCommande(
    var id: Long? = null,
    var nom: String,
    var categorie: String,     // Exemple : "Réception", "Dîner", etc.
    var prix: Double,
    var selectionne: Boolean = false,
    var quantite: Int = 1

) : Serializable
{
    // ✅ Constructeur secondaire équivalent à celui en Java
    constructor(nom: String, categorie: String, prix: Double) : this(
        id = null,
        nom = nom,
        categorie = categorie,
        prix = prix,
        selectionne = false
    )

    override fun toString(): String {
        return if (prix > 0) "$nom x$quantite - $prix DH" else "$nom x$quantite"
    }

}

package com.example.cateringapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cateringapp.data.dto.CategorieProduit
import com.example.cateringapp.data.dto.ProduitDefini
import com.example.cateringapp.data.remote.ApiService
import com.example.cateringapp.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CatalogueViewModel(
    private val api: ApiService = RetrofitInstance.api
) : ViewModel() {

    private val _categories = MutableStateFlow<List<CategorieProduit>>(emptyList())
    val categories: StateFlow<List<CategorieProduit>> = _categories

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    val typesCommandeLabels = listOf(
        "Mariage", "Anniversaire", "Baptême", "Buffet de soutenance",
        "Repas coffret", "Séminaire", "Ftour Ramadan", "Fiançailles", "Henna"
    )

    private fun toEnum(value: String): String = when (value.trim().lowercase()) {
        "mariage" -> "MARIAGE"
        "anniversaire" -> "ANNIVERSAIRE"
        "baptême", "bapteme" -> "BAPTEME"
        "buffet de soutenance" -> "BUFFET_DE_SOUTENANCE"
        "repas coffret" -> "REPAS_COFFRET"
        "séminaire", "seminaire" -> "SEMINAIRE"
        "ftour ramadan" -> "FTOUR_RAMADAN"
        "fiançailles", "fiancailles" -> "FIANCAILLES"
        "henna" -> "HENNA"
        else -> value.uppercase().replace(" ", "_")
    }

    fun fetchCatalogue(typeCommande: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val enum = if (typeCommande.contains("_")) typeCommande else toEnum(typeCommande)
                val result = api.getCatalogue(enum)
                _categories.value = result.sortedBy { it.ordreAffichage }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur de chargement du catalogue"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() { _error.value = null }

    /** Ajuste et persiste les ordres des catégories pour éviter les doublons */
    private suspend fun ajusterEtPersisterOrdresCategorie(nouvelleCategorie: CategorieProduit): List<CategorieProduit> {
        val list = _categories.value.toMutableList()
        val newOrder = nouvelleCategorie.ordreAffichage
        val oldOrder = list.find { it.id == nouvelleCategorie.id }?.ordreAffichage

        list.forEach { cat ->
            if (cat.id != nouvelleCategorie.id) {
                if (oldOrder == null && cat.ordreAffichage >= newOrder) cat.ordreAffichage += 1
                else if (oldOrder != null) {
                    if (newOrder > oldOrder && cat.ordreAffichage in (oldOrder + 1)..newOrder) cat.ordreAffichage -= 1
                    else if (newOrder < oldOrder && cat.ordreAffichage in newOrder until oldOrder) cat.ordreAffichage += 1
                }
            }
        }

        val updatedList = (list.filter { it.id != nouvelleCategorie.id } + nouvelleCategorie).sortedBy { it.ordreAffichage }

        // Persister tous les ordres côté backend
        updatedList.forEach { cat ->
            if (cat.id != null) {
                try { api.modifierCategorie(cat.id, cat) } catch (_: Exception) { }
            }
        }
        return updatedList
    }

    fun ajouterCategorie(typeCommande: String, nom: String, ordre: Int, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val enum = toEnum(typeCommande)
                val nouvelleCategorie = CategorieProduit(
                    id = null,
                    nom = nom,
                    ordreAffichage = ordre,
                    typeCommande = enum,
                    produits = emptyList()
                )
                val saved = api.creerCategorie(nouvelleCategorie)
                _categories.value = ajusterEtPersisterOrdresCategorie(saved)
                callback(true)
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de l’ajout de la catégorie"
                callback(false)
            }
        }
    }

    fun modifierCategorie(categorie: CategorieProduit) {
        if (categorie.id == null) return
        viewModelScope.launch {
            try {
                val updated = api.modifierCategorie(categorie.id, categorie)
                _categories.value = ajusterEtPersisterOrdresCategorie(updated)
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de la modification de la catégorie"
            }
        }
    }

    fun supprimerCategorie(id: Long) {
        viewModelScope.launch {
            try {
                api.supprimerCategorie(id)
                _categories.value = _categories.value.filter { it.id != id }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de la suppression de la catégorie"
            }
        }
    }

    /** Ajuste et persiste les ordres des produits pour éviter les doublons */
    private suspend fun ajusterEtPersisterOrdresProduit(categorie: CategorieProduit, nouveauProduit: ProduitDefini): CategorieProduit {
        val list = categorie.produits.toMutableList()
        val newOrder = nouveauProduit.ordreAffichage
        val oldOrder = list.find { it.id == nouveauProduit.id }?.ordreAffichage

        list.forEach { p ->
            if (p.id != nouveauProduit.id) {
                if (oldOrder == null && p.ordreAffichage >= newOrder) p.ordreAffichage += 1
                else if (oldOrder != null) {
                    if (newOrder > oldOrder && p.ordreAffichage in (oldOrder + 1)..newOrder) p.ordreAffichage -= 1
                    else if (newOrder < oldOrder && p.ordreAffichage in newOrder until oldOrder) p.ordreAffichage += 1
                }
            }
        }

        val updatedCat = categorie.copy(
            produits = (list.filter { it.id != nouveauProduit.id } + nouveauProduit).sortedBy { it.ordreAffichage }
        )

        // Persister tous les produits
        updatedCat.produits.forEach { p ->
            if (p.id != null) {
                try { api.modifierProduit(p.id, p) } catch (_: Exception) { }
            }
        }

        return updatedCat
    }

    fun ajouterProduit(categorieId: Long, nom: String = "Nouveau produit", ordre: Int? = null) {
        val categorie = _categories.value.find { it.id == categorieId } ?: return
        viewModelScope.launch {
            try {
                val nextOrder = ordre ?: (categorie.produits.maxOfOrNull { it.ordreAffichage } ?: 0) + 1
                val nouveauProduit = ProduitDefini(
                    id = null,
                    nom = nom,
                    ordreAffichage = nextOrder,
                    categorieId = categorieId
                )
                val saved = api.creerProduit(categorieId, nouveauProduit)
                _categories.value = _categories.value.map {
                    if (it.id == categorieId) ajusterEtPersisterOrdresProduit(it, saved) else it
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de l’ajout du produit"
            }
        }
    }

    fun modifierProduit(produit: ProduitDefini) {
        val produitId = produit.id ?: return
        viewModelScope.launch {
            try {
                val updated = api.modifierProduit(produitId, produit)
                _categories.value = _categories.value.map { cat ->
                    if (cat.id == updated.categorieId) ajusterEtPersisterOrdresProduit(cat, updated) else cat
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de la modification du produit"
            }
        }
    }

    fun supprimerProduit(id: Long) {
        viewModelScope.launch {
            try {
                api.supprimerProduit(id)
                _categories.value = _categories.value.map { cat ->
                    cat.copy(produits = cat.produits.filter { p -> p.id != id })
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de la suppression du produit"
            }
        }
    }
}

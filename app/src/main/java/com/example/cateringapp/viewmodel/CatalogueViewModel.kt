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

    // Labels utilisés dans l’app (tu peux les réutiliser dans l’UI)
    val typesCommandeLabels = listOf(
        "Mariage", "Anniversaire", "Baptême", "Buffet de soutenance",
        "Repas coffret", "Séminaire", "Ftour Ramadan", "Fiançailles", "Henna"
    )

    /** Map label UI → enum backend */
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

    /** Charger catégories + produits pour un type de commande */
    fun fetchCatalogue(typeCommande: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val enum = if (typeCommande.contains("_")) typeCommande else toEnum(typeCommande)
                val result = api.getCatalogue(enum)
                _categories.value = result.sortedBy { c -> c.ordreAffichage }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur de chargement du catalogue"
            } finally {
                _loading.value = false
            }
        }
    }

    /** Ajouter une catégorie */
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
                _categories.value = (_categories.value + saved).sortedBy { it.ordreAffichage }
                callback(true)
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de l’ajout de la catégorie"
                callback(false)
            }
        }
    }

    fun clearError() { _error.value = null }


    /** Modifier une catégorie */
    fun modifierCategorie(categorie: CategorieProduit) {
        if (categorie.id == null) return
        viewModelScope.launch {
            try {
                val updated = api.modifierCategorie(categorie.id, categorie)
                _categories.value = _categories.value
                    .map { c -> if (c.id == updated.id) updated else c }
                    .sortedBy { c -> c.ordreAffichage }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de la modification de la catégorie"
            }
        }
    }

    /** Supprimer une catégorie */
    fun supprimerCategorie(id: Long) {
        viewModelScope.launch {
            try {
                api.supprimerCategorie(id)
                _categories.value = _categories.value.filter { c -> c.id != id }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de la suppression de la catégorie"
            }
        }
    }

    /** Ajouter un produit dans une catégorie */
    fun ajouterProduit(categorieId: Long, nom: String = "Nouveau produit", ordre: Int? = null) {
        val categorie = _categories.value.find { c -> c.id == categorieId } ?: return
        viewModelScope.launch {
            try {
                val nextOrder = ordre ?: (categorie.produits.maxOfOrNull { p -> p.ordreAffichage } ?: 0) + 1
                val nouveauProduit = ProduitDefini(
                    id = null,
                    nom = nom,
                    ordreAffichage = nextOrder,
                    categorieId = categorieId
                )
                val saved = api.creerProduit(categorieId, nouveauProduit)
                val updatedCategorie = categorie.copy(
                    produits = (categorie.produits + saved).sortedBy { p -> p.ordreAffichage }
                )
                _categories.value = _categories.value.map { c ->
                    if (c.id == categorieId) updatedCategorie else c
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de l’ajout du produit"
            }
        }
    }


    /** Modifier un produit */
    fun modifierProduit(produit: ProduitDefini) {
        val produitId = produit.id ?: return
        viewModelScope.launch {
            try {
                val updated = api.modifierProduit(produitId, produit)
                _categories.value = _categories.value.map { cat ->
                    if (cat.id == updated.categorieId) {
                        cat.copy(
                            produits = cat.produits
                                .map { p -> if (p.id == updated.id) updated else p }
                                .sortedBy { p -> p.ordreAffichage }
                        )
                    } else cat
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erreur lors de la modification du produit"
            }
        }
    }

    /** Supprimer un produit */
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

    /** Permuter l’ordre de 2 catégories + persister l’ordre */
    fun permuterOrdreCategorie(sourceId: Long, targetId: Long) {
        val list = _categories.value.toMutableList()
        val aIdx = list.indexOfFirst { c -> c.id == sourceId }
        val bIdx = list.indexOfFirst { c -> c.id == targetId }
        if (aIdx == -1 || bIdx == -1) return

        val a = list[aIdx]
        val b = list[bIdx]
        val updatedA = a.copy(ordreAffichage = b.ordreAffichage)
        val updatedB = b.copy(ordreAffichage = a.ordreAffichage)
        list[aIdx] = updatedA
        list[bIdx] = updatedB
        _categories.value = list.sortedBy { c -> c.ordreAffichage }

        // Persist côté backend (best effort)
        viewModelScope.launch {
            try {
                if (updatedA.id != null) api.modifierCategorie(updatedA.id, updatedA)
                if (updatedB.id != null) api.modifierCategorie(updatedB.id, updatedB)
            } catch (_: Exception) { /* on ne casse pas l’UI si ça échoue */ }
        }
    }

    /** Permuter l’ordre de 2 produits d’une même catégorie + persister l’ordre */
    fun permuterOrdreProduit(categorieId: Long, sourceProduitId: Long, targetProduitId: Long) {
        val cat = _categories.value.find { c -> c.id == categorieId } ?: return
        val produits = cat.produits.toMutableList()
        val aIdx = produits.indexOfFirst { p -> p.id == sourceProduitId }
        val bIdx = produits.indexOfFirst { p -> p.id == targetProduitId }
        if (aIdx == -1 || bIdx == -1) return

        val a = produits[aIdx]
        val b = produits[bIdx]
        val updatedA = a.copy(ordreAffichage = b.ordreAffichage)
        val updatedB = b.copy(ordreAffichage = a.ordreAffichage)
        produits[aIdx] = updatedA
        produits[bIdx] = updatedB

        val updatedCat = cat.copy(produits = produits.sortedBy { p -> p.ordreAffichage })
        _categories.value = _categories.value.map { c -> if (c.id == categorieId) updatedCat else c }

        // Persist côté backend (best effort)
        viewModelScope.launch {
            try {
                if (updatedA.id != null) api.modifierProduit(updatedA.id, updatedA)
                if (updatedB.id != null) api.modifierProduit(updatedB.id, updatedB)
            } catch (_: Exception) { /* on ignore côté UI */ }
        }
    }
}

package com.example.cateringapp.viewmodel

import CommandeDTO
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cateringapp.data.dto.Avance
import com.example.cateringapp.data.dto.Commande
import com.example.cateringapp.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import java.text.SimpleDateFormat
import java.util.*

class CommandeViewModel : ViewModel() {

    private val _commandes = MutableStateFlow<List<Commande>>(emptyList())
    val commandes: StateFlow<List<Commande>> = _commandes.asStateFlow()

    private val _commandesParDate = MutableStateFlow<Map<String, List<Commande>>>(emptyMap())
    val commandesParDate: StateFlow<Map<String, List<Commande>>> = _commandesParDate.asStateFlow()

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        fetchCommandes()
    }

    fun fetchCommandes() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getCommandes()
                _commandes.value = response
                regrouperParDate(response)
            } catch (e: Exception) {
                _commandes.value = emptyList()
                _commandesParDate.value = emptyMap()
                Log.e("CommandeViewModel", "Erreur fetchCommandes: ${e.message}")
            }
        }
    }

    private fun regrouperParDate(commandes: List<Commande>) {
        val map = commandes.groupBy { it.date }
        _commandesParDate.value = map
    }

    fun getCommandesPourDate(date: Date): List<Commande> {
        val key = dateFormatter.format(date)
        return _commandesParDate.value[key] ?: emptyList()
    }

    fun dateAvecCommandes(): Set<String> {
        return _commandesParDate.value.keys
    }

    fun supprimerCommande(id: Long) {
        viewModelScope.launch {
            // TODO : si tu ajoutes suppression plus tard
        }
    }

    fun modifierCommande(
        id: Long,
        commandeDTO: CommandeDTO,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.modifierCommande(id, commandeDTO)
                fetchCommandes()
                onSuccess()
            } catch (e: Exception) {
                onError()
            }
        }
    }

    // ✅ Ajouter une avance à une commande
    fun ajouterAvance(idCommande: Long, avance: Avance) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.ajouterAvance(idCommande, avance)
                fetchCommandes()
            } catch (e: Exception) {
                Log.e("CommandeViewModel", "Erreur ajout avance: ${e.message}")
            }
        }
    }

    // ✅ Récupérer les avances d'une commande
    fun getAvancesForCommande(idCommande: Long): Flow<List<Avance>> = flow {
        try {
            val result = RetrofitInstance.api.getAvancesByCommande(idCommande)
            emit(result)
        } catch (e: Exception) {
            Log.e("CommandeViewModel", "Erreur getAvances: ${e.message}")
            emit(emptyList())
        }
    }
    fun getCommandeById(id: Long): Commande? {
        return _commandes.value.find { it.id == id }
    }

}

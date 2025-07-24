package com.example.cateringapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cateringapp.data.dto.Commande
import com.example.cateringapp.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private fun fetchCommandes() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getCommandes()
                _commandes.value = response
                regrouperParDate(response)
            } catch (e: Exception) {
                _commandes.value = emptyList()
                _commandesParDate.value = emptyMap()
            }
        }
    }

    private fun regrouperParDate(commandes: List<Commande>) {
        val map = commandes.groupBy { it.date } // ici le format est déjà "yyyy-MM-dd"
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

        }
    }

}

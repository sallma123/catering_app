package com.example.cateringapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cateringapp.data.dto.Commande
import com.example.cateringapp.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommandeViewModel : ViewModel() {

    private val _commandes = MutableStateFlow<List<Commande>>(emptyList())
    val commandes: StateFlow<List<Commande>> = _commandes

    init {
        fetchCommandes()
    }

    private fun fetchCommandes() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getCommandes()
                _commandes.value = response
            } catch (e: Exception) {
                _commandes.value = emptyList()
            }
        }
    }
}

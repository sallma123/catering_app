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
    private val _avances = MutableStateFlow<Map<Long, List<Avance>>>(emptyMap())
    val avances = _avances.asStateFlow()

    private val _commandes = MutableStateFlow<List<Commande>>(emptyList())
    val commandes: StateFlow<List<Commande>> = _commandes.asStateFlow()

    private val _commandesParDate = MutableStateFlow<Map<String, List<Commande>>>(emptyMap())

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        fetchCommandes()
    }
    fun fetchCommandes() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getCommandes()
                Log.d("fetchCommandes", "Reçu ${response.size} commandes")

                val commandesActives = response.filter { !it.corbeille }

                Log.d("fetchCommandes", "Commandes actives : ${commandesActives.size}")

                _commandes.value = commandesActives
                regrouperParDate(commandesActives)
                chargerToutesLesAvances(commandesActives)

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

    fun modifierCommande(
        id: Long,
        commandeDTO: CommandeDTO,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.modifierCommande(id, commandeDTO)
                if (response.isSuccessful) {
                    fetchCommandes()
                    onSuccess()
                } else {
                    Log.e("CommandeViewModel", "❌ Erreur HTTP : ${response.code()} - ${response.message()}")
                    onError()
                }
            } catch (e: Exception) {
                Log.e("CommandeViewModel", "❌ Exception : ${e.message}")
                onError()
            }
        }
    }


    // ✅ Ajouter une avance à une commande
    fun ajouterAvance(idCommande: Long, avance: Avance) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.ajouterAvance(idCommande, avance)
                if (response.isSuccessful) {
                    Log.d("Avance", "✅ Avance bien ajoutée")

                    // 🔁 Petite attente (assure que l'avance est bien persistée)
                    kotlinx.coroutines.delay(300)

                    // 🔁 Recharge les avances depuis l'API
                    chargerAvancesPourCommande(idCommande)

                    // 🔁 Recharge les commandes (pour mise à jour du reste si modifié)
                    fetchCommandes()
                } else {
                    Log.e("Avance", "❌ Échec de l'ajout : ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("Avance", "❌ Exception ajout avance: ${e.message}")
            }
        }
    }




    // ✅ Récupérer les avances d'une commande
    fun getAvancesForCommande(idCommande: Long): Flow<List<Avance>> {
        return avances.map { it[idCommande] ?: emptyList() }
    }

    fun creerCommande(dto: CommandeDTO, onSuccess: (Long) -> Unit, onError: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.creerCommande(dto)
                if (response.isSuccessful) {
                    val idCree = response.body()?.id
                    if (idCree != null) {
                        fetchCommandes()
                        onSuccess(idCree)
                    } else {
                        onError()
                    }
                } else {
                    onError()
                }
            } catch (e: Exception) {
                Log.e("CommandeViewModel", "Erreur création commande : ${e.message}")
                onError()
            }
        }
    }
    fun verifierDateCommande(
        dateIso: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val existe = RetrofitInstance.api.verifierDate(dateIso)
                onResult(existe)
            } catch (e: Exception) {
                Log.e("CommandeViewModel", "Erreur vérification date : ${e.message}")
                onResult(false)
            }
        }
    }
    fun chargerAvancesPourCommande(idCommande: Long) {
        viewModelScope.launch {
            try {
                val result = RetrofitInstance.api.getAvancesByCommande(idCommande)
                val nouvelleMap = _avances.value.toMutableMap()
                nouvelleMap[idCommande] = result
                _avances.value = nouvelleMap
            } catch (e: Exception) {
                Log.e("CommandeViewModel", "Erreur chargement avances init : ${e.message}")
            }
        }
    }
    fun chargerToutesLesAvances(commandes: List<Commande>) {
        viewModelScope.launch {
            try {
                val nouvellesAvances = mutableMapOf<Long, List<Avance>>()
                commandes.forEach { commande ->
                    val id = commande.id ?: return@forEach
                    val result = RetrofitInstance.api.getAvancesByCommande(id)
                    nouvellesAvances[id] = result
                }
                _avances.value = nouvellesAvances
            } catch (e: Exception) {
                Log.e("CommandeViewModel", "Erreur chargement avances global : ${e.message}")
            }
        }
    }
    fun supprimerCommandeSoft(id: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.deplacerVersCorbeille(id)
                if (response.isSuccessful) {
                    fetchCommandes()
                    onSuccess() // ✅ callback ici
                } else {
                    Log.e("CommandeViewModel", "Erreur suppression soft: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("CommandeViewModel", "Exception suppression soft: ${e.message}")
            }
        }
    }

    val commandesSupprimees: StateFlow<List<Commande>>
        get() = commandes.map { list -> list.filter { it.corbeille } }.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
    private val _corbeille = MutableStateFlow<List<Commande>>(emptyList())
    val corbeille: StateFlow<List<Commande>> = _corbeille
    fun fetchCorbeille() {
        viewModelScope.launch {
            try {
                val result = RetrofitInstance.api.getCommandesDansCorbeille()
                _corbeille.value = result
            } catch (e: Exception) {
                Log.e("CommandeViewModel", "Erreur fetchCorbeille : ${e.message}")
            }
        }
    }

    fun restaurerCommande(id: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.restaurerCommande(id)
                if (response.isSuccessful) {
                    fetchCorbeille()
                    fetchCommandes()
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("CommandeViewModel", "Erreur restauration : ${e.message}")
            }
        }
    }

    fun supprimerDefinitivement(id: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.supprimerCommandeDefinitivement(id)
                if (response.isSuccessful) {
                    fetchCorbeille()
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("CommandeViewModel", "Erreur suppression définitive : ${e.message}")
            }
        }
    }
    fun supprimerAvance(idCommande: Long, idAvance: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.supprimerAvance(idCommande, idAvance)
                if (response.isSuccessful) {
                    Log.d("Avance", "✅ Avance supprimée")

                    // 🔄 Recharge la liste des avances pour cette commande
                    chargerAvancesPourCommande(idCommande)

                    // 🔄 Recharge les commandes (reste à payer mis à jour)
                    fetchCommandes()
                } else {
                    Log.e("Avance", "❌ Erreur suppression avance : ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("Avance", "❌ Exception suppression avance: ${e.message}")
            }
        }
    }



}

package com.example.cateringapp.ui.screen.commandes

import CommandeDTO
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cateringapp.data.dto.ProduitCommande
import com.example.cateringapp.data.dto.SectionProduit
import com.example.cateringapp.data.remote.ApiService
import com.example.cateringapp.data.remote.RetrofitInstance
import com.example.cateringapp.viewmodel.CommandeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SelectionProduitsScreen(
    commandeDTO: CommandeDTO,
    navController: NavController,
    commandeViewModel: CommandeViewModel,
    apiService: ApiService = RetrofitInstance.api
) {
    val context = LocalContext.current
    var prixParTable by remember { mutableStateOf("") }
    var total by remember { mutableStateOf(0.0) }

    // ✅ Nouvelle fonction pour initialiser avec produits sélectionnés existants
    fun initialiserSectionsAvecProduits(): List<SectionProduit> {
        val reception = mutableListOf(
            ProduitCommande("Dattes et lait", "Réception", 0.0),
            ProduitCommande("Amuses bouche", "Réception", 0.0),
            ProduitCommande("Petits fours salés", "Réception", 0.0)
        )
        val dessert = mutableListOf(
            ProduitCommande("Gâteaux prestige", "Dessert", 0.0)
        )
        val supplement = mutableListOf<ProduitCommande>()

        commandeDTO.produits.forEach { produit ->
            val existing = when (produit.categorie) {
                "Réception" -> reception
                "Dessert" -> dessert
                "Supplément" -> supplement
                else -> supplement
            }
            val index = existing.indexOfFirst { it.nom == produit.nom }
            if (index >= 0) {
                existing[index] = existing[index].copy(
                    prix = produit.prix,
                    selectionne = true
                )
            } else {
                existing.add(produit.copy(selectionne = true))
            }
        }

        return listOf(
            SectionProduit("Réception", reception.toMutableStateList()),
            SectionProduit("Dessert", dessert.toMutableStateList()),
            SectionProduit("Supplément", supplement.toMutableStateList())
        )
    }

    val sections = remember { initialiserSectionsAvecProduits().toMutableStateList() }


    fun recalculerTotal() {
        val base = prixParTable.toDoubleOrNull() ?: 0.0
        val totalSuppl = sections.flatMap { it.produits }.filter { it.selectionne }.sumOf { it.prix }
        total = commandeDTO.nombreTables * base + totalSuppl
    }
    LaunchedEffect(Unit) {
        prixParTable = commandeDTO.prixParTable.takeIf { it > 0 }?.toString() ?: ""
        recalculerTotal()
    }

    Scaffold(
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Produits pour ${commandeDTO.typeCommande}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            sections.forEach { section ->
                var newNom by remember { mutableStateOf("") }
                var newPrix by remember { mutableStateOf("") }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = section.titre,
                        color = Color(0xFFFFC107),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Checkbox(
                        checked = section.produits.all { it.selectionne },
                        onCheckedChange = { checked ->
                            section.produits.forEachIndexed { index, produit ->
                                section.produits[index] = produit.copy(selectionne = checked)
                            }
                            recalculerTotal()
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFFFC107),
                            uncheckedColor = Color.White
                        )
                    )
                }

                section.produits.forEachIndexed { index, produit ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = produit.selectionne,
                            onCheckedChange = {
                                section.produits[index] = produit.copy(selectionne = it)
                                recalculerTotal()
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFFFC107),
                                uncheckedColor = Color.White
                            )
                        )
                        Column {
                            Text(produit.nom, color = Color.White)
                            if (produit.prix > 0) {
                                Text("${produit.prix} DH", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newNom,
                        onValueChange = { newNom = it },
                        label = { Text("Nom") },
                        modifier = Modifier.weight(1f),
                        colors = produitTextFieldColors()
                    )
                    if (section.titre == "Supplément") {
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = newPrix,
                            onValueChange = { newPrix = it },
                            label = { Text("Prix") },
                            modifier = Modifier.width(100.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = produitTextFieldColors()
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val prix = if (section.titre == "Supplément") newPrix.toDoubleOrNull() ?: 0.0 else 0.0
                            if (newNom.isNotBlank()) {
                                section.produits.add(
                                    ProduitCommande(nom = newNom, categorie = section.titre, prix = prix, selectionne = true)
                                )
                                newNom = ""
                                newPrix = ""
                                recalculerTotal()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
                    ) {
                        Text("Ajouter", color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = prixParTable,
                onValueChange = {
                    prixParTable = it
                    recalculerTotal()
                },
                label = { Text("Prix par table") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = produitTextFieldColors()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Total : %.2f DH".format(total), color = Color.White, fontSize = 18.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val prix = prixParTable.toDoubleOrNull() ?: 0.0
                    val produits = sections.flatMap { it.produits }.filter { it.selectionne }

                    if (produits.isEmpty()) {
                        Toast.makeText(context, "Sélectionnez au moins un produit", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val isModification = commandeDTO.id != null

                    val commandeFinale = commandeDTO.copy(
                        prixParTable = prix,
                        produits = produits
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = if (isModification) {
                                apiService.modifierCommande(commandeFinale.id!!, commandeFinale)
                            } else {
                                apiService.creerCommande(commandeFinale)
                            }
                            withContext(Dispatchers.Main) {
                                if (response.isSuccessful) {
                                    val id = if (isModification) commandeFinale.id!! else response.body()!!.id
                                    commandeViewModel.fetchCommandes()
                                    navController.navigate("ficheCommande/$id")
                                } else {
                                    Toast.makeText(context, "Erreur API", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Erreur réseau", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
            ) {
                Text("Valider", fontWeight = FontWeight.Bold, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun produitTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color(0xFFFFC107),
    unfocusedBorderColor = Color.Gray,
    cursorColor = Color.White,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = Color.Gray
)

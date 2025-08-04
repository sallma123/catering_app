package com.example.cateringapp.ui.screen.commandes

import CommandeDTO
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
    var prixParTable by remember { mutableStateOf(commandeDTO.prixParTable.takeIf { it > 0 }?.toString() ?: "") }
    var total by remember { mutableStateOf(0.0) }

    val sections = remember {
        val initialSections = getSectionsPourTypeCommande(commandeDTO.typeCommande)
        commandeDTO.produits.forEach { produit ->
            initialSections.find { it.titre == produit.categorie }?.let { section ->
                val index = section.produits.indexOfFirst { it.nom == produit.nom }
                if (index >= 0) {
                    section.produits[index] = section.produits[index].copy(
                        prix = produit.prix,
                        selectionne = true,
                        quantite = produit.quantite
                    )
                } else {
                    section.produits.add(produit.copy(selectionne = true))
                }
            }
        }
        initialSections.toMutableStateList()
    }

    fun recalculerTotal() {
        val base = prixParTable.toDoubleOrNull() ?: 0.0
        val totalSuppl = sections.flatMap { it.produits }
            .filter { it.selectionne }
            .sumOf { it.prix * (it.quantite ?: 1) }
        total = commandeDTO.nombreTables * base + totalSuppl
    }

    LaunchedEffect(Unit) { recalculerTotal() }

    Scaffold(containerColor = Color(0xFF121212)) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text("Produits pour ${commandeDTO.typeCommande}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))

            sections.forEach { section ->
                var newNom by remember { mutableStateOf("") }
                var newPrix by remember { mutableStateOf("") }
                var newQuantite by remember { mutableStateOf("1") }

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
                            section.produits.indices.forEach { i ->
                                section.produits[i] = section.produits[i].copy(selectionne = checked)
                            }
                            recalculerTotal()
                        },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFFC107), uncheckedColor = Color.White)
                    )
                }

                section.produits.forEachIndexed { index, produit ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = produit.selectionne,
                            onCheckedChange = {
                                section.produits[index] = produit.copy(selectionne = it)
                                recalculerTotal()
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFFC107), uncheckedColor = Color.White)
                        )
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text(produit.nom, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            if (section.titre == "Supplément") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${(produit.quantite ?: 1)} x", color = Color.LightGray, fontSize = 12.sp)
                                    Spacer(Modifier.width(4.dp))
                                    Text("${produit.prix} DH", color = Color.Gray, fontSize = 12.sp)
                                }
                            } else if (produit.prix > 0) {
                                Text("${produit.prix} DH", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newNom,
                        onValueChange = { newNom = it },
                        label = { Text("Nom") },
                        modifier = Modifier.weight(0.45f),
                        colors = produitTextFieldColors()
                    )
                    if (section.titre == "Supplément") {
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = newPrix,
                            onValueChange = { newPrix = it },
                            label = { Text("Prix") },
                            modifier = Modifier.weight(0.25f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = produitTextFieldColors()
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = newQuantite,
                            onValueChange = { newQuantite = it },
                            label = { Text("Qté") },
                            modifier = Modifier.weight(0.18f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = produitTextFieldColors()
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val prix = if (section.titre == "Supplément") newPrix.toDoubleOrNull() ?: 0.0 else 0.0
                            val quantite = if (section.titre == "Supplément") newQuantite.toIntOrNull() ?: 1 else 1
                            if (newNom.isNotBlank()) {
                                section.produits.add(
                                    ProduitCommande(nom = newNom, categorie = section.titre, prix = prix, quantite = quantite, selectionne = true)
                                )
                                newNom = ""
                                newPrix = ""
                                newQuantite = "1"
                                recalculerTotal()
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = Color(0xFFFFC107))
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
                    val commandeFinale = commandeDTO.copy(prixParTable = prix, produits = produits)

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

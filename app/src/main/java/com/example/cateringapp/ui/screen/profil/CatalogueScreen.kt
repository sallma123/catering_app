package com.example.cateringapp.ui.screen.catalogue

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.cateringapp.ui.screen.commandes.ExposedDropdownField
import com.example.cateringapp.ui.screen.commandes.mapTypeCommandeLabelToEnum
import com.example.cateringapp.viewmodel.CatalogueViewModel
import com.example.cateringapp.data.dto.CategorieProduit
import com.example.cateringapp.data.dto.ProduitDefini

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogueScreen(
    navController: NavHostController,
    catalogueViewModel: CatalogueViewModel = viewModel()
) {
    val context = LocalContext.current

    var selectedTypeCommande by remember { mutableStateOf("") }
    var nouveauNomCategorie by remember { mutableStateOf("") }
    var nouvelOrdre by remember { mutableStateOf("") }

    var categorieEnEdition by remember { mutableStateOf<CategorieProduit?>(null) }
    var produitEnEdition by remember { mutableStateOf<ProduitDefini?>(null) }
    var categoriePourAjoutProduit by remember { mutableStateOf<CategorieProduit?>(null) }

    val categories by catalogueViewModel.categories.collectAsState()
    val errorMessage by catalogueViewModel.error.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            catalogueViewModel.clearError()
        }
    }

    Scaffold(containerColor = Color(0xFF121212)) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            // Sélecteur de type de commande
            ExposedDropdownField(
                label = "Type de commande",
                options = catalogueViewModel.typesCommandeLabels,
                selected = selectedTypeCommande
            ) {
                selectedTypeCommande = it
                catalogueViewModel.fetchCatalogue(mapTypeCommandeLabelToEnum(it))
            }

            Spacer(Modifier.height(16.dp))

            // Ligne Nom + Ordre
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nouveauNomCategorie,
                    onValueChange = { nouveauNomCategorie = it },
                    label = { Text("Nom de la catégorie") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFFFC107),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = nouvelOrdre,
                    onValueChange = { nouvelOrdre = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Ordre *") },
                    modifier = Modifier.width(120.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFFFC107),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (nouveauNomCategorie.isBlank() || selectedTypeCommande.isBlank()) {
                        Toast.makeText(context, "Remplissez le nom et le type", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val ordre = nouvelOrdre.toIntOrNull()
                        ?: (categories.maxOfOrNull { it.ordreAffichage } ?: 0) + 1

                    catalogueViewModel.ajouterCategorie(selectedTypeCommande, nouveauNomCategorie, ordre) { success ->
                        if (success) {
                            Toast.makeText(context, "Catégorie ajoutée", Toast.LENGTH_SHORT).show()
                            nouveauNomCategorie = ""
                            nouvelOrdre = ""
                        } else {
                            Toast.makeText(context, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ajouter catégorie")
            }

            Spacer(Modifier.height(16.dp))

            // 🔹 Affichage des catégories avec dernier Spacer
            categories.forEachIndexed { index, cat ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${cat.ordreAffichage} - ${cat.nom}",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row {
                                IconButton(onClick = { categorieEnEdition = cat }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Modifier catégorie", tint = Color.White)
                                }
                                IconButton(onClick = { catalogueViewModel.supprimerCategorie(cat.id!!) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Supprimer catégorie", tint = Color.Red)
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Produits
                        cat.produits.sortedBy { it.ordreAffichage }.forEach { produit ->
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text("${produit.ordreAffichage} - ${produit.nom}", color = Color.White)
                                Row {
                                    IconButton(onClick = { produitEnEdition = produit }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Modifier produit", tint = Color.White)
                                    }
                                    IconButton(onClick = { catalogueViewModel.supprimerProduit(produit.id!!) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Supprimer produit", tint = Color.Red)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Button(
                            onClick = { categoriePourAjoutProduit = cat },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ajouter produit")
                        }

                        // 🔹 Espace uniquement après le dernier bouton de la dernière catégorie
                        if (index == categories.lastIndex) {
                            Spacer(modifier = Modifier.height(50.dp))
                        }
                    }
                }
            }
        }

        // 🔹 Dialog Modifier Catégorie
        categorieEnEdition?.let { cat ->
            var nom by remember { mutableStateOf(cat.nom) }
            var ordre by remember { mutableStateOf(cat.ordreAffichage.toString()) }

            AlertDialog(
                onDismissRequest = { categorieEnEdition = null },
                title = { Text("Modifier catégorie") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = nom,
                            onValueChange = { nom = it },
                            label = { Text("Nom") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = ordre,
                            onValueChange = { ordre = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Ordre") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val updatedCat = cat.copy(
                            nom = nom,
                            ordreAffichage = ordre.toIntOrNull() ?: cat.ordreAffichage
                        )
                        catalogueViewModel.modifierCategorie(updatedCat)
                        categorieEnEdition = null
                    }) { Text("Valider") }
                },
                dismissButton = { Button(onClick = { categorieEnEdition = null }) { Text("Annuler") } }
            )
        }

        // 🔹 Dialog Ajouter Produit
        categoriePourAjoutProduit?.let { cat ->
            var nom by remember { mutableStateOf("") }
            var ordre by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { categoriePourAjoutProduit = null },
                title = { Text("Ajouter produit") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = nom,
                            onValueChange = { nom = it },
                            label = { Text("Nom") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = ordre,
                            onValueChange = { ordre = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Ordre") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val ordreInt = ordre.toIntOrNull()
                            ?: (cat.produits.maxOfOrNull { it.ordreAffichage } ?: 0) + 1
                        catalogueViewModel.ajouterProduit(cat.id!!, nom, ordreInt)
                        categoriePourAjoutProduit = null
                    }) { Text("Ajouter") }
                },
                dismissButton = { Button(onClick = { categoriePourAjoutProduit = null }) { Text("Annuler") } }
            )
        }

        // 🔹 Dialog Modifier Produit
        produitEnEdition?.let { produit ->
            var nom by remember { mutableStateOf(produit.nom) }
            var ordre by remember { mutableStateOf(produit.ordreAffichage.toString()) }

            AlertDialog(
                onDismissRequest = { produitEnEdition = null },
                title = { Text("Modifier produit") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = nom,
                            onValueChange = { nom = it },
                            label = { Text("Nom") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = ordre,
                            onValueChange = { ordre = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Ordre") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val updatedProduit = produit.copy(
                            nom = nom,
                            ordreAffichage = ordre.toIntOrNull() ?: produit.ordreAffichage
                        )
                        catalogueViewModel.modifierProduit(updatedProduit)
                        produitEnEdition = null
                    }) { Text("Valider") }
                },
                dismissButton = { Button(onClick = { produitEnEdition = null }) { Text("Annuler") } }
            )
        }
    }
}

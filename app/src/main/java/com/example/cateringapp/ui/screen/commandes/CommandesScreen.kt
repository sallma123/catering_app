package com.example.cateringapp.ui.screen.commandes


import CommandeDTO
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cateringapp.data.dto.Commande
import com.example.cateringapp.data.dto.toDTO


import com.example.cateringapp.ui.components.CommandeSwipeItem
import com.example.cateringapp.ui.navigation.BottomNavBar
import com.example.cateringapp.viewmodel.CommandeViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CommandesScreen(navController: NavController, viewModel: CommandeViewModel = viewModel()) {
    val commandes by viewModel.commandes.collectAsState()
    val scrollState = rememberLazyListState()
    var query by remember { mutableStateOf("") }
    val context = LocalContext.current

    val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sdfMois = SimpleDateFormat("MMMM", Locale.FRENCH)
    val sdfSort = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val commandesTriees = commandes
        .filter {
            query.isBlank() || it.nomClient.contains(query, ignoreCase = true) || it.salle.contains(query, ignoreCase = true)
        }
        .sortedBy {
            try {
                sdfSort.parse(it.date)
            } catch (e: Exception) {
                null
            }
        }

    val grouped = commandesTriees.groupBy {
        try {
            val date = sdfInput.parse(it.date)
            sdfMois.format(date!!).replaceFirstChar { c -> c.uppercase() }
        } catch (e: Exception) {
            "Inconnue"
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController) },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .height(56.dp),
                placeholder = {
                    Text("Rechercher...", color = Color.Gray)
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color(0xFFFFC107),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.Gray
                )
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                state = scrollState,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp)
            ) {
                grouped.forEach { (mois, items) ->
                    item {
                        Text(
                            text = mois,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF121212))
                                .padding(vertical = 2.dp, horizontal = 1.dp)
                        )
                    }
                    items(items) { commande ->
                        CommandeSwipeItem(
                            commande = commande,
                            onDeleteClick = {
                                Toast.makeText(
                                    context,
                                    "Suppression bientôt disponible",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onFicheClick = { navController.navigate("ficheCommande/${commande.id}") },
                            onDuplicateClick = {
                                Toast.makeText(
                                    context,
                                    "Duplication bientôt disponible",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            content = {
                                CommandeCard(commande) {
                                    val dto = commande.toDTO()
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("commandeExistante", dto)

                                    navController.navigate("creerCommande/${commande.typeClient}") {
                                        // efface l'entrée après navigation
                                        launchSingleTop = true
                                    }

                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.remove<CommandeDTO>("commandeExistante")

                                }

                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Particulier", "Entreprise", "Partenaire").forEach { label ->
                    Button(
                        onClick = {
                            navController.navigate("creerCommande/${label.uppercase()}")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = label.uppercase(),
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            fontSize = 10.2.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommandeCard(commande: Commande, onClick: () -> Unit = {}) {
    val dateFormatted = try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(commande.date)
        SimpleDateFormat("dd/MM", Locale.getDefault()).format(date!!)
    } catch (e: Exception) {
        "??/??"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // 👈 Ajout du clic
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = commande.typeCommande.uppercase(),
                    color = Color(0xFFFFC107),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${commande.nomClient} | ${commande.salle} | ${commande.nombreTables} tables",
                    fontSize = 14.sp
                )
            }
            Text(
                text = dateFormatted,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

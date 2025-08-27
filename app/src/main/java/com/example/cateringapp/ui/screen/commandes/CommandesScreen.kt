package com.example.cateringapp.ui.screen.commandes

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
    var filtre by remember { mutableStateOf("FUTURE") } // ✅ par défaut FUTURE
    var dateDebut by remember { mutableStateOf<Date?>(null) }
    var dateFin by remember { mutableStateOf<Date?>(null) }

    val context = LocalContext.current
    val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sdfMois = SimpleDateFormat("MMMM", Locale.FRENCH)
    val sdfSort = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = remember { normalizeDate(Date()) }

    // ✅ Rafraîchissement si besoin
    val shouldRefresh = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<Boolean>("refreshCommandes") == true
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh == true) {
            viewModel.fetchCommandes()
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("refreshCommandes")
        }
    }

    // ✅ Filtrage identique à PaiementsScreen
    val commandesFiltrees = commandes.filter {
        val nomMatch = query.isBlank() ||
                it.nomClient.contains(query, ignoreCase = true) ||
                it.salle.contains(query, ignoreCase = true)

        val date = try { normalizeDate(sdfInput.parse(it.date)!!) } catch (e: Exception) { null }

        val dateOK = when (filtre) {
            "PASSEE" -> date != null && date.before(today)
            "FUTURE" -> date != null && !date.before(today)
            "ENTRE DEUX DATES" -> {
                if (date != null && dateDebut != null && dateFin != null) {
                    val debut = normalizeDate(dateDebut!!)
                    val fin = normalizeDate(dateFin!!)
                    !date.before(debut) && !date.after(fin)
                } else false
            }
            else -> true
        }

        nomMatch && dateOK
    }.sortedBy {
        try { sdfSort.parse(it.date) } catch (e: Exception) { null }
    }

    val grouped = commandesFiltrees.groupBy {
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

            // 🔎 Barre recherche + filtre
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Rechercher...", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = Color(0xFFFFC107),
                        unfocusedBorderColor = Color.Gray
                    )
                )
                Spacer(Modifier.width(8.dp))
                FilterDropdown(filtre) { filtre = it }
            }

            if (filtre == "ENTRE DEUX DATES") {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DateSelector("Date début", dateDebut) { dateDebut = it }
                    DateSelector("Date fin", dateFin) { dateFin = it }
                }
            }

            // 📋 Liste des commandes
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = scrollState,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                grouped.forEach { (mois, items) ->
                    item {
                        Text(
                            text = mois,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                    }
                    items(items) { commande ->
                        CommandeSwipeItem(
                            commande = commande,
                            onDeleteClick = {
                                viewModel.supprimerCommandeSoft(commande.id!!) {
                                    Toast.makeText(context, "Commande supprimée", Toast.LENGTH_SHORT).show()
                                    viewModel.fetchCommandes()
                                }
                            },
                            onFicheClick = { navController.navigate("ficheCommande/${commande.id}") },
                            onDuplicateClick = {
                                val duplicated = commande.toDTO().copy(
                                    id = null,
                                    nomClient = "" // ✅ on vide le client
                                )
                                viewModel.creerCommande(duplicated,
                                    onSuccess = { newId ->
                                        val commandeClonee = duplicated.copy(id = newId) // ⚠️ on repart de duplicated pour garder le nom vide
                                        navController.currentBackStackEntry
                                            ?.savedStateHandle
                                            ?.set("commandeExistante", commandeClonee)

                                        navController.navigate("creerCommande/${commande.typeClient}") {
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                            ,
                            content = {
                                CommandeCard(commande) {
                                    val dto = commande.toDTO()
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("commandeExistante", dto)
                                    navController.navigate("creerCommande/${commande.typeClient}") {
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // ➕ Boutons ajout commande
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Particulier", "Entreprise", "Partenaire").forEach { label ->
                    Button(
                        onClick = { navController.navigate("creerCommande/${label.uppercase()}") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                        modifier = Modifier.weight(1f).height(40.dp),
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
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
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
            Text(text = dateFormatted, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

// 🔽 Réutilisés depuis PaiementsScreen
@Composable
fun FilterDropdown(selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("TOUS", "FUTURE", "PASSEE", "ENTRE DEUX DATES")
    Box {
        OutlinedButton(onClick = { expanded = true }) { Text(selected) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelected(it)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DateSelector(label: String, selectedDate: Date?, onDateSelected: (Date) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }
    OutlinedButton(onClick = { datePickerDialog.show() }) {
        Text(selectedDate?.let { sdf.format(it) } ?: label)
    }
}

fun normalizeDate(date: Date): Date {
    val cal = Calendar.getInstance()
    cal.time = date
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.time
}
package com.example.cateringapp.ui.screen.paiements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cateringapp.data.dto.Commande
import com.example.cateringapp.ui.component.StatsPaiementRow
import com.example.cateringapp.viewmodel.CommandeViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PaiementsScreen(navController: NavController, viewModel: CommandeViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        viewModel.chargerToutesLesAvances(viewModel.commandes.value)
    }

    val commandes by viewModel.commandes.collectAsState()
    val avancesMap by viewModel.avances.collectAsState()
    val context = LocalContext.current

    var query by remember { mutableStateOf("") }
    var filtre by remember { mutableStateOf("TOUS") }
    var dateDebut by remember { mutableStateOf<Date?>(null) }
    var dateFin by remember { mutableStateOf<Date?>(null) }

    val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sdfMois = SimpleDateFormat("MMMM", Locale.FRENCH)
    val sdfSort = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = remember { Date() }



    val commandesFiltrees = commandes.filter {
        val nomMatch = it.nomClient.contains(query, ignoreCase = true) || it.salle.contains(query, ignoreCase = true)
        val date = try { sdfInput.parse(it.date) } catch (e: Exception) { null }
        val dateOK = when (filtre) {
            "PASSEE" -> date != null && date.before(today)
            "FUTURE" -> date != null && date.after(today)
            "ENTRE DEUX DATES" -> {
                val debutOk = dateDebut?.let { date != null && !date.before(it) } ?: false
                val finOk = dateFin?.let { date != null && !date.after(it) } ?: false
                debutOk && finOk
            }
            else -> true
        }

        nomMatch && dateOK
    }

    val totalCA = commandesFiltrees.sumOf { it.total }
    val totalPaye = commandesFiltrees.sumOf { commande ->
        val avances = avancesMap[commande.id] ?: emptyList()
        avances.sumOf { it.montant }
    }
    val totalReste = totalCA - totalPaye

    val grouped = commandesFiltrees
        .sortedBy { try { sdfSort.parse(it.date) } catch (e: Exception) { null } }
        .groupBy {
            try {
                val date = sdfInput.parse(it.date)
                sdfMois.format(date!!).replaceFirstChar { c -> c.uppercase() }
            } catch (e: Exception) {
                "Inconnue"
            }
        }

    Scaffold(
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(8.dp)
                .fillMaxSize()
        ) {
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Rechercher...") },
                    singleLine = true,
                    colors = textFieldColors()
                )
                Spacer(Modifier.width(8.dp))
                FilterDropdown(filtre) { filtre = it }
            }
            if (filtre == "ENTRE DEUX DATES") {
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateSelector("Date début", dateDebut) { dateDebut = it }
                    DateSelector("Date fin", dateFin) { dateFin = it }
                }
            }

            Spacer(Modifier.height(2.dp))
            StatsPaiementRow(totalCA, totalPaye, totalReste)
            Spacer(Modifier.height(3.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                grouped.forEach { (mois, list) ->
                    item {
                        Text(mois, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    items(list) { commande ->
                        val avances = avancesMap[commande.id] ?: emptyList()
                        val reste = commande.total - avances.sumOf { it.montant }

                        PaiementCard(
                            commande = commande,
                            reste = reste,
                            onDollarClick = {
                                commande.id?.let { id ->
                                    navController.navigate("avancesCommande/$id")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaiementCard(commande: Commande, reste: Double, onDollarClick: () -> Unit)
 {
    val dateFormatted = try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(commande.date)
        SimpleDateFormat("dd/MM", Locale.getDefault()).format(date!!)
    } catch (e: Exception) {
        "??/??"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        colors = cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.medium,
        elevation = cardElevation(4.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${commande.typeCommande} : ${commande.total} Dh",
                    color = Color(0xFFFFC107),
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { onDollarClick() }) {
                    Icon(Icons.Default.AttachMoney, contentDescription = "Avances", tint = Color.Green)
                }
            }
            Text("${commande.nomClient} | ${commande.salle} | ${commande.nombreTables} tables | $dateFormatted")
            Text("Reste à payer : ${"%.2f".format(reste)} Dh", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun FilterDropdown(selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("TOUS", "FUTURE", "PASSEE", "ENTRE DEUX DATES")

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selected)
        }
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
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color(0xFFFFC107),
    unfocusedBorderColor = Color.Gray,
    cursorColor = Color.White,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = Color.Gray
)
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

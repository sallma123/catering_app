package com.example.cateringapp.ui.screen.paiements

import androidx.compose.foundation.text.KeyboardOptions
import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.cateringapp.data.dto.Avance
import com.example.cateringapp.data.dto.Commande
import com.example.cateringapp.viewmodel.CommandeViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cateringapp.ui.component.StatsPaiementRow
import kotlinx.coroutines.flow.map

@Composable
fun AvancesCommandeScreen(
    navController: NavController,
    commandeId: Long,
    viewModel: CommandeViewModel = viewModel()
) {
    val context = LocalContext.current
    val commandeState = viewModel.commandes
        .map { it.find { it.id == commandeId } }
        .collectAsState(initial = null)

    val commande = commandeState.value ?: return
    LaunchedEffect(commande.id) {
        viewModel.chargerAvancesPourCommande(commande.id!!)
    }

    var montant by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(getTodayFr()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val avances = viewModel.getAvancesForCommande(commande.id ?: 0L).collectAsState(initial = emptyList())
    val reste = commande.total - avances.value.sumOf { it.montant }

    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                date = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
        showDatePicker = false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Text("Avances pour ${commande.nomClient}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(1.dp))

            StatsPaiementRow(
                total = commande.total,
                paye = commande.total - reste,
                reste = reste
            )

            Spacer(Modifier.height(1.dp))
            Text("Historique des avances", color = Color.Gray)
        }

        items(avances.value) { avance ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("${avance.montant} Dh", fontWeight = FontWeight.Bold)
                    Text("Date: ${convertIsoToFr(avance.date)}")
                }
            }
        }

        item {
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = montant,
                onValueChange = { montant = it },
                label = { Text("Montant avance") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = textFieldColorsAvance(),
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            ) {
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    label = { Text("Date avance") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.White,
                        disabledBorderColor = Color.Gray,
                        disabledLabelColor = Color.Gray,
                        disabledTrailingIconColor = Color.Gray,
                        disabledPlaceholderColor = Color.Gray
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val montantVal = montant.toDoubleOrNull()
                    if (montantVal == null || montantVal <= 0) {
                        Toast.makeText(context, "Montant invalide", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (montantVal > reste) {
                        Toast.makeText(context, "Montant dépasse le reste à payer", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.ajouterAvance(
                        commande.id!!,
                        Avance(montant = montantVal, date = convertToIsoDate(date))
                    )
                    // 🔄 Forcer rafraîchissement global après ajout
                    viewModel.fetchCommandes()
                    montant = ""
                    date = getTodayFr()
                    Toast.makeText(context, "Avance ajoutée", Toast.LENGTH_SHORT).show()

                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text("Ajouter Avance", color = Color.Black)
            }
        }
    }
}

fun getTodayFr(): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
}

fun convertToIsoDate(dateFr: String): String {
    return try {
        val fr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val iso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        iso.format(fr.parse(dateFr)!!)
    } catch (e: Exception) {
        "2025-01-01"
    }
}

fun convertIsoToFr(dateIso: String): String {
    return try {
        val iso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        fr.format(iso.parse(dateIso)!!)
    } catch (e: Exception) {
        getTodayFr()
    }
}


@Composable
fun textFieldColorsAvance() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color(0xFFFFC107),
    unfocusedBorderColor = Color.Gray,
    cursorColor = Color.White,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = Color.Gray
)

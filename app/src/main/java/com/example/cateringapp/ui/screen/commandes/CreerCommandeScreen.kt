package com.example.cateringapp.ui.screen.commandes

import CommandeDTO
import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.example.cateringapp.viewmodel.CommandeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreerCommandeScreen(
    typeClient: String,
    navController: NavController,
    commandeInitiale: CommandeDTO? = null,
    commandeViewModel: CommandeViewModel
) {
    val context = LocalContext.current

    var nomClient by remember { mutableStateOf(commandeInitiale?.nomClient ?: "") }
    var salle by remember { mutableStateOf(commandeInitiale?.salle ?: "") }
    var nombre by remember { mutableStateOf(commandeInitiale?.nombreTables?.toString() ?: "") }
    var date by remember { mutableStateOf(commandeInitiale?.date?.let { convertIsoToFr(it) } ?: getTodayFr()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var typeCommande by remember { mutableStateOf(commandeInitiale?.typeCommande ?: "") }
    var statut by remember { mutableStateOf(commandeInitiale?.statut ?: "PAYEE") }

    // ✅ MODIF : Variable locale pour stocker la commande courante (modifiée ou non)
    var commandeCourante by remember { mutableStateOf(commandeInitiale) }

    val typesParticulier = listOf("Mariage", "Anniversaire", "Baptême")
    val typesPro = listOf("Buffet de soutenance", "Repas coffret", "Séminaire")
    val statuts = listOf("PAYEE", "NON_PAYEE")

    val commandesOptions = if (typeClient.equals("Entreprise", true)) typesPro else typesParticulier
    val isNombreTable = typeClient.equals("Particulier", true) || typeClient.equals("Partenaire", true)

    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                date = String.format("%02d/%02d/%04d", day, month + 1, year)
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnCancelListener { showDatePicker = false }
        }.show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (commandeInitiale != null) "Modification commande" else "Création commande : ${typeClient.uppercase()}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownField("Type de commande", commandesOptions, typeCommande) {
            typeCommande = it
        }

        OutlinedTextField(
            value = nomClient,
            onValueChange = { nomClient = it },
            label = { Text("Nom du client") },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors()
        )

        OutlinedTextField(
            value = salle,
            onValueChange = { salle = it },
            label = { Text("Salle") },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors()
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text(if (isNombreTable) "Nombre de tables" else "Nombre de personnes") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = textFieldColors()
        )

        OutlinedTextField(
            value = date,
            onValueChange = {},
            label = { Text("Date") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            colors = textFieldColors()
        )

        ExposedDropdownField("Statut", statuts, statut) {
            statut = it
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (commandeInitiale != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val isoDate = convertToIsoDate(date)

                        val commandeModifiee = commandeInitiale.copy(
                            nomClient = nomClient,
                            salle = salle,
                            nombreTables = nombre.toIntOrNull() ?: 0,
                            prixParTable = commandeInitiale.prixParTable,
                            typeCommande = mapTypeCommandeLabelToEnum(typeCommande),
                            statut = statut,
                            date = isoDate
                        )

                        val id = commandeInitiale.id
                        if (id == null || id == 0L) {
                            Toast.makeText(context, "ID manquant : impossible de modifier", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        commandeViewModel.modifierCommande(
                            id = id,
                            commandeDTO = commandeModifiee,
                            onSuccess = {
                                Log.d("CreerCommandeScreen", "Commande modifiée avec succès, id=$id")
                                Toast.makeText(context, "Commande modifiée avec succès", Toast.LENGTH_SHORT).show()

                                // ✅ Mise à jour de la commande en mémoire
                                commandeCourante = commandeModifiee.copy(id = id)

                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("refreshCommandes", true)
                            },
                            onError = {
                                Log.e("CreerCommandeScreen", "Erreur lors de la modification de la commande id=$id")
                                Toast.makeText(context, "Erreur lors de la modification", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Modifier", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        // ✅ Utiliser la commandeCourante (modifiée ou non)
                        commandeCourante?.let {
                            navController.currentBackStackEntry?.savedStateHandle?.set("commande", it)
                            navController.navigate("selectionProduits")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
                ) {
                    Text("Suivant", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Button(
                onClick = {
                    if (salle.isBlank() || nombre.isBlank() || typeCommande.isBlank()) {
                        Toast.makeText(context, "Veuillez remplir les champs obligatoires", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val isoDate = convertToIsoDate(date)

                    val commande = CommandeDTO(
                        nomClient = nomClient,
                        salle = salle,
                        nombreTables = nombre.toIntOrNull() ?: 0,
                        prixParTable = 0.0,
                        typeClient = typeClient.uppercase(),
                        typeCommande = mapTypeCommandeLabelToEnum(typeCommande),
                        statut = statut,
                        date = isoDate,
                        produits = emptyList()
                    )

                    navController.currentBackStackEntry?.savedStateHandle?.set("commande", commande)
                    navController.navigate("selectionProduits")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
            ) {
                Text("Suivant", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    LaunchedEffect(Unit) {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.remove<CommandeDTO>("commandeExistante")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownField(label: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = textFieldColors()
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
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

fun getTodayFr(): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
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

fun mapTypeCommandeLabelToEnum(label: String): String {
    return when (label.trim()) {
        "Mariage" -> "MARIAGE"
        "Anniversaire" -> "ANNIVERSAIRE"
        "Baptême" -> "BAPTEME"
        "Buffet de soutenance" -> "BUFFET_DE_SOUTENANCE"
        "Repas coffret" -> "REPAS_COFFRET"
        "Séminaire" -> "SÉMINAIRE"
        else -> label.uppercase().replace(" ", "_")
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


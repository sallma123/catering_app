package com.example.cateringapp.ui.screen.corbeille

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.cateringapp.data.dto.Commande
import com.example.cateringapp.viewmodel.CommandeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CorbeilleScreen(
    navController: NavHostController,
    viewModel: CommandeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val corbeille by viewModel.corbeille.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchCorbeille()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Commandes supprimées", color = Color.White, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212)),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 100.dp, // ✅ espace pour le scroll
                start = 8.dp,
                end = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(corbeille) { commande ->
                CorbeilleCard(
                    commande = commande,
                    onRestaurer = {
                        viewModel.restaurerCommande(commande.id ?: 0) {
                            navController.getBackStackEntry("commandes")
                                .savedStateHandle["refreshCommandes"] = true
                            viewModel.fetchCorbeille()
                        }
                    },
                    onSupprimer = {
                        viewModel.supprimerDefinitivement(commande.id ?: 0) {
                            viewModel.fetchCorbeille()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CorbeilleCard(
    commande: Commande,
    onRestaurer: () -> Unit,
    onSupprimer: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("${commande.nomClient} | ${commande.salle}", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Text("Date : ${commande.date}")
            Text("Total : ${commande.total} MAD")

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onRestaurer,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Restaurer")
                }

                Button(
                    onClick = onSupprimer,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Supprimer")
                }
            }
        }
    }
}

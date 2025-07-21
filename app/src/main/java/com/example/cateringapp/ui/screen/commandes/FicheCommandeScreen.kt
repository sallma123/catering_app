package com.example.cateringapp.ui.screen.commandes

import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.Icons
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.cateringapp.data.remote.ApiService
import com.example.cateringapp.data.remote.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun FicheCommandeScreen(id: Long, apiService: ApiService = RetrofitInstance.api) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var pdfUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    // Lancer téléchargement dès l'ouverture
    LaunchedEffect(id) {
        val result = telechargerEtEnregistrerPDF(context, id, apiService)
        when (result) {
            is ResultPDF.Success -> {
                pdfUri = result.uri
                showDialog = true
            }
            is ResultPDF.Error -> {
                errorMessage = result.message
            }
        }
        isLoading = false
    }

    // UI principale
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFFFFC107))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Génération de la fiche PDF...", color = Color.White)
                }
            }

            errorMessage != null -> {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("Erreur") },
                    text = { Text(errorMessage ?: "Erreur inconnue") },
                    confirmButton = {
                        TextButton(onClick = { /* tu peux ajouter navController.popBackStack() ici */ }) {
                            Text("Fermer")
                        }
                    }
                )
            }

            pdfUri != null && showDialog -> {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("✅ Fiche PDF générée") },
                    text = { Text("Que souhaitez-vous faire ?") },
                    confirmButton = {
                        TextButton(onClick = {
                            ouvrirPDF(context, pdfUri!!)
                        }) {
                            Text("📄 Ouvrir")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            partagerPDF(context, pdfUri!!)
                        }) {
                            Text("📤 Partager")
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Description, contentDescription = "PDF", tint = Color(0xFFFFC107))
                    },
                    tonalElevation = 8.dp,
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    textContentColor = Color.DarkGray
                )
            }
        }
    }

    // Gérer le bouton retour
    BackHandler(enabled = !isLoading && !showDialog) {
        // Tu peux gérer la navigation retour ici si besoin
    }
}

private sealed class ResultPDF {
    data class Success(val uri: Uri) : ResultPDF()
    data class Error(val message: String) : ResultPDF()
}

private suspend fun telechargerEtEnregistrerPDF(context: Context, id: Long, api: ApiService): ResultPDF {
    return withContext(Dispatchers.IO) {
        try {
            val response = api.telechargerFiche(id).execute()
            if (response.isSuccessful && response.body() != null) {
                val pdfFile = File(context.getExternalFilesDir(null), "fiche_commande_$id.pdf")
                val inputStream: InputStream = response.body()!!.byteStream()
                val outputStream = FileOutputStream(pdfFile)

                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }

                inputStream.close()
                outputStream.close()

                val uri = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".provider",
                    pdfFile
                )

                ResultPDF.Success(uri)
            } else {
                ResultPDF.Error("Réponse invalide : ${response.code()}")
            }
        } catch (e: Exception) {
            ResultPDF.Error("Erreur réseau : ${e.localizedMessage}")
        }
    }
}

private fun ouvrirPDF(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(uri, "application/pdf")
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Aucune application pour ouvrir le PDF", Toast.LENGTH_LONG).show()
    }
}

private fun partagerPDF(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "application/pdf"
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(intent, "Partager avec..."))
}

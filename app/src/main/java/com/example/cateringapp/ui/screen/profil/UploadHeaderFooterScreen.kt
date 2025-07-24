package com.example.cateringapp.ui.screen.profil

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.cateringapp.data.remote.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.InputStream
import java.io.OutputStream

@Composable
fun UploadHeaderFooterScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Fichiers persistants dans filesDir
    val headerFile = File(context.filesDir, "header.jpg")
    val footerFile = File(context.filesDir, "footer.jpg")

    var headerUri by remember { mutableStateOf(if (headerFile.exists()) Uri.fromFile(headerFile) else null) }
    var footerUri by remember { mutableStateOf(if (footerFile.exists()) Uri.fromFile(footerFile) else null) }

    val headerPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri ->
            headerUri = uri
            saveUriToFile(context, uri, headerFile)
            headerUri = Uri.fromFile(headerFile)
        }
    }

    val footerPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri ->
            footerUri = uri
            saveUriToFile(context, uri, footerFile)
            footerUri = Uri.fromFile(footerFile)
        }
    }

    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Ajouter l'entête & pied de page",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = "Définissez les images qui seront automatiquement ajoutées à chaque fiche PDF générée.",
            color = Color.LightGray,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 24.dp),
            lineHeight = 20.sp
        )


        Spacer(modifier = Modifier.height(5.dp))

        Button(
            onClick = { headerPicker.launch("image/*") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
        ) {
            Icon(Icons.Default.Image, contentDescription = null, tint = Color.Black)
            Spacer(Modifier.width(8.dp))
            Text("Sélectionner l’image d’entête", color = Color.Black)
        }

        headerUri?.let {
            Spacer(Modifier.height(12.dp))
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Header Preview",
                modifier = Modifier
                    .height(140.dp)
                    .fillMaxWidth()
                    .background(Color.White),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { footerPicker.launch("image/*") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
        ) {
            Icon(Icons.Default.Image, contentDescription = null, tint = Color.Black)
            Spacer(Modifier.width(8.dp))
            Text("Sélectionner l’image de pied de page", color = Color.Black)
        }

        footerUri?.let {
            Spacer(Modifier.height(12.dp))
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Footer Preview",
                modifier = Modifier
                    .height(140.dp)
                    .fillMaxWidth()
                    .background(Color.White),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        var uploadSuccess = false

                        if (headerFile.exists()) {
                            val part = MultipartBody.Part.createFormData(
                                "file", headerFile.name,
                                headerFile.asRequestBody("image/*".toMediaTypeOrNull())
                            )
                            val response = RetrofitInstance.api.uploadHeader(part)
                            if (response.isSuccessful) uploadSuccess = true
                        }

                        if (footerFile.exists()) {
                            val part = MultipartBody.Part.createFormData(
                                "file", footerFile.name,
                                footerFile.asRequestBody("image/*".toMediaTypeOrNull())
                            )
                            val response = RetrofitInstance.api.uploadFooter(part)
                            if (response.isSuccessful) uploadSuccess = true
                        }

                        message = if (uploadSuccess) "✅ Upload effectué avec succès" else "⚠️ Aucune image uploadée"
                        isError = !uploadSuccess
                    } catch (e: Exception) {
                        message = "❌ Erreur lors de l'upload"
                        isError = true
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Envoyer", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (message.isNotEmpty()) {
            Text(
                text = message,
                color = if (isError) Color.Red else Color(0xFF4CAF50)
            )
        }
    }
}

// Copie l'image sélectionnée dans un fichier local
private fun saveUriToFile(context: android.content.Context, uri: Uri, file: File) {
    try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val outputStream: OutputStream = file.outputStream()
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

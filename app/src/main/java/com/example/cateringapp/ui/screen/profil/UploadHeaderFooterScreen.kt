package com.example.cateringapp.ui.screen.profil

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.cateringapp.data.remote.RetrofitInstance
import com.example.cateringapp.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@Composable
fun UploadHeaderFooterScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var headerUri by remember { mutableStateOf<Uri?>(null) }
    var footerUri by remember { mutableStateOf<Uri?>(null) }

    val headerPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        headerUri = it
    }

    val footerPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        footerUri = it
    }

    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🖼️ Upload entête et pied de page", color = Color.White, fontSize = MaterialTheme.typography.titleMedium.fontSize)

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { headerPicker.launch("image/*") }) {
            Text("Sélectionner l’image d’entête")
        }

        headerUri?.let {
            Spacer(Modifier.height(10.dp))
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Header Preview",
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { footerPicker.launch("image/*") }) {
            Text("Sélectionner l’image de pied de page")
        }

        footerUri?.let {
            Spacer(Modifier.height(10.dp))
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Footer Preview",
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    if (headerUri != null && footerUri != null) {
                        val headerFile = FileUtils.getFileFromUri(context, headerUri!!)
                        val footerFile = FileUtils.getFileFromUri(context, footerUri!!)

                        val headerPart = MultipartBody.Part.createFormData(
                            "header", headerFile.name,
                            headerFile.asRequestBody("image/*".toMediaTypeOrNull())
                        )
                        val footerPart = MultipartBody.Part.createFormData(
                            "footer", footerFile.name,
                            footerFile.asRequestBody("image/*".toMediaTypeOrNull())
                        )

                        val response = RetrofitInstance.api.uploadHeaderAndFooter(headerPart, footerPart)
                        message = if (response.isSuccessful) {
                            "✅ Upload réussi !"
                        } else {
                            "❌ Erreur API : ${response.code()} - ${response.errorBody()?.string() ?: "inconnue"}"
                        }
                    } else {
                        message = "❌ Sélectionnez les deux images"
                    }
                }
            }
        ) {
            Text("Envoyer", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(message, color = Color.White)
    }
}

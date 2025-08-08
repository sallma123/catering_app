package com.example.cateringapp.ui.screen.profil

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.cateringapp.data.local.SessionManager
import com.example.cateringapp.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangerMotDePasseScreen(navController: NavHostController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()

    var ancienMotDePasse by remember { mutableStateOf("") }
    var nouveauMotDePasse by remember { mutableStateOf("") }
    var confirmationMotDePasse by remember { mutableStateOf("") }

    var ancienVisible by remember { mutableStateOf(false) }
    var nouveauVisible by remember { mutableStateOf(false) }
    var confirmationVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Changer le mot de passe",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = ancienMotDePasse,
                onValueChange = { ancienMotDePasse = it },
                label = { Text("Ancien mot de passe") },
                visualTransformation = if (ancienVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { ancienVisible = !ancienVisible }) {
                        Icon(
                            imageVector = if (ancienVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (ancienVisible) "Masquer" else "Afficher",
                            tint = Color.LightGray
                        )
                    }
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    containerColor = Color.Transparent,
                    focusedBorderColor = Color(0xFFFFC107),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFFFFC107),
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = Color(0xFFFFC107)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = nouveauMotDePasse,
                onValueChange = { nouveauMotDePasse = it },
                label = { Text("Nouveau mot de passe") },
                visualTransformation = if (nouveauVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { nouveauVisible = !nouveauVisible }) {
                        Icon(
                            imageVector = if (nouveauVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (nouveauVisible) "Masquer" else "Afficher",
                            tint = Color.LightGray
                        )
                    }
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    containerColor = Color.Transparent,
                    focusedBorderColor = Color(0xFFFFC107),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFFFFC107),
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = Color(0xFFFFC107)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = confirmationMotDePasse,
                onValueChange = { confirmationMotDePasse = it },
                label = { Text("Confirmer le mot de passe") },
                visualTransformation = if (confirmationVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { confirmationVisible = !confirmationVisible }) {
                        Icon(
                            imageVector = if (confirmationVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (confirmationVisible) "Masquer" else "Afficher",
                            tint = Color.LightGray
                        )
                    }
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    containerColor = Color.Transparent,
                    focusedBorderColor = Color(0xFFFFC107),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFFFFC107),
                    unfocusedLabelColor = Color.LightGray,
                    cursorColor = Color(0xFFFFC107)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (nouveauMotDePasse != confirmationMotDePasse) {
                        Toast.makeText(
                            context,
                            "Les mots de passe ne correspondent pas",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        coroutineScope.launch {
                            try {
                                val email = sessionManager.userEmail.first() ?: ""
                                val response = RetrofitInstance.api.changerMotDePasse(
                                    email,
                                    ancienMotDePasse,
                                    nouveauMotDePasse
                                )
                                if (response.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "Mot de passe changé avec succès",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Erreur : ${response.code()}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Erreur : ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
            ) {
                Text("Valider", color = Color.Black)
            }
        }
    }
}

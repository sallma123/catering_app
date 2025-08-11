package com.example.cateringapp.ui.screen.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cateringapp.MainActivity
import com.example.cateringapp.data.local.SessionManager
import com.example.cateringapp.ui.theme.CateringAppTheme
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appLinkData = intent?.data
        if (appLinkData != null && appLinkData.scheme == "cateringapp") {
            val email = appLinkData.pathSegments.getOrNull(0) ?: ""
            val token = appLinkData.pathSegments.getOrNull(1) ?: ""

            setContent {
                CateringAppTheme {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "reset_password/{email}/{token}"
                            .replace("{email}", email)
                            .replace("{token}", token)
                    ) {
                        composable("reset_password/{email}/{token}") {
                            ResetPasswordScreen(navController, email, token)
                        }
                        composable("login") {
                            LoginScreen(navController = navController)
                        }
                    }
                }
            }
            return // éviter d'exécuter la suite
        }

        val sessionManager = SessionManager(this)

        lifecycleScope.launch {
            sessionManager.isLoggedIn.collect { loggedIn ->
                if (loggedIn) {
                    // ✅ Déjà connecté → Aller direct au MainActivity
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    setContent {
                        CateringAppTheme {
                            val navController = rememberNavController()

                            // ✅ NavHost pour gérer Login / Register / ForgotPassword
                            NavHost(
                                navController = navController,
                                startDestination = "login"
                            ) {
                                composable("login") {
                                    LoginScreen(navController = navController)
                                }
                                composable("register") {
                                    RegisterScreen(navController = navController)
                                }
                                composable("forgot_password") {
                                    ForgotPasswordScreen(navController = navController)
                                }
                                composable("reset_password/{email}/{token}") { backStackEntry ->
                                    val email = backStackEntry.arguments?.getString("email") ?: ""
                                    val token = backStackEntry.arguments?.getString("token") ?: ""
                                    ResetPasswordScreen(navController, email, token)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

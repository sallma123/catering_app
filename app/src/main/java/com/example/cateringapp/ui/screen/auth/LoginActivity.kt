package com.example.cateringapp.ui.screen.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.cateringapp.MainActivity
import com.example.cateringapp.data.local.SessionManager
import com.example.cateringapp.ui.theme.CateringAppTheme
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)
        lifecycleScope.launch {
            sessionManager.isLoggedIn.collect { loggedIn ->
                if (loggedIn) {
                    // ✅ Rediriger si déjà connecté
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    setContent {
                        CateringAppTheme {
                            LoginScreen()
                        }
                    }
                }
            }
        }
    }

}

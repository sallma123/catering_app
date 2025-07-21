package com.example.cateringapp.ui.screen.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.cateringapp.ui.theme.CateringAppTheme

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CateringAppTheme {
                RegisterScreen()
            }
        }
    }
}

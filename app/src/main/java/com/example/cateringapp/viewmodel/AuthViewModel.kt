package com.example.cateringapp.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cateringapp.data.dto.LoginRequest
import com.example.cateringapp.data.dto.LoginResponse
import com.example.cateringapp.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = RetrofitInstance.api.loginUser(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!

                    val prefs = getApplication<Application>()
                        .getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("email", user.email).apply()

                    _loginState.value = LoginState.Success(user)
                } else {
                    _loginState.value = LoginState.Error("Email ou mot de passe incorrect")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Erreur réseau : ${e.message}")
            }
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: LoginResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}

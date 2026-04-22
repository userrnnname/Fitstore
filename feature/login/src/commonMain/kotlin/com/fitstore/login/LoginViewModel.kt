package com.fitstore.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitstore.data.domain.CustomerRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: CustomerRepository
) : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var loading by mutableStateOf(false)

    fun updateEmail(value: String) {
        email = value
    }

    fun updatePassword(value: String) {
        password = value
    }

    fun signIn(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            loading = true
            repository.signInWithEmail(
                email = email,
                password = password,
                onSuccess = {
                    loading = false
                    onSuccess()
                },
                onError = {
                    loading = false
                    onError(it)
                }
            )
        }
    }
}
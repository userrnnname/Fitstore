package com.fitstore.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitstore.data.domain.CustomerRepository
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repository: CustomerRepository
) : ViewModel() {
    var firstName by mutableStateOf("")
        private set
    var lastName by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    var loading by mutableStateOf(false)
        private set

    fun updateFirstName(value: String) { firstName = value }
    fun updateLastName(value: String) { lastName = value }
    fun updateEmail(value: String) { email = value }
    fun updatePassword(value: String) { password = value }

    val isFormValid: Boolean get() =
        lastName.length in 3..50 &&
                firstName.length in 3..50 &&
                email.contains("@") &&
                password.length >= 6

    fun signUp(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            loading = true
            repository.signUpWithEmail(
                email = email,
                password = password,
                lastName = lastName,
                firstName = firstName,
                onSuccess = {
                    loading = false
                    onSuccess()
                },
                onError = { message ->
                    loading = false
                    onError(message)
                }
            )
        }
    }
}

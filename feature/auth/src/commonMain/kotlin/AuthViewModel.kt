package com.fitstore.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class AuthViewModel(
    private val supabase: SupabaseClient
) : ViewModel() {

    fun checkSession(onAuthenticated: () -> Unit) {
        viewModelScope.launch {
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                onAuthenticated()
            }
        }
    }
}
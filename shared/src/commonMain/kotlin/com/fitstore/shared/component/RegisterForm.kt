package com.fitstore.shared.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun RegisterForm(
    modifier: Modifier = Modifier,
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CustomTextField(
            value = lastName,
            onValueChange = onLastNameChange,
            placeholder = "Фамилия",
            error = lastName.isNotEmpty() && lastName.length !in 3..50
        )
        CustomTextField(
            value = firstName,
            onValueChange = onFirstNameChange,
            placeholder = "Имя",
            error = firstName.isNotEmpty() && firstName.length !in 3..50
        )
        CustomTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = "Email",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        PasswordTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "Пароль",
            error = password.isNotEmpty() && password.length < 6
        )
    }
}

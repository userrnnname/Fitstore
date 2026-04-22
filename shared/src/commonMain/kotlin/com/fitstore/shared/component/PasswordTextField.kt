package com.fitstore.shared.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.fitstore.shared.Alpha
import com.fitstore.shared.IconPrimary
import com.fitstore.shared.Resources
import org.jetbrains.compose.resources.painterResource

@Composable
fun PasswordTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Пароль",
    error: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    CustomTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        error = error,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val icon = if (passwordVisible) Resources.Icon.EyeOpen else Resources.Icon.EyeClosed
            IconButton(
                modifier = Modifier.padding(end = 4.dp),
                onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль",
                    modifier = Modifier.size(24.dp),
                    tint = IconPrimary
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
}
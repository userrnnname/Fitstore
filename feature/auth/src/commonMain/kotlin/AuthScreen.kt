package com.fitstore.auth

import ContentWithMessageBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fitstore.auth.component.SignInButton
import com.fitstore.shared.Alpha
import com.fitstore.shared.AuthLoadingState
import com.fitstore.shared.FontSize
import com.fitstore.shared.K2DFont
import com.fitstore.shared.Surface
import com.fitstore.shared.SurfaceBrand
import com.fitstore.shared.SurfaceError
import com.fitstore.shared.TextPrimary
import com.fitstore.shared.TextSecondary
import com.fitstore.shared.TextWhite
import org.koin.compose.viewmodel.koinViewModel
import rememberMessageBarState

@Composable
fun AuthScreen(
    navigateToHome: () -> Unit,
    navigateToLogin: () -> Unit
) {

    val viewModel = koinViewModel<AuthViewModel>()
    val messageBarState = rememberMessageBarState()
    var loadingState by remember { mutableStateOf(AuthLoadingState.IDLE) }


    LaunchedEffect(Unit) {
        viewModel.checkSession {
            navigateToHome()
        }
    }

    Scaffold (
        containerColor = Surface
    ) { padding ->
        ContentWithMessageBar(
            contentBackgroundColor = Surface,
            modifier = Modifier
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                ),
            messageBarState = messageBarState,
            errorMaxLines = 2,
            errorContainerColor = SurfaceError,
            errorContentColor = TextWhite,
            successContainerColor = SurfaceBrand,
            successContentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 24.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "FITSTORE",
                        textAlign = TextAlign.Center,
                        fontFamily = K2DFont(),
                        fontSize = FontSize.EXTRA_LARGE,
                        color = TextSecondary
                    )
                    Text(
                        modifier = Modifier
                            .width(187.dp)
                            .alpha(Alpha.HALF),
                        text = "Войдите, чтобы продолжить",
                        textAlign = TextAlign.Center,
                        fontSize = FontSize.EXTRA_REGULAR,
                        color = TextPrimary
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SignInButton(
                        loading = false,
                        enabled = (loadingState == AuthLoadingState.IDLE),
                        onClick = navigateToLogin
                    )
                }
            }
        }
    }
}
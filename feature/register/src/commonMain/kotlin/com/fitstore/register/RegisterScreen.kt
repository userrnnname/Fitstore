package com.fitstore.register

import ContentWithMessageBar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fitstore.shared.Alpha
import com.fitstore.shared.FontSize
import com.fitstore.shared.IconPrimary
import com.fitstore.shared.K2DFont
import com.fitstore.shared.Resources
import com.fitstore.shared.Surface
import com.fitstore.shared.SurfaceBrand
import com.fitstore.shared.SurfaceError
import com.fitstore.shared.TextPrimary
import com.fitstore.shared.TextSecondary
import com.fitstore.shared.TextWhite
import com.fitstore.shared.component.PrimaryButton
import com.fitstore.shared.component.RegisterForm
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import rememberMessageBarState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navigateBack: () -> Unit,
    navigateToHome: () -> Unit
) {
    val viewModel = koinViewModel<RegisterViewModel>()
    val messageBarState = rememberMessageBarState()
    val scope = rememberCoroutineScope()
    val isKeyboardVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 100.dp

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "РЕГИСТРАЦИЯ",
                        fontFamily = K2DFont(),
                        fontSize = FontSize.LARGE,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = "Назад",
                            tint = IconPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { padding ->
        ContentWithMessageBar(
            contentBackgroundColor = Surface,
            modifier = Modifier.padding(padding),
            messageBarState = messageBarState,
            errorMaxLines = 2,
            errorContainerColor = SurfaceError,
            errorContentColor = TextWhite,
            successContainerColor = SurfaceBrand,
            successContentColor = TextPrimary
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .imePadding()
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedVisibility(
                        visible = !isKeyboardVisible,
                        enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
                        exit = fadeOut(animationSpec = tween(300)) + shrinkVertically()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                                text = "Зарегистрируйтесь, чтобы продолжить",
                                textAlign = TextAlign.Center,
                                fontSize = FontSize.EXTRA_REGULAR,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                    RegisterForm(
                        firstName = viewModel.firstName,
                        onFirstNameChange = viewModel::updateFirstName,
                        lastName = viewModel.lastName,
                        onLastNameChange = viewModel::updateLastName,
                        email = viewModel.email,
                        onEmailChange = viewModel::updateEmail,
                        password = viewModel.password,
                        onPasswordChange = viewModel::updatePassword
                    )
                    Spacer(modifier = Modifier.height(120.dp))
                }
                PrimaryButton(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    text = "Создать профиль",
                    icon = Resources.Icon.Register,
                    enabled = !viewModel.loading && viewModel.isFormValid,
                    onClick = {
                        viewModel.signUp(
                            onSuccess = {
                                scope.launch {
                                    messageBarState.addSuccess("Профиль успешно создан!")
                                    delay(2000)
                                    navigateToHome()
                                }
                            },
                            onError = { message ->
                                messageBarState.addError(message)
                            }
                        )
                    }
                )
            }
        }
    }
}



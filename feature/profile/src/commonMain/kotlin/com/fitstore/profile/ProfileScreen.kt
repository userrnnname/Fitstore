package com.fitstore.profile

import ContentWithMessageBar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitstore.profile.component.SupplementCard
import com.fitstore.shared.*
import com.fitstore.shared.component.InfoCard
import com.fitstore.shared.component.LoadingCard
import com.fitstore.shared.component.dialog.ServingsDialog
import com.fitstore.shared.domain.SupplementTrack
import com.fitstore.shared.util.DisplayResult
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import rememberMessageBarState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navigateBack: () -> Unit,
    navigateToEditProfile: () -> Unit
) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val screenReady = viewModel.screenReady
    val screenState = viewModel.screenState
    val messageBarState = rememberMessageBarState()

    var editingTrack by remember { mutableStateOf<SupplementTrack?>(null) }

    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "МОЙ ПРОФИЛЬ",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        ContentWithMessageBar(
            contentBackgroundColor = Surface,
            modifier = Modifier.padding(padding),
            messageBarState = messageBarState,
            errorContainerColor = SurfaceError,
            errorContentColor = TextWhite,
            successContainerColor = SurfaceBrand,
            successContentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    .imePadding()
            ) {
                screenReady.DisplayResult(
                    onLoading = { LoadingCard(modifier = Modifier.fillMaxSize()) },
                    onSuccess = {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceLighter)
                                    .border(
                                        width = 1.dp,
                                        color = BorderIdle,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(
                                            horizontal = 14.dp,
                                            vertical = 18.dp
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(Resources.Icon.Person),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = IconPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${screenState.firstName} ${screenState.lastName}",
                                        color = TextPrimary,
                                        style = TextStyle(
                                            fontSize = FontSize.REGULAR,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }

                                IconButton(
                                    onClick = { navigateToEditProfile() },
                                ) {
                                    Icon(
                                        painter = painterResource(Resources.Icon.Edit),
                                        contentDescription = "Изменить",
                                        modifier = Modifier.size(18.dp),
                                        tint = IconPrimary.copy(alpha = Alpha.DISABLED)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Мои курсы",
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                color = TextPrimary.copy(alpha = Alpha.HALF),
                                fontSize = FontSize.REGULAR,
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (screenState.supplements.isEmpty()) {
                                InfoCard(
                                    image = Resources.Image.Cat,
                                    title = "Упс!",
                                    subtitle = "На данный момент у вас нет активных курсов"
                                )
                            } else {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    screenState.supplements.forEach { track ->
                                        key(track.id) {
                                            SupplementCard(
                                                track = track,
                                                onTakeServing = {
                                                    viewModel.takeServing(
                                                        track,
                                                        onError = { messageBarState.addError(it) })
                                                },
                                                onDelete = {
                                                    viewModel.deleteSupplementTrack(
                                                        track.id ?: "",
                                                        onError = { messageBarState.addError(it) })
                                                },
                                                onEditClick = { editingTrack = track }
                                            )
                                        }
                                    }
                                    editingTrack?.let { track ->
                                        ServingsDialog(
                                            track = track,
                                            onDismiss = { editingTrack = null },
                                            onConfirmClick = { newRem, newTot ->
                                                viewModel.updateSupplementTrack(
                                                    track = track,
                                                    newRemaining = newRem,
                                                    newTotal = newTot,
                                                    onError = { messageBarState.addError(it) }
                                                )
                                                editingTrack = null
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Купить снова",
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                color = TextPrimary.copy(alpha = Alpha.HALF),
                                fontSize = FontSize.REGULAR,
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            /*if (screenState.lastPurchases.isEmpty()) {
                                InfoCard(
                                    image = Resources.Image.Cart,
                                    title = "История пуста",
                                    subtitle = "Ознакомьтесь с нашим ассортиментом товаров"
                                )
                            } else {
                                Column(
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    screenState.lastPurchases.take(3).forEach { product ->
                                        ProductCard(
                                            product = product,
                                            onClick = { /* Навигация на Details */ }
                                        )
                                    }
                                }
                            }*/

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                )
            }
        }
    }
}


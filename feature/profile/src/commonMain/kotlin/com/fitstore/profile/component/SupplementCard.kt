package com.fitstore.profile.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.fitstore.shared.Alpha
import com.fitstore.shared.BorderIdle
import com.fitstore.shared.FontSize
import com.fitstore.shared.IconPrimary
import com.fitstore.shared.ProgressHigh
import com.fitstore.shared.ProgressLow
import com.fitstore.shared.ProgressMiddle
import com.fitstore.shared.Resources
import com.fitstore.shared.RobotoCondensedFont
import com.fitstore.shared.Surface
import com.fitstore.shared.SurfaceError
import com.fitstore.shared.TextBrand
import com.fitstore.shared.TextPrimary
import com.fitstore.shared.TextSecondary
import com.fitstore.shared.TextWhite
import com.fitstore.shared.domain.SupplementTrack
import com.fitstore.shared.util.formatTimestamp
import org.jetbrains.compose.resources.painterResource

@Composable
fun SupplementCard(
    modifier: Modifier = Modifier,
    track: SupplementTrack,
    onTakeServing: () -> Unit,
    onDelete: () -> Unit,
    onEditClick: () -> Unit
) {
    AnimatedContent(
        targetState = track,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "CardUpdateAnimation"
    ) { animatedTrack ->
        val progress = track.remainingServings.toFloat() / track.totalServings.toFloat()
        var menuExpanded by remember { mutableStateOf(false) }

        val statusColor = when {
            progress <= 0.20f -> ProgressLow
            progress <= 0.59f -> ProgressMiddle
            else -> ProgressHigh
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clip(RoundedCornerShape(12.dp))
                .background(statusColor.copy(alpha = Alpha.TWENTY_PERCENT))
                .border(
                    width = 1.dp,
                    color = statusColor.copy(alpha = Alpha.HALF),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            AsyncImage(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(size = 12.dp))
                    .border(
                        width = 1.dp,
                        color = BorderIdle,
                        shape = RoundedCornerShape(size = 12.dp)
                    ),
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(track.productThumbnail)
                    .crossfade(enable = true)
                    .build(),
                contentDescription = "Миниатюра",
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(all = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = track.productTitle.uppercase(),
                        fontSize = FontSize.MEDIUM,
                        color = TextPrimary,
                        fontFamily = RobotoCondensedFont(),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                painter = painterResource(Resources.Icon.VerticalMenu),
                                contentDescription = null,
                                tint = IconPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            containerColor = Surface
                        ) {
                            DropdownMenuItem(
                                text = { Text("Изменить кол-во", fontSize = FontSize.SMALL) },
                                leadingIcon = {
                                    Icon(
                                        painterResource(Resources.Icon.Edit),
                                        null,
                                        Modifier.size(18.dp)
                                    )
                                },
                                onClick = { menuExpanded = false; onEditClick() }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Удалить",
                                        color = SurfaceError,
                                        fontSize = FontSize.SMALL
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        painterResource(Resources.Icon.Delete),
                                        null,
                                        Modifier.size(18.dp),
                                        tint = SurfaceError
                                    )
                                },
                                onClick = { menuExpanded = false; onDelete() }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${track.remainingServings}/${track.totalServings}",
                    fontSize = FontSize.REGULAR,
                    color = TextPrimary.copy(alpha = Alpha.DISABLED),
                    fontWeight = FontWeight.Medium
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(64.dp)),
                    color = statusColor,
                    trackColor = BorderIdle,
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val lastDate = track.lastTakenDate
                    if (lastDate != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier.size(12.dp),
                                painter = painterResource(Resources.Icon.Clock),
                                contentDescription = "Часы"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = lastDate.toLong().formatTimestamp(),
                                fontSize = FontSize.EXTRA_SMALL,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    val isEnabled = track.remainingServings > 0
                    Text(
                        text = "Принял порцию",
                        modifier = Modifier
                            .height(32.dp)
                            .alpha(if (isEnabled) Alpha.FULL else Alpha.DISABLED)
                            .clickable(
                                enabled = isEnabled,
                                onClick = onTakeServing
                            )
                            .wrapContentHeight(), // Чтобы текст был по центру высоты 32.dp
                        style = TextStyle(
                            color = if (isEnabled) TextPrimary else TextWhite,
                            fontSize = FontSize.EXTRA_REGULAR,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

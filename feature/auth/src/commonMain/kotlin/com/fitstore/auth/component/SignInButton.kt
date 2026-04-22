package com.fitstore.auth.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.fitstore.shared.Alpha
import com.fitstore.shared.BorderIdle
import com.fitstore.shared.FontSize
import com.fitstore.shared.IconSecondary
import com.fitstore.shared.SurfaceDarker
import com.fitstore.shared.SurfaceLighter
import com.fitstore.shared.TextPrimary

@Composable
fun SignInButton(
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true,
    primaryText: String = "Войти",
    secondaryText: String = "Пожалуйста подождите...",
    shape: Shape = RoundedCornerShape(size = 99.dp),
    backgroundColor: Color = SurfaceLighter,
    borderColor: Color = BorderIdle,
    progressIndicatorColor: Color = IconSecondary,
    onClick: () -> Unit,
) {
    var buttonText by remember { mutableStateOf(primaryText) }

    LaunchedEffect(loading) {
        buttonText = if (loading) secondaryText else primaryText
    }

    val textAlpha = if (loading || enabled) Alpha.FULL else Alpha.DISABLED
    val containerAlpha = if (loading || enabled) backgroundColor else SurfaceDarker

    Surface (
        modifier = modifier
            .clip(shape)
            .border(
                width = 1.dp,
                color = if (enabled) borderColor else borderColor.copy(alpha = Alpha.DISABLED),
                shape = shape
            )
            .clickable(enabled = enabled && !loading) { onClick() },
        color = containerAlpha
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 20.dp)
                .animateContentSize(
                    animationSpec = tween(durationMillis = 200)
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = loading
            ) { loadingState ->
                if (loadingState) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = progressIndicatorColor
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = buttonText,
                color = TextPrimary.copy(alpha = textAlpha),
                fontSize = FontSize.REGULAR
            )
        }
    }
}
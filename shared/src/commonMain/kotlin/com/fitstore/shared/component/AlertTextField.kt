package com.fitstore.shared.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.fitstore.shared.BorderIdle
import com.fitstore.shared.FontSize
import com.fitstore.shared.SurfaceLighter
import com.fitstore.shared.TextPrimary

@Composable
fun AlertTextField(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .background(SurfaceLighter)
            .border(
                width = 1.dp,
                color = BorderIdle,
                shape = RoundedCornerShape(size = 6.dp)
            )
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(
                vertical = 16.dp,
                horizontal = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = FontSize.REGULAR,
            color = TextPrimary
        )
    }
}
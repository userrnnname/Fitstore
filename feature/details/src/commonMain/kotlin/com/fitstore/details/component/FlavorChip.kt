package com.fitstore.details.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitstore.shared.BorderIdle
import com.fitstore.shared.BorderSecondary
import com.fitstore.shared.FontSize
import com.fitstore.shared.Surface
import com.fitstore.shared.TextPrimary
import com.fitstore.shared.TextSecondary

@Composable
fun FlavorChip(
    flavor: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(size = 12.dp))
            .clickable { onClick() }
            .background(Surface)
            .border(
                width = 1.dp,
                color = if (isSelected) BorderSecondary else BorderIdle,
                shape = RoundedCornerShape(size = 12.dp)
            )
            .padding(all = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = flavor,
            fontSize = FontSize.SMALL,
            color = if (isSelected) TextSecondary else TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}
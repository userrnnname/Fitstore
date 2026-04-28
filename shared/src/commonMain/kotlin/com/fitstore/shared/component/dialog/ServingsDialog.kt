package com.fitstore.shared.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.fitstore.shared.Alpha
import com.fitstore.shared.BorderIdle
import com.fitstore.shared.FontSize
import com.fitstore.shared.K2DFont
import com.fitstore.shared.Surface
import com.fitstore.shared.SurfaceDarker
import com.fitstore.shared.SurfaceLighter
import com.fitstore.shared.TextBrand
import com.fitstore.shared.TextPrimary
import com.fitstore.shared.TextSecondary
import com.fitstore.shared.component.CustomTextField
import com.fitstore.shared.domain.SupplementTrack

@Composable
fun ServingsDialog(
    track: SupplementTrack,
    onDismiss: () -> Unit,
    onConfirmClick: (Int, Int) -> Unit,
) {
    var remainingText by remember { mutableStateOf(track.remainingServings.toString()) }
    var totalText by remember { mutableStateOf(track.totalServings.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .width(312.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Surface)
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Изменить курс",
                    color = TextPrimary,
                    fontSize = FontSize.EXTRA_MEDIUM,
                    fontFamily = K2DFont(),
                    modifier = Modifier.fillMaxWidth()
                )

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Оставшихся порций:", fontSize = FontSize.REGULAR, color = TextPrimary)
                    CustomTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceLighter)
                            .border(1.dp, BorderIdle, RoundedCornerShape(6.dp)),
                        value = remainingText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) remainingText = it },
                        placeholder = "Осталось",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Text(text = "Порций всего:", fontSize = FontSize.REGULAR, color = TextPrimary)
                    CustomTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceLighter)
                            .border(1.dp, BorderIdle, RoundedCornerShape(6.dp)),
                        value = totalText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) totalText = it },
                        placeholder = "Всего",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Отменить",
                        fontSize = FontSize.REGULAR,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        val rem = remainingText.toIntOrNull() ?: track.remainingServings
                        val tot = totalText.toIntOrNull() ?: track.totalServings
                        onConfirmClick(rem, tot)
                    }
                ) {
                    Text(
                        text = "Подтвердить",
                        color = TextSecondary,
                        fontSize = FontSize.REGULAR,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

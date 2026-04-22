package com.fitstore.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import fitstore.shared.generated.resources.Res
import fitstore.shared.generated.resources.k2d_medium
import fitstore.shared.generated.resources.roboto_condensed_medium
import org.jetbrains.compose.resources.Font

@Composable
fun K2DFont() = FontFamily(
    Font(Res.font.k2d_medium)
)

@Composable
fun RobotoCondensedFont() = FontFamily(
    Font(Res.font.roboto_condensed_medium)
)

object FontSize {
    val EXTRA_SMALL = 10.sp
    val SMALL = 12.sp
    val REGULAR = 14.sp
    val EXTRA_REGULAR = 16.sp
    val MEDIUM = 18.sp
    val EXTRA_MEDIUM = 20.sp
    val LARGE = 30.sp
    val EXTRA_LARGE = 40.sp
}
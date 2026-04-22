package com.fitstore.shared.component

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.fitstore.shared.FontSize
import com.fitstore.shared.K2DFont
import com.fitstore.shared.SurfaceBrand
import com.fitstore.shared.TextPrimary

@Composable
fun AuthLinkText(
    modifier: Modifier = Modifier,
    fullText: String,
    linkText: String,
    onLinkClick: () -> Unit
) {
    val annotatedString = buildAnnotatedString {
        val startIndex = fullText.indexOf(linkText)
        val endIndex = startIndex + linkText.length

        append(fullText)

        if (startIndex != -1) {
            addStyle(
                style = SpanStyle(
                    color = SurfaceBrand,
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Bold
                ),
                start = startIndex,
                end = endIndex
            )
            addStringAnnotation(
                tag = "URL",
                annotation = "click",
                start = startIndex,
                end = endIndex
            )
        }
    }

    ClickableText(
        text = annotatedString,
        style = TextStyle(
            textAlign = TextAlign.Center,
            color = TextPrimary.copy(alpha = 0.5f),
            fontSize = FontSize.EXTRA_REGULAR,
            fontFamily = K2DFont()
        ),
        modifier = modifier,
        onClick = { offset ->
            annotatedString.getStringAnnotations("URL", offset, offset)
                .firstOrNull()?.let { onLinkClick() }
        }
    )
}

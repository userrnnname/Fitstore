package com.fitstore.products_overview.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.fitstore.shared.Alpha
import com.fitstore.shared.FontSize
import com.fitstore.shared.IconWhite
import com.fitstore.shared.Resources
import com.fitstore.shared.RobotoCondensedFont
import com.fitstore.shared.TextBrand
import com.fitstore.shared.TextWhite
import com.fitstore.shared.domain.Product
import com.fitstore.shared.domain.ProductCategory
import com.fitstore.shared.util.formatPrice
import org.jetbrains.compose.resources.painterResource

@Composable
fun MainProductCard(
    modifier: Modifier = Modifier,
    product: Product,
    isLarge: Boolean = false,
    onClick: () -> Unit,
    //onClick: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedScale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val animatedRotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(size = 12.dp))
            //.clickable { onClick(product.id) }
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .animateContentSize()
                .then(
                    if (isLarge) Modifier
                        .scale(animatedScale.value)
                        .rotate(animatedRotation.value)
                    else Modifier
                ),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(product.thumbnail)
                .crossfade(enable = true)
                .build(),
            contentDescription = "Миниатюра",
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black,
                            Color.Black.copy(Alpha.ZERO)
                        ),
                        startY = Float.POSITIVE_INFINITY,
                        endY = 0.0f
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 12.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = product.title,
                fontSize = FontSize.EXTRA_MEDIUM,
                fontWeight = FontWeight.Medium,
                color = TextWhite,
                fontFamily = RobotoCondensedFont(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = product.description,
                fontSize = FontSize.REGULAR,
                lineHeight = FontSize.REGULAR * 1.3f,
                color = TextWhite.copy(alpha = Alpha.HALF),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AnimatedContent(
                    targetState = product.category
                ) { category ->
                    if (ProductCategory.fromString(category) == ProductCategory.Accessories) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier.size(14.dp),
                                painter = painterResource(Resources.Icon.Weight),
                                contentDescription = "Вес",
                                tint = IconWhite
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${product.weight} гр.",
                                fontSize = FontSize.EXTRA_SMALL,
                                color = TextWhite
                            )
                        }
                    }
                }
                Text(
                    text = "${product.price.formatPrice()} руб.",
                    fontSize = FontSize.EXTRA_REGULAR,
                    color = TextBrand,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
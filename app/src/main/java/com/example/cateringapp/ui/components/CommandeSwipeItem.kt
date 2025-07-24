package com.example.cateringapp.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.example.cateringapp.data.dto.Commande
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
fun CommandeSwipeItem(
    commande: Commande,
    onDeleteClick: () -> Unit,
    onFicheClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val maxSwipe = 180.dp
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffset by animateDpAsState(targetValue = offsetX.dp, label = "swipe")

    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(Color.Transparent)
    ) {
        // Action buttons background
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionButton(
                color = Color(0xFFAD9FFF),
                icon = Icons.Default.ContentCopy,
                onClick = onDuplicateClick
            )

            ActionButton(color = Color(0xFFFFC107), icon = Icons.Default.Description, onClick = onFicheClick)
            ActionButton(color = Color.Red, icon = Icons.Default.Delete, onClick = onDeleteClick)
        }

        // Foreground content (Commande card)
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consumeAllChanges()
                            offsetX = (offsetX + dragAmount).coerceIn(-with(density) { maxSwipe.toPx() }, 0f)
                        },
                        onDragEnd = {
                            // Snap back if swipe not enough
                            if (offsetX > -with(density) { maxSwipe.toPx() } * 0.5f) {
                                offsetX = 0f
                            }
                        }
                    )
                }
                .zIndex(1f)
        ) {
            content()
        }
    }
}

@Composable
private fun ActionButton(color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(60.dp)
            .fillMaxHeight()
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White)
    }
}

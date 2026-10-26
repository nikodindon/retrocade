package com.nikodindon.retrocade.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nikodindon.retrocade.core.common.ProceduralIcon

@Composable
fun NeonPanel(
    modifier: Modifier = Modifier,
    accent: Color = RetroColors.NeonGreen,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .background(RetroColors.Surface.copy(alpha = 0.86f), RoundedCornerShape(4.dp))
            .border(1.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(4.dp))
            .padding(14.dp),
    ) {
        content()
    }
}

@Composable
fun RetroHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = RetroColors.NeonGreen,
                )
            }
            Spacer(Modifier.width(6.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = RetroColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = RetroColors.NeonGreen,
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun RetroButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = RetroColors.NeonGreen,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    val shape = RoundedCornerShape(3.dp)
    Box(
        modifier = modifier
            .background(
                color = if (enabled) accent.copy(alpha = 0.12f) else RetroColors.Surface,
                shape = shape,
            )
            .border(1.dp, if (enabled) accent else RetroColors.TextDim, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) accent else RetroColors.TextDim,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(9.dp))
            }
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) accent else RetroColors.TextDim,
            )
        }
    }
}

@Composable
fun PixelIcon(
    icon: ProceduralIcon,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        val rows = icon.rows
        if (rows.isEmpty()) return@Canvas
        val columns = rows.maxOf { it.length }
        val px = minOf(size.width / columns, size.height / rows.size)
        val startX = (size.width - columns * px) / 2f
        val startY = (size.height - rows.size * px) / 2f
        val color = Color(icon.color)
        rows.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { columnIndex, char ->
                if (char == 'X' || char == '#') {
                    drawRect(
                        color = color,
                        topLeft = Offset(startX + columnIndex * px, startY + rowIndex * px),
                        size = Size(px * 0.9f, px * 0.9f),
                    )
                }
            }
        }
    }
}

@Composable
fun PixelDivider(modifier: Modifier = Modifier, color: Color = RetroColors.Grid) {
    Canvas(
        modifier
            .fillMaxWidth()
            .height(2.dp),
    ) {
        var x = 0f
        while (x < size.width) {
            drawRect(
                color = color,
                topLeft = Offset(x, 0f),
                size = Size(6f, size.height),
            )
            x += 10f
        }
    }
}

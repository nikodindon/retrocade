package com.nikodindon.retrocade.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikodindon.retrocade.core.storage.ScoreBook
import com.nikodindon.retrocade.core.ui.NeonPanel
import com.nikodindon.retrocade.core.ui.PixelDivider
import com.nikodindon.retrocade.core.ui.PixelIcon
import com.nikodindon.retrocade.core.ui.RetroBackdrop
import com.nikodindon.retrocade.core.ui.RetroButton
import com.nikodindon.retrocade.core.ui.RetroColors
import com.nikodindon.retrocade.games.api.Game
import com.nikodindon.retrocade.ui.formatScore

@Composable
fun HubScreen(
    onPlay: () -> Unit,
    onScores: () -> Unit,
    onSettings: () -> Unit,
    onStore: () -> Unit,
) {
    val viewModel: HubViewModel = hiltViewModel()
    val scores by viewModel.scores.collectAsStateWithLifecycle()
    val premium by viewModel.premium.collectAsStateWithLifecycle()

    RetroBackdrop(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
        ) {
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "RÉTROCADE",
                        style = MaterialTheme.typography.displayLarge,
                        color = RetroColors.NeonGreen,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "ARCADE SYSTEM  ·  BUILD 0.1.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = RetroColors.TextMuted,
                    )
                }
                StatusBadge(premium = premium)
            }

            Text(
                text = "Des jeux d'arcade old school, dessinés et sonorisés par le code.",
                style = MaterialTheme.typography.bodyMedium,
                color = RetroColors.TextMuted,
                modifier = Modifier.padding(top = 12.dp),
            )

            Spacer(Modifier.height(18.dp))
            PixelDivider()
            Spacer(Modifier.height(16.dp))

            Text(
                text = "CATALOGUE",
                style = MaterialTheme.typography.labelSmall,
                color = RetroColors.NeonCyan,
            )
            Spacer(Modifier.height(8.dp))
            viewModel.games.forEach { game ->
                GameCard(
                    game = game,
                    best = scores.forGame(game.meta.id).best,
                    onPlay = onPlay,
                )
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(14.dp))
            PixelDivider()
            Spacer(Modifier.height(16.dp))

            Text(
                text = "MENU",
                style = MaterialTheme.typography.labelSmall,
                color = RetroColors.NeonCyan,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RetroButton(
                    label = "Jouer",
                    onClick = onPlay,
                    icon = Icons.Default.PlayArrow,
                    modifier = Modifier.weight(1f),
                )
                RetroButton(
                    label = "Scores",
                    onClick = onScores,
                    accent = RetroColors.NeonCyan,
                    icon = Icons.Default.Star,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RetroButton(
                    label = "Paramètres",
                    onClick = onSettings,
                    accent = RetroColors.TextMuted,
                    icon = Icons.Default.Settings,
                    modifier = Modifier.weight(1f),
                )
                RetroButton(
                    label = "Boutique",
                    onClick = onStore,
                    accent = RetroColors.Amber,
                    icon = Icons.Default.ShoppingCart,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "AUCUN ASSET BINAIRE  ·  TOUT EST PROCÉDURAL",
                style = MaterialTheme.typography.labelSmall,
                color = RetroColors.TextDim,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Fait avec passion pour l'époque des bornes d'arcade.",
                style = MaterialTheme.typography.labelSmall,
                color = RetroColors.TextDim,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatusBadge(premium: Boolean) {
    val color = if (premium) RetroColors.Amber else RetroColors.TextDim
    Box(
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
    ) {
        Text(
            text = if (premium) "PREMIUM" else "FREE",
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

@Composable
private fun GameCard(
    game: Game,
    best: Int,
    onPlay: () -> Unit,
) {
    val accent = Color(game.meta.accentColor)
    NeonPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay),
        accent = accent,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .background(
                        accent.copy(alpha = 0.08f),
                        RoundedCornerShape(3.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                PixelIcon(
                    icon = game.meta.icon,
                    modifier = Modifier.size(52.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = game.meta.name.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = RetroColors.TextPrimary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = game.meta.releaseLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                    )
                }
                Text(
                    text = game.meta.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = RetroColors.TextMuted,
                )
                Text(
                    text = "MEILLEUR SCORE  ${formatScore(best)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = RetroColors.TextDim,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Jouer à ${game.meta.name}",
                tint = accent,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

package com.nikodindon.retrocade.scores

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikodindon.retrocade.core.storage.ScoreEntry
import com.nikodindon.retrocade.core.ui.NeonPanel
import com.nikodindon.retrocade.core.ui.PixelDivider
import com.nikodindon.retrocade.core.ui.RetroBackdrop
import com.nikodindon.retrocade.core.ui.RetroColors
import com.nikodindon.retrocade.core.ui.RetroHeader
import com.nikodindon.retrocade.ui.formatDate
import com.nikodindon.retrocade.ui.formatScore

@Composable
fun ScoresScreen(onBack: () -> Unit) {
    val viewModel: ScoresViewModel = hiltViewModel()
    val boards by viewModel.boards.collectAsStateWithLifecycle()

    RetroBackdrop(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
        ) {
            RetroHeader(
                title = "Scores",
                subtitle = "Classements locaux · top 10",
                onBack = onBack,
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 4.dp,
                    bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(boards, key = { it.gameId }) { board ->
                    NeonPanel(
                        modifier = Modifier.fillMaxWidth(),
                        accent = Color(board.accentColor),
                    ) {
                        Column(Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = board.name.uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(board.accentColor),
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = "BEST ${formatScore(board.scores.best)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RetroColors.TextMuted,
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            PixelDivider()
                            Spacer(Modifier.height(8.dp))

                            if (board.scores.entries.isEmpty()) {
                                Text(
                                    text = "Aucun score. Lancez une partie.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = RetroColors.TextDim,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                )
                            } else {
                                board.scores.entries.forEachIndexed { index, entry ->
                                    ScoreRow(index + 1, entry, Color(board.accentColor))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreRow(rank: Int, entry: ScoreEntry, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (rank == 1) accent.copy(alpha = 0.06f) else Color.Transparent,
            )
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = rank.toString().padStart(2, '0'),
            style = MaterialTheme.typography.labelLarge,
            color = if (rank <= 3) accent else RetroColors.TextDim,
            modifier = Modifier.padding(end = 12.dp),
        )
        Text(
            text = formatScore(entry.score),
            style = MaterialTheme.typography.titleMedium,
            color = if (rank == 1) accent else RetroColors.TextPrimary,
            modifier = Modifier.weight(1f),
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${entry.lines} LIGNES  ·  NIV. ${entry.level}",
                style = MaterialTheme.typography.labelSmall,
                color = RetroColors.TextMuted,
            )
            Text(
                text = formatDate(entry.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = RetroColors.TextDim,
            )
        }
    }
}

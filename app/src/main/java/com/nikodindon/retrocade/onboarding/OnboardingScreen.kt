package com.nikodindon.retrocade.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nikodindon.retrocade.core.common.ProceduralIcon
import com.nikodindon.retrocade.core.ui.NeonPanel
import com.nikodindon.retrocade.core.ui.PixelIcon
import com.nikodindon.retrocade.core.ui.RetroBackdrop
import com.nikodindon.retrocade.core.ui.RetroButton
import com.nikodindon.retrocade.core.ui.RetroColors

private data class OnboardingPage(
    val eyebrow: String,
    val title: String,
    val body: String,
    val icon: ProceduralIcon,
)

private val pages = listOf(
    OnboardingPage(
        eyebrow = "BIENVENUE",
        title = "L'ARCADE DANS\nVOTRE POCHE",
        body = "Rétrocade rassemble de petits jeux old school, sans asset binaire : tout est dessiné et sonorisé par le code.",
        icon = ProceduralIcon(
            rows = listOf(
                "........",
                "...XX...",
                "...XX...",
                ".XXXXXX.",
                "..XXXX..",
                ".XX..XX.",
                "........",
                "........",
            ),
            color = 0xFF39FFB6,
        ),
    ),
    OnboardingPage(
        eyebrow = "PROCÉDURAL",
        title = "AUCUN ASSET,\nQUE DU CODE",
        body = "Graphismes Canvas, musique chiptune et effets sonores sont générés en temps réel. Les parties restent reproductibles à partir d'une graine.",
        icon = ProceduralIcon(
            rows = listOf(
                "..XXXX..",
                ".X....X.",
                "X..XX..X",
                "X..XX..X",
                ".X....X.",
                "..XXXX..",
                "........",
                "........",
            ),
            color = 0xFF35D6FF,
        ),
    ),
    OnboardingPage(
        eyebrow = "PRÊT À JOUER",
        title = "DES SCORES,\nDES DÉFIS",
        body = "Chaque jeu conserve son meilleur score et son top 10 en local. Ajustez le son, les vibrations et les contrôles dans les paramètres.",
        icon = ProceduralIcon(
            rows = listOf(
                "........",
                "...XX...",
                "..XXXX..",
                ".XXXXXX.",
                "XXXXXXXX",
                "..X..X..",
                ".XX..XX.",
                "........",
            ),
            color = 0xFFFFC857,
        ),
    ),
)

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    var pageIndex by rememberSaveable { mutableIntStateOf(0) }
    val page = pages[pageIndex]
    val isLast = pageIndex == pages.lastIndex

    RetroBackdrop(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 22.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "RÉTROCADE",
                    style = MaterialTheme.typography.titleLarge,
                    color = RetroColors.NeonGreen,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = onDone,
                    modifier = Modifier.testTag("onboarding_skip"),
                ) {
                    Text(
                        text = "PASSER",
                        style = MaterialTheme.typography.labelLarge,
                        color = RetroColors.TextMuted,
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            NeonPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_page_${pageIndex + 1}"),
                accent = RetroColors.NeonCyan,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(148.dp)
                            .background(
                                RetroColors.Background.copy(alpha = 0.55f),
                                RoundedCornerShape(4.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        PixelIcon(
                            icon = page.icon,
                            modifier = Modifier.size(112.dp),
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = page.eyebrow,
                        style = MaterialTheme.typography.labelSmall,
                        color = RetroColors.NeonCyan,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.displayMedium,
                        color = RetroColors.TextPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = page.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = RetroColors.TextMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                pages.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (index == pageIndex) 24.dp else 8.dp, 8.dp)
                            .background(
                                if (index == pageIndex) RetroColors.NeonGreen else RetroColors.Grid,
                                RoundedCornerShape(1.dp),
                            ),
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            RetroButton(
                label = if (isLast) "Commencer" else "Suivant",
                onClick = {
                    if (isLast) {
                        onDone()
                    } else {
                        pageIndex += 1
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_next"),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Aucune publicité pendant la découverte.",
                style = MaterialTheme.typography.labelSmall,
                color = RetroColors.TextDim,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

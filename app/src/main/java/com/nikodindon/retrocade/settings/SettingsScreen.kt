package com.nikodindon.retrocade.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikodindon.retrocade.core.billing.PremiumState
import com.nikodindon.retrocade.core.billing.PremiumType
import com.nikodindon.retrocade.core.common.ControlScheme
import com.nikodindon.retrocade.core.common.sensitivityToSwipeThresholdDp
import com.nikodindon.retrocade.core.storage.AppSettings
import com.nikodindon.retrocade.core.ui.NeonPanel
import com.nikodindon.retrocade.core.ui.PixelDivider
import com.nikodindon.retrocade.core.ui.RetroBackdrop
import com.nikodindon.retrocade.core.ui.RetroButton
import com.nikodindon.retrocade.core.ui.RetroColors
import com.nikodindon.retrocade.core.ui.RetroHeader
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onStore: () -> Unit,
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val premium by viewModel.premium.collectAsStateWithLifecycle()

    RetroBackdrop(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
        ) {
            RetroHeader(
                title = "Paramètres",
                subtitle = "Audio, contrôles et achats",
                onBack = onBack,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp),
            ) {
                SectionLabel("AUDIO")
                NeonPanel(Modifier.fillMaxWidth()) {
                    Column {
                        SettingSwitch(
                            title = "Musique",
                            description = "Chiptune générée en temps réel",
                            checked = settings.musicEnabled,
                            onChange = viewModel::setMusicEnabled,
                        )
                        PixelDivider()
                        SettingSwitch(
                            title = "Effets sonores",
                            description = "Déplacements, rotations, lignes",
                            checked = settings.sfxEnabled,
                            onChange = viewModel::setSfxEnabled,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                SectionLabel("CONTRÔLES")
                NeonPanel(Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = "SCHÉMA",
                            style = MaterialTheme.typography.labelSmall,
                            color = RetroColors.TextDim,
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SchemeChip(
                                label = "Tactile",
                                selected = settings.controls == ControlScheme.TAP,
                                onClick = { viewModel.setControlScheme(ControlScheme.TAP) },
                                modifier = Modifier.weight(1f),
                            )
                            SchemeChip(
                                label = "Boutons",
                                selected = settings.controls == ControlScheme.BUTTONS,
                                onClick = { viewModel.setControlScheme(ControlScheme.BUTTONS) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        SettingSwitch(
                            title = "Vibrations",
                            description = "Retour haptique sur les actions",
                            checked = settings.hapticsEnabled,
                            onChange = viewModel::setHapticsEnabled,
                        )
                        PixelDivider()
                        SettingSwitch(
                            title = "Bouton réserve",
                            description = "Afficher HOLD dans le HUD",
                            checked = settings.showHoldButton,
                            onChange = viewModel::setShowHoldButton,
                        )
                        PixelDivider()
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "SENSIBILITÉ DES GLISSEMENTS",
                            style = MaterialTheme.typography.labelSmall,
                            color = RetroColors.TextDim,
                        )
                        Slider(
                            value = settings.swipeSensitivity,
                            onValueChange = viewModel::setSwipeSensitivity,
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = RetroColors.NeonCyan,
                                activeTrackColor = RetroColors.NeonCyan,
                                inactiveTrackColor = RetroColors.Grid,
                            ),
                        )
                        Text(
                            text = "Seuil ≈ ${sensitivityToSwipeThresholdDp(settings.swipeSensitivity).roundToInt()} dp",
                            style = MaterialTheme.typography.labelSmall,
                            color = RetroColors.TextMuted,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                SectionLabel("PREMIUM")
                NeonPanel(Modifier.fillMaxWidth(), accent = RetroColors.Amber) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            text = premiumLabel(premium),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (premium is PremiumState.Premium) {
                                RetroColors.Amber
                            } else {
                                RetroColors.TextPrimary
                            },
                        )
                        Text(
                            text = if (premium is PremiumState.Premium) {
                                "Aucune publicité. Merci de soutenir Rétrocade."
                            } else {
                                "Retirez les interstitiels et soutenez le projet."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = RetroColors.TextMuted,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                        )
                        RetroButton(
                            label = "Voir la boutique",
                            onClick = onStore,
                            accent = RetroColors.Amber,
                            icon = Icons.Default.ShoppingCart,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        RetroButton(
                            label = "Restaurer mes achats",
                            onClick = viewModel::restorePurchases,
                            accent = RetroColors.TextMuted,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        viewModel.restoreMessage?.let { message ->
                            Text(
                                text = message,
                                style = MaterialTheme.typography.labelSmall,
                                color = RetroColors.NeonCyan,
                                modifier = Modifier.padding(top = 10.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "VERSION 0.1.0  ·  MIN SDK 24",
                    style = MaterialTheme.typography.labelSmall,
                    color = RetroColors.TextDim,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = RetroColors.NeonCyan,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = RetroColors.TextPrimary,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = RetroColors.TextMuted,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = RetroColors.Black,
                checkedTrackColor = RetroColors.NeonGreen,
                uncheckedThumbColor = RetroColors.TextMuted,
                uncheckedTrackColor = RetroColors.SurfaceAlt,
                uncheckedBorderColor = RetroColors.Grid,
            ),
        )
    }
}

@Composable
private fun SchemeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = if (selected) RetroColors.NeonCyan else RetroColors.TextDim
    Row(
        modifier = modifier
            .background(accent.copy(alpha = if (selected) 0.14f else 0.04f), RoundedCornerShape(2.dp))
            .border(1.dp, accent, RoundedCornerShape(2.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = accent,
        )
    }
}

private fun premiumLabel(premium: PremiumState): String = when (premium) {
    PremiumState.Unknown -> "STATUT : VÉRIFICATION…"
    PremiumState.Free -> "OFFRE GRATUITE"
    is PremiumState.Premium -> when (premium.type) {
        PremiumType.MONTHLY -> "PREMIUM MENSUEL ACTIF"
        PremiumType.LIFETIME -> "PREMIUM À VIE ACTIF"
    }
}

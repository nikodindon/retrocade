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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikodindon.retrocade.core.billing.PremiumState
import com.nikodindon.retrocade.core.billing.PremiumType
import com.nikodindon.retrocade.core.common.ControlScheme
import com.nikodindon.retrocade.core.common.GestureSensitivity
import com.nikodindon.retrocade.core.storage.AppSettings
import com.nikodindon.retrocade.core.ui.NeonPanel
import com.nikodindon.retrocade.core.ui.PixelDivider
import com.nikodindon.retrocade.core.ui.RetroBackdrop
import com.nikodindon.retrocade.core.ui.RetroButton
import com.nikodindon.retrocade.core.ui.RetroColors
import com.nikodindon.retrocade.core.ui.RetroHeader
import kotlin.math.roundToInt

data class SettingsActions(
    val setMusicEnabled: (Boolean) -> Unit = {},
    val setSfxEnabled: (Boolean) -> Unit = {},
    val setMusicVolume: (Int) -> Unit = {},
    val setSfxVolume: (Int) -> Unit = {},
    val setHapticsEnabled: (Boolean) -> Unit = {},
    val setShowHoldButton: (Boolean) -> Unit = {},
    val setControlScheme: (ControlScheme) -> Unit = {},
    val setGestureSensitivity: (GestureSensitivity) -> Unit = {},
    val restorePurchases: () -> Unit = {},
    val resetScores: () -> Unit = {},
)

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onStore: () -> Unit,
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val premium by viewModel.premium.collectAsStateWithLifecycle()

    SettingsContent(
        settings = settings,
        premium = premium,
        restoreMessage = viewModel.restoreMessage,
        scoresMessage = viewModel.scoresMessage,
        onBack = onBack,
        onStore = onStore,
        actions = SettingsActions(
            setMusicEnabled = viewModel::setMusicEnabled,
            setSfxEnabled = viewModel::setSfxEnabled,
            setMusicVolume = viewModel::setMusicVolume,
            setSfxVolume = viewModel::setSfxVolume,
            setHapticsEnabled = viewModel::setHapticsEnabled,
            setShowHoldButton = viewModel::setShowHoldButton,
            setControlScheme = viewModel::setControlScheme,
            setGestureSensitivity = viewModel::setGestureSensitivity,
            restorePurchases = viewModel::restorePurchases,
            resetScores = viewModel::resetScores,
        ),
    )
}

/** Stateless settings screen, also used by Compose tests. */
@Composable
fun SettingsContent(
    settings: AppSettings,
    premium: PremiumState,
    restoreMessage: String?,
    scoresMessage: String?,
    onBack: () -> Unit,
    onStore: () -> Unit,
    actions: SettingsActions,
) {
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            modifier = Modifier.testTag("settings_reset_dialog"),
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "EFFACER LES SCORES ?",
                    style = MaterialTheme.typography.titleMedium,
                    color = RetroColors.NeonMagenta,
                )
            },
            text = {
                Text(
                    text = "Le meilleur score et les tops 10 de tous les jeux seront supprimés. Cette action est définitive.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RetroColors.TextMuted,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        actions.resetScores()
                    },
                    modifier = Modifier.testTag("settings_reset_confirm"),
                ) {
                    Text("EFFACER", color = RetroColors.NeonMagenta)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false },
                    modifier = Modifier.testTag("settings_reset_cancel"),
                ) {
                    Text("ANNULER", color = RetroColors.TextMuted)
                }
            },
            containerColor = RetroColors.Surface,
        )
    }

    RetroBackdrop(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
        ) {
            RetroHeader(
                title = "Paramètres",
                subtitle = "Audio, contrôles, données et achats",
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
                            onChange = actions.setMusicEnabled,
                            tag = "settings_music_switch",
                        )
                        VolumeSlider(
                            value = settings.musicVolume,
                            enabled = settings.musicEnabled,
                            onChange = actions.setMusicVolume,
                            testTag = "settings_music_volume",
                        )
                        PixelDivider()
                        SettingSwitch(
                            title = "Effets sonores",
                            description = "Déplacements, rotations, lignes",
                            checked = settings.sfxEnabled,
                            onChange = actions.setSfxEnabled,
                            tag = "settings_sfx_switch",
                        )
                        VolumeSlider(
                            value = settings.sfxVolume,
                            enabled = settings.sfxEnabled,
                            onChange = actions.setSfxVolume,
                            testTag = "settings_sfx_volume",
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
                                onClick = { actions.setControlScheme(ControlScheme.TAP) },
                                modifier = Modifier.weight(1f),
                                tag = "settings_scheme_tap",
                            )
                            SchemeChip(
                                label = "Boutons",
                                selected = settings.controls == ControlScheme.BUTTONS,
                                onClick = { actions.setControlScheme(ControlScheme.BUTTONS) },
                                modifier = Modifier.weight(1f),
                                tag = "settings_scheme_buttons",
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        SettingSwitch(
                            title = "Vibrations",
                            description = "Retour haptique sur les actions",
                            checked = settings.hapticsEnabled,
                            onChange = actions.setHapticsEnabled,
                            tag = "settings_haptics_switch",
                        )
                        PixelDivider()
                        SettingSwitch(
                            title = "Bouton réserve",
                            description = "Afficher HOLD dans le HUD",
                            checked = settings.showHoldButton,
                            onChange = actions.setShowHoldButton,
                            tag = "settings_hold_switch",
                        )
                        PixelDivider()
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "SENSIBILITÉ DES GLISSEMENTS",
                            style = MaterialTheme.typography.labelSmall,
                            color = RetroColors.TextDim,
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GestureSensitivity.entries.forEach { sensitivity ->
                                SchemeChip(
                                    label = sensitivity.label,
                                    selected = settings.gestures == sensitivity,
                                    onClick = { actions.setGestureSensitivity(sensitivity) },
                                    modifier = Modifier.weight(1f),
                                    tag = "settings_sensitivity_${sensitivity.name.lowercase()}",
                                )
                            }
                        }
                        Text(
                            text = "Seuil actuel ≈ ${settings.gestures.thresholdDp.roundToInt()} dp",
                            style = MaterialTheme.typography.labelSmall,
                            color = RetroColors.TextMuted,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                SectionLabel("DONNÉES LOCALES")
                NeonPanel(Modifier.fillMaxWidth(), accent = RetroColors.NeonMagenta) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            text = "Scores enregistrés",
                            style = MaterialTheme.typography.titleMedium,
                            color = RetroColors.TextPrimary,
                        )
                        Text(
                            text = "Efface les tops 10 et meilleurs scores de tous les jeux. Les réglages sont conservés.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RetroColors.TextMuted,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                        )
                        RetroButton(
                            label = "Effacer les scores",
                            onClick = { showResetDialog = true },
                            accent = RetroColors.NeonMagenta,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_reset_scores"),
                        )
                        scoresMessage?.let { message ->
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
                            onClick = actions.restorePurchases,
                            accent = RetroColors.TextMuted,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        restoreMessage?.let { message ->
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
                    text = "VERSION 0.2.0  ·  MIN SDK 24",
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
    tag: String,
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
            modifier = Modifier.testTag(tag),
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
private fun VolumeSlider(
    value: Int,
    enabled: Boolean,
    onChange: (Int) -> Unit,
    testTag: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.roundToInt()) },
            modifier = Modifier
                .weight(1f)
                .testTag(testTag),
            enabled = enabled,
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = RetroColors.NeonCyan,
                activeTrackColor = RetroColors.NeonCyan,
                inactiveTrackColor = RetroColors.Grid,
            ),
        )
        Text(
            text = "$value %",
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) RetroColors.TextMuted else RetroColors.TextDim,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun SchemeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String,
) {
    val accent = if (selected) RetroColors.NeonCyan else RetroColors.TextDim
    Row(
        modifier = modifier
            .testTag(tag)
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

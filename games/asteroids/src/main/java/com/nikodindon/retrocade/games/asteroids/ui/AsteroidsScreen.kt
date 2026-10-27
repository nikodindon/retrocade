package com.nikodindon.retrocade.games.asteroids.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikodindon.retrocade.core.audio.AudioController
import com.nikodindon.retrocade.core.audio.Sfx
import com.nikodindon.retrocade.core.common.ControlScheme
import com.nikodindon.retrocade.core.engine.GameCanvas
import com.nikodindon.retrocade.core.engine.rememberGameFrame
import com.nikodindon.retrocade.core.ui.HapticIntensity
import com.nikodindon.retrocade.core.ui.NeonPanel
import com.nikodindon.retrocade.core.ui.RetroButton
import com.nikodindon.retrocade.core.ui.RetroColors
import com.nikodindon.retrocade.core.ui.rememberHapticsController
import com.nikodindon.retrocade.games.api.GameScoreResult
import com.nikodindon.retrocade.games.api.GameStatus
import com.nikodindon.retrocade.games.api.InputEvent
import com.nikodindon.retrocade.games.asteroids.AsteroidsSession
import com.nikodindon.retrocade.games.asteroids.audio.AsteroidsMusic
import com.nikodindon.retrocade.games.asteroids.audio.AsteroidsSfx
import com.nikodindon.retrocade.games.asteroids.logic.AsteroidSize
import com.nikodindon.retrocade.games.asteroids.logic.AsteroidsEvent
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AsteroidsScreen(
    session: AsteroidsSession,
    audio: AudioController,
    bestScore: Int,
    sfxEnabled: Boolean,
    musicEnabled: Boolean,
    showHelpOnStart: Boolean,
    onHelpDismissed: () -> Unit,
    onExit: () -> Unit,
    onReplay: () -> Unit,
    onOpenPaywall: () -> Unit,
    onRecordScore: suspend (score: Int, lines: Int, level: Int) -> GameScoreResult,
    modifier: Modifier = Modifier,
) {
    val ui by session.uiState.collectAsStateWithLifecycle()
    val haptics = rememberHapticsController(ui.settings.hapticsEnabled)
    val hapticsState by rememberUpdatedState(haptics)
    val sfxState by rememberUpdatedState(sfxEnabled)
    val recordCallback by rememberUpdatedState(onRecordScore)

    var helpVisible by rememberSaveable(showHelpOnStart) {
        mutableStateOf(showHelpOnStart)
    }

    val status = ui.status
    val isPlaying = status is GameStatus.Playing
    val isPaused = status is GameStatus.Paused
    val isOver = status is GameStatus.GameOver
    val frame = rememberGameFrame(session = session, running = isPlaying && !helpVisible)

    var scoreResult by remember(session) { mutableStateOf<GameScoreResult?>(null) }
    LaunchedEffect(session, status) {
        if (status is GameStatus.GameOver && scoreResult == null) {
            scoreResult = runCatching {
                // The shared score screen calls the second column "lines". Asteroids
                // has no lines, so it stores the reached wave as the level instead.
                recordCallback(status.score, 0, session.engine.wave)
            }.getOrNull()
        }
    }

    LaunchedEffect(sfxEnabled) {
        audio.setSfxEnabled(sfxEnabled)
    }

    LaunchedEffect(session, status, musicEnabled, helpVisible) {
        if (musicEnabled && status is GameStatus.Playing && !helpVisible) {
            audio.playMusic(AsteroidsMusic.track(session.seed))
        } else {
            audio.stopMusic()
        }
    }

    DisposableEffect(session) {
        onDispose { audio.stopMusic() }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        audio.stopMusic()
        if (!helpVisible && session.state.value is GameStatus.Playing) {
            session.onInput(InputEvent.TogglePause)
        }
    }

    LaunchedEffect(session) {
        session.events.collect { event ->
            if (sfxState) {
                sfxFor(event)?.let(audio::play)
            }
            when (event) {
                is AsteroidsEvent.ShipHit,
                is AsteroidsEvent.GameOver,
                is AsteroidsEvent.ExtraLife,
                is AsteroidsEvent.SaucerDestroyed,
                -> hapticsState.perform(HapticIntensity.STRONG)

                is AsteroidsEvent.AsteroidDestroyed -> hapticsState.perform(
                    if (event.size == AsteroidSize.LARGE) {
                        HapticIntensity.STRONG
                    } else {
                        HapticIntensity.LIGHT
                    },
                )

                is AsteroidsEvent.WaveStarted -> hapticsState.perform(HapticIntensity.LIGHT)

                AsteroidsEvent.Shot,
                AsteroidsEvent.ThrustStarted,
                AsteroidsEvent.SaucerAppeared,
                -> Unit
            }
        }
    }

    fun send(event: InputEvent) {
        if (event is InputEvent.TogglePause && session.state.value !is GameStatus.GameOver) {
            audio.play(AsteroidsSfx.pause)
        }
        session.onInput(event)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RetroColors.Background),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding(),
        ) {
            AsteroidsTopBar(
                score = ui.score,
                onExit = onExit,
                onPause = { send(InputEvent.TogglePause) },
            )
            AsteroidsHud(
                lives = ui.lives,
                wave = ui.wave,
                bestScore = bestScore,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .border(1.dp, RetroColors.Grid.copy(alpha = 0.8f), RoundedCornerShape(3.dp)),
            ) {
                GameCanvas(
                    session = session,
                    frame = frame,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("asteroids_board")
                        .then(
                            if (ui.settings.controlScheme == ControlScheme.TAP) {
                                Modifier.asteroidsTouchInput(::send)
                            } else {
                                Modifier
                            },
                        ),
                )
            }

            if (ui.settings.controlScheme == ControlScheme.BUTTONS) {
                AsteroidsControlPad(onInput = ::send)
            } else {
                Text(
                    text = "GAUCHE / DROITE : TOURNER  ·  BAS : POUSSER  ·  HAUT : TIRER",
                    style = MaterialTheme.typography.labelSmall,
                    color = RetroColors.TextDim,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
        }

        when {
            helpVisible -> HelpOverlay(
                onDismiss = {
                    helpVisible = false
                    onHelpDismissed()
                },
            )

            isPaused -> PauseOverlay(
                onResume = { send(InputEvent.TogglePause) },
                onExit = onExit,
            )

            isOver -> GameOverOverlay(
                score = ui.score,
                wave = ui.wave,
                bestScore = bestScore,
                result = scoreResult,
                onReplay = onReplay,
                onExit = onExit,
                onOpenPaywall = onOpenPaywall,
            )
        }
    }
}

@Composable
private fun AsteroidsTopBar(
    score: Int,
    onExit: () -> Unit,
    onPause: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onExit,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Retour au hub",
                tint = RetroColors.TextMuted,
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = "ASTÉROÏDES",
                style = MaterialTheme.typography.titleLarge,
                color = RetroColors.Amber,
            )
            Text(
                text = "CHAMP DE DÉBRIS",
                style = MaterialTheme.typography.labelSmall,
                color = RetroColors.TextMuted,
            )
        }
        Text(
            text = formatScore(score),
            style = MaterialTheme.typography.headlineLarge,
            color = RetroColors.TextPrimary,
            modifier = Modifier
                .padding(end = 4.dp)
                .testTag("asteroids_score"),
        )
        IconButton(
            onClick = onPause,
            modifier = Modifier
                .size(40.dp)
                .testTag("asteroids_pause"),
        ) {
            Icon(
                imageVector = Icons.Default.Pause,
                contentDescription = "Pause",
                tint = RetroColors.NeonCyan,
            )
        }
    }
}

@Composable
private fun AsteroidsHud(
    lives: Int,
    wave: Int,
    bestScore: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        LivesRow(lives)
        Text(
            text = "VAGUE $wave",
            style = MaterialTheme.typography.labelLarge,
            color = RetroColors.NeonCyan,
            modifier = Modifier.testTag("asteroids_wave"),
        )
        Text(
            text = "RECORD ${formatScore(bestScore)}",
            style = MaterialTheme.typography.labelSmall,
            color = RetroColors.TextMuted,
        )
    }
}

@Composable
private fun LivesRow(lives: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.testTag("asteroids_lives"),
    ) {
        Text(
            text = "VIES",
            style = MaterialTheme.typography.labelSmall,
            color = RetroColors.TextDim,
        )
        Spacer(Modifier.width(6.dp))
        repeat(lives.coerceAtLeast(0)) {
            ShipLifeIcon()
            Spacer(Modifier.width(3.dp))
        }
    }
}

@Composable
private fun ShipLifeIcon() {
    Canvas(Modifier.size(16.dp)) {
        val stroke = (size.minDimension * 0.12f).coerceAtLeast(1.4f)
        val color = RetroColors.Amber
        val nose = Offset(size.width * 0.5f, size.height * 0.08f)
        val left = Offset(size.width * 0.1f, size.height * 0.9f)
        val right = Offset(size.width * 0.9f, size.height * 0.9f)
        val tail = Offset(size.width * 0.5f, size.height * 0.62f)
        drawLine(color, nose, left, stroke)
        drawLine(color, left, tail, stroke)
        drawLine(color, tail, right, stroke)
        drawLine(color, right, nose, stroke)
    }
}

@Composable
private fun AsteroidsControlPad(onInput: (InputEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            HoldKey(
                label = "↺",
                onPress = { onInput(InputEvent.HorizontalHold(-1, true)) },
                onRelease = { onInput(InputEvent.HorizontalHold(-1, false)) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("asteroids_left"),
            )
            ControlKey(
                label = "TIR",
                onClick = { onInput(InputEvent.HardDrop) },
                modifier = Modifier
                    .weight(1.4f)
                    .testTag("asteroids_fire"),
            )
            HoldKey(
                label = "↻",
                onPress = { onInput(InputEvent.HorizontalHold(1, true)) },
                onRelease = { onInput(InputEvent.HorizontalHold(1, false)) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("asteroids_right"),
            )
        }
        HoldKey(
            label = "POUSSER",
            onPress = { onInput(InputEvent.SoftDrop(true)) },
            onRelease = { onInput(InputEvent.SoftDrop(false)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("asteroids_thrust"),
        )
    }
}

@Composable
private fun ControlKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(46.dp)
            .background(RetroColors.Surface, RoundedCornerShape(3.dp))
            .border(1.dp, RetroColors.NeonCyan.copy(alpha = 0.55f), RoundedCornerShape(3.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = RetroColors.NeonCyan,
        )
    }
}

@Composable
private fun HoldKey(
    label: String,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(46.dp)
            .background(RetroColors.Surface, RoundedCornerShape(3.dp))
            .border(1.dp, RetroColors.NeonCyan.copy(alpha = 0.55f), RoundedCornerShape(3.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPress()
                        tryAwaitRelease()
                        onRelease()
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = RetroColors.NeonCyan,
        )
    }
}

@Composable
private fun HelpOverlay(onDismiss: () -> Unit) {
    OverlayScrim(tag = "asteroids_help") {
        NeonPanel(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .wrapContentHeight(),
            accent = RetroColors.Amber,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "COMMENT JOUER",
                    style = MaterialTheme.typography.headlineLarge,
                    color = RetroColors.Amber,
                )
                Text(
                    text = "Le vaisseau conserve son élan. Anticipez, tournez et tirez.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RetroColors.TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 14.dp),
                )
                HelpLine("◀ ▶", "Zone gauche / droite : tourner")
                HelpLine("▼", "Zone basse : pousser")
                HelpLine("▲", "Zone haute : tirer")
                Text(
                    text = "Avec le pavé de boutons, les commandes sont sous le terrain.",
                    style = MaterialTheme.typography.labelSmall,
                    color = RetroColors.TextDim,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 14.dp),
                )
                RetroButton(
                    label = "J'AI COMPRIS",
                    onClick = onDismiss,
                    accent = RetroColors.Amber,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("asteroids_help_dismiss"),
                )
            }
        }
    }
}

@Composable
private fun HelpLine(symbol: String, label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.labelLarge,
            color = RetroColors.NeonCyan,
            modifier = Modifier.width(42.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = RetroColors.TextPrimary,
        )
    }
}

@Composable
private fun PauseOverlay(
    onResume: () -> Unit,
    onExit: () -> Unit,
) {
    OverlayScrim(tag = "asteroids_pause_overlay") {
        NeonPanel(
            modifier = Modifier.widthIn(max = 300.dp),
            accent = RetroColors.NeonCyan,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "PAUSE",
                    style = MaterialTheme.typography.displayMedium,
                    color = RetroColors.NeonCyan,
                )
                Text(
                    text = "La partie est en attente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RetroColors.TextMuted,
                    modifier = Modifier.padding(top = 4.dp, bottom = 18.dp),
                )
                RetroButton(
                    label = "Reprendre",
                    onClick = onResume,
                    icon = Icons.Default.PlayArrow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("asteroids_resume"),
                )
                Spacer(Modifier.height(8.dp))
                RetroButton(
                    label = "Menu",
                    onClick = onExit,
                    accent = RetroColors.TextMuted,
                    icon = Icons.Default.Home,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun GameOverOverlay(
    score: Int,
    wave: Int,
    bestScore: Int,
    result: GameScoreResult?,
    onReplay: () -> Unit,
    onExit: () -> Unit,
    onOpenPaywall: () -> Unit,
) {
    OverlayScrim(tag = "asteroids_game_over") {
        NeonPanel(
            modifier = Modifier
                .widthIn(max = 330.dp)
                .wrapContentHeight(),
            accent = RetroColors.NeonMagenta,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "FIN DE PARTIE",
                    style = MaterialTheme.typography.headlineLarge,
                    color = RetroColors.NeonMagenta,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "SCORE",
                    style = MaterialTheme.typography.labelSmall,
                    color = RetroColors.TextDim,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Text(
                    text = formatScore(score),
                    style = MaterialTheme.typography.displayMedium,
                    color = RetroColors.NeonGreen,
                    modifier = Modifier.testTag("asteroids_final_score"),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    FinalStat("VAGUE", wave.toString())
                    FinalStat("RECORD", formatScore(maxOf(bestScore, result?.best ?: 0)))
                }
                if (result?.isNewBest == true) {
                    Text(
                        text = "NOUVEAU RECORD  ·  #${result.rank ?: 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = RetroColors.Amber,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                } else if (result?.rank != null) {
                    Text(
                        text = "TOP 10  ·  #${result.rank}",
                        style = MaterialTheme.typography.labelSmall,
                        color = RetroColors.TextMuted,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
                RetroButton(
                    label = "Rejouer",
                    onClick = onReplay,
                    icon = Icons.Default.Replay,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                RetroButton(
                    label = "Menu",
                    onClick = onExit,
                    accent = RetroColors.TextMuted,
                    icon = Icons.Default.Home,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "SANS PUBLICITÉ →",
                    style = MaterialTheme.typography.labelSmall,
                    color = RetroColors.TextDim,
                    modifier = Modifier
                        .padding(top = 18.dp)
                        .clickable(onClick = onOpenPaywall),
                )
            }
        }
    }
}

@Composable
private fun FinalStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = RetroColors.TextDim,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = RetroColors.TextPrimary,
        )
    }
}

@Composable
private fun OverlayScrim(
    tag: String,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f))
            .pointerInput(Unit) { detectTapGestures { } }
            .testTag(tag),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

private fun sfxFor(event: AsteroidsEvent): Sfx? = when (event) {
    AsteroidsEvent.Shot -> AsteroidsSfx.shoot
    AsteroidsEvent.ThrustStarted -> AsteroidsSfx.thrust
    AsteroidsEvent.SaucerAppeared -> AsteroidsSfx.saucerAppeared
    is AsteroidsEvent.AsteroidDestroyed -> when (event.size) {
        AsteroidSize.LARGE -> AsteroidsSfx.explosionLarge
        AsteroidSize.MEDIUM -> AsteroidsSfx.explosionMedium
        AsteroidSize.SMALL -> AsteroidsSfx.explosionSmall
    }

    is AsteroidsEvent.SaucerDestroyed -> AsteroidsSfx.saucerDestroyed
    is AsteroidsEvent.ShipHit -> AsteroidsSfx.shipHit
    AsteroidsEvent.ExtraLife -> AsteroidsSfx.extraLife
    is AsteroidsEvent.WaveStarted -> AsteroidsSfx.wave
    is AsteroidsEvent.GameOver -> AsteroidsSfx.gameOver
}

private val scoreFormat = NumberFormat.getIntegerInstance(Locale.FRANCE)

private fun formatScore(score: Int): String =
    scoreFormat.format(score).padStart(6, '0')

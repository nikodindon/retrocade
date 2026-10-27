package com.nikodindon.retrocade.games.tetris.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.nikodindon.retrocade.core.ui.PixelDivider
import com.nikodindon.retrocade.core.ui.RetroColors
import com.nikodindon.retrocade.core.ui.RetroButton
import com.nikodindon.retrocade.core.ui.rememberHapticsController
import com.nikodindon.retrocade.games.api.GameScoreResult
import com.nikodindon.retrocade.games.api.GameStatus
import com.nikodindon.retrocade.games.api.InputEvent
import com.nikodindon.retrocade.games.tetris.TetrisSession
import com.nikodindon.retrocade.games.tetris.audio.TetrisMusic
import com.nikodindon.retrocade.games.tetris.audio.TetrisSfx
import com.nikodindon.retrocade.games.tetris.logic.PieceType
import com.nikodindon.retrocade.games.tetris.logic.TetrisEvent
import com.nikodindon.retrocade.games.tetris.procedural.TetrisPalette
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.min

@Composable
fun TetrisScreen(
    session: TetrisSession,
    audio: AudioController,
    bestScore: Int,
    sfxEnabled: Boolean,
    musicEnabled: Boolean,
    showHoldButton: Boolean,
    onExit: () -> Unit,
    onReplay: () -> Unit,
    onOpenPaywall: () -> Unit,
    onRecordScore: suspend (score: Int, lines: Int, level: Int) -> GameScoreResult,
    modifier: Modifier = Modifier,
) {
    val ui by session.uiState.collectAsStateWithLifecycle()
    val haptics = rememberHapticsController(ui.settings.hapticsEnabled)
    val hapticsState by rememberUpdatedState(haptics)
    val density = LocalDensity.current
    val sfxState by rememberUpdatedState(sfxEnabled)
    val musicState by rememberUpdatedState(musicEnabled)
    val recordCallback by rememberUpdatedState(onRecordScore)

    val status = ui.status
    val isPlaying = status is GameStatus.Playing
    val isPaused = status is GameStatus.Paused
    val isOver = status is GameStatus.GameOver
    val frame = rememberGameFrame(session = session, running = isPlaying)

    var scoreResult by remember(session) { mutableStateOf<GameScoreResult?>(null) }
    LaunchedEffect(session, status) {
        if (status is GameStatus.GameOver && scoreResult == null) {
            scoreResult = runCatching {
                recordCallback(status.score, ui.lines, ui.level)
            }.getOrNull()
        }
    }

    LaunchedEffect(sfxEnabled) {
        audio.setSfxEnabled(sfxEnabled)
    }

    LaunchedEffect(session, status, musicEnabled) {
        if (musicEnabled && status is GameStatus.Playing) {
            audio.playMusic(TetrisMusic.track(session.seed))
        } else {
            audio.stopMusic()
        }
    }

    DisposableEffect(session) {
        onDispose { audio.stopMusic() }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        audio.stopMusic()
        if (session.state.value is GameStatus.Playing) {
            session.onInput(InputEvent.TogglePause)
        }
    }

    LaunchedEffect(session) {
        session.events.collect { event ->
            if (sfxState) {
                sfxFor(event)?.let(audio::play)
            }
            when (event) {
                is TetrisEvent.HardDropped,
                is TetrisEvent.GameOver,
                is TetrisEvent.LineCleared,
                -> hapticsState.perform(HapticIntensity.STRONG)

                is TetrisEvent.Rotated,
                is TetrisEvent.PieceLocked,
                -> hapticsState.perform(HapticIntensity.LIGHT)

                else -> Unit
            }
        }
    }

    fun send(event: InputEvent) {
        if (event is InputEvent.TogglePause && session.state.value !is GameStatus.GameOver) {
            audio.play(TetrisSfx.pause)
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
            TetrisTopBar(
                level = ui.level,
                bestScore = bestScore,
                score = ui.score,
                onExit = onExit,
                onPause = { send(InputEvent.TogglePause) },
            )
            TetrisStats(ui.score, bestScore, ui.lines, ui.level, ui.combo, ui.backToBack)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                HoldColumn(
                    piece = ui.hold,
                    canHold = ui.canHold,
                    palette = session.palette,
                    showHoldButton = showHoldButton,
                    onHold = { send(InputEvent.HoldPiece) },
                )

                GameCanvas(
                    session = session,
                    frame = frame,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("tetris_board")
                        .then(
                            if (ui.settings.controlScheme == ControlScheme.TAP) {
                                Modifier.tetrisTouchInput(
                                    thresholdPx = with(density) {
                                        ui.settings.swipeThresholdDp.dp.toPx()
                                    },
                                    onInput = ::send,
                                )
                            } else {
                                Modifier
                            },
                        ),
                )

                NextColumn(
                    next = ui.next,
                    palette = session.palette,
                )
            }

            if (ui.settings.controlScheme == ControlScheme.BUTTONS) {
                ControlPad(
                    canHold = ui.canHold,
                    showHoldButton = showHoldButton,
                    onInput = ::send,
                )
            } else {
                Text(
                    text = "TAP CENTRE : TOURNER  ·  GLISSE ↑ : DROP  ·  ↓ : DESCENDRE",
                    style = MaterialTheme.typography.labelSmall,
                    color = RetroColors.TextDim,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
        }

        if (isPaused) {
            PauseOverlay(
                onResume = { send(InputEvent.TogglePause) },
                onExit = onExit,
            )
        }
        if (isOver) {
            GameOverOverlay(
                score = ui.score,
                lines = ui.lines,
                level = ui.level,
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
private fun TetrisTopBar(
    level: Int,
    bestScore: Int,
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
                contentDescription = "Retour au menu",
                tint = RetroColors.TextMuted,
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = "TETRIS",
                style = MaterialTheme.typography.titleLarge,
                color = RetroColors.NeonGreen,
            )
            Text(
                text = "NIVEAU $level  ·  BEST ${formatScore(bestScore)}",
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
                .testTag("tetris_score"),
        )
        IconButton(
            onClick = onPause,
            modifier = Modifier
                .size(40.dp)
                .testTag("tetris_pause"),
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
private fun TetrisStats(
    score: Int,
    bestScore: Int,
    lines: Int,
    level: Int,
    combo: Int,
    backToBack: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StatChip("SCORE", formatScore(score), RetroColors.NeonGreen, Modifier.weight(1.3f))
        StatChip("LIGNES", lines.toString().padStart(2, '0'), RetroColors.NeonCyan, Modifier.weight(1f))
        StatChip("BEST", formatScore(bestScore), RetroColors.TextMuted, Modifier.weight(1.3f))
        if (combo > 0 || backToBack) {
            StatChip(
                label = if (backToBack) "B2B" else "COMBO",
                value = if (backToBack) "x1.5" else "x$combo",
                accent = RetroColors.Amber,
                modifier = Modifier.weight(0.9f),
            )
        } else {
            StatChip("NIVEAU", level.toString().padStart(2, '0'), RetroColors.Amber, Modifier.weight(0.9f))
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(RetroColors.Surface.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
            .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(2.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = RetroColors.TextDim,
            maxLines = 1,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = accent,
            maxLines = 1,
        )
    }
}

@Composable
private fun HoldColumn(
    piece: PieceType?,
    canHold: Boolean,
    palette: TetrisPalette,
    showHoldButton: Boolean,
    onHold: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(62.dp)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "RÉSERVE",
            style = MaterialTheme.typography.labelSmall,
            color = RetroColors.TextDim,
            modifier = Modifier.padding(top = 4.dp),
        )
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(54.dp, 44.dp)
                .background(RetroColors.Surface.copy(alpha = 0.55f), RoundedCornerShape(2.dp)),
            contentAlignment = Alignment.Center,
        ) {
            PiecePreview(
                piece = piece,
                palette = palette,
                alpha = if (canHold) 1f else 0.35f,
                modifier = Modifier.fillMaxSize().padding(5.dp),
            )
        }
        if (showHoldButton) {
            Spacer(Modifier.height(8.dp))
            ControlKey(
                label = "HOLD",
                enabled = canHold,
                onClick = onHold,
                modifier = Modifier
                    .width(54.dp)
                    .height(32.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        PixelDivider(Modifier.width(44.dp))
        Text(
            text = "SEED\n" + palette.seedLabel,
            style = MaterialTheme.typography.labelSmall,
            color = RetroColors.TextDim,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun NextColumn(
    next: List<PieceType>,
    palette: TetrisPalette,
) {
    Column(
        modifier = Modifier
            .width(62.dp)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "SUIVANT",
            style = MaterialTheme.typography.labelSmall,
            color = RetroColors.TextDim,
            modifier = Modifier.padding(top = 4.dp),
        )
        next.take(3).forEach { piece ->
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(54.dp, 40.dp)
                    .background(RetroColors.Surface.copy(alpha = 0.55f), RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center,
            ) {
                PiecePreview(
                    piece = piece,
                    palette = palette,
                    modifier = Modifier.fillMaxSize().padding(5.dp),
                )
            }
        }
    }
}

@Composable
private fun PiecePreview(
    piece: PieceType?,
    palette: TetrisPalette,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
) {
    Canvas(modifier) {
        if (piece == null) return@Canvas
        val cells = piece.cells(0)
        val minX = cells.minOf { it.x }
        val maxX = cells.maxOf { it.x }
        val minY = cells.minOf { it.y }
        val maxY = cells.maxOf { it.y }
        val columns = (maxX - minX + 1).coerceAtLeast(1)
        val rows = (maxY - minY + 1).coerceAtLeast(1)
        val cell = min(size.width / (columns + 0.5f), size.height / (rows + 0.5f))
        val startX = (size.width - columns * cell) / 2f
        val startY = (size.height - rows * cell) / 2f
        cells.forEach { offset ->
            val x = startX + (offset.x - minX) * cell
            val y = startY + (offset.y - minY) * cell
            val inset = cell * 0.08f
            drawRect(
                color = palette.piece(piece.colorIndex).copy(alpha = alpha),
                topLeft = Offset(x + inset, y + inset),
                size = Size(cell - inset * 2f, cell - inset * 2f),
            )
            drawRect(
                color = Color.White.copy(alpha = 0.22f * alpha),
                topLeft = Offset(x + inset, y + inset),
                size = Size(cell - inset * 2f, cell * 0.12f),
            )
        }
    }
}

@Composable
private fun ControlPad(
    canHold: Boolean,
    showHoldButton: Boolean,
    onInput: (InputEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            HoldKey(
                label = "◀",
                onPress = { onInput(InputEvent.HorizontalHold(-1, true)) },
                onRelease = { onInput(InputEvent.HorizontalHold(-1, false)) },
                modifier = Modifier.weight(1f),
            )
            ControlKey(
                label = "↻",
                onClick = { onInput(InputEvent.RotateClockwise) },
                modifier = Modifier.weight(1.2f),
            )
            HoldKey(
                label = "▶",
                onPress = { onInput(InputEvent.HorizontalHold(1, true)) },
                onRelease = { onInput(InputEvent.HorizontalHold(1, false)) },
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            HoldKey(
                label = "▼",
                onPress = { onInput(InputEvent.SoftDrop(true)) },
                onRelease = { onInput(InputEvent.SoftDrop(false)) },
                modifier = Modifier.weight(1.3f),
            )
            ControlKey(
                label = "DROP",
                onClick = { onInput(InputEvent.HardDrop) },
                modifier = Modifier.weight(1.3f),
            )
            if (showHoldButton) {
                ControlKey(
                    label = "HOLD",
                    enabled = canHold,
                    onClick = { onInput(InputEvent.HoldPiece) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ControlKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .height(46.dp)
            .background(
                if (enabled) RetroColors.Surface else RetroColors.Surface.copy(alpha = 0.4f),
                RoundedCornerShape(3.dp),
            )
            .border(
                1.dp,
                if (enabled) RetroColors.NeonCyan.copy(alpha = 0.55f) else RetroColors.TextDim,
                RoundedCornerShape(3.dp),
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) RetroColors.NeonCyan else RetroColors.TextDim,
        )
    }
}

@Composable
private fun HoldKey(
    label: String,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .height(46.dp)
            .background(RetroColors.Surface, RoundedCornerShape(3.dp))
            .border(1.dp, RetroColors.NeonCyan.copy(alpha = 0.55f), RoundedCornerShape(3.dp))
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
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
private fun PauseOverlay(
    onResume: () -> Unit,
    onExit: () -> Unit,
) {
    OverlayScrim(tag = "tetris_pause_overlay") {
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
            }
        }
    }
}

@Composable
private fun GameOverOverlay(
    score: Int,
    lines: Int,
    level: Int,
    bestScore: Int,
    result: GameScoreResult?,
    onReplay: () -> Unit,
    onExit: () -> Unit,
    onOpenPaywall: () -> Unit,
) {
    OverlayScrim(tag = "tetris_game_over") {
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
                GlitchTitle()
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
                    modifier = Modifier.testTag("tetris_final_score"),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    FinalStat("LIGNES", lines.toString())
                    FinalStat("NIVEAU", level.toString())
                    FinalStat("BEST", formatScore(maxOf(bestScore, result?.best ?: 0)))
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
private fun GlitchTitle() {
    val transition = rememberInfiniteTransition(label = "glitch")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "glitch-phase",
    )
    val direction = if ((phase * 12).toInt() % 2 == 0) 1f else -1f
    val dx = 3f * direction
    val dy = 1.5f * direction
    Box(contentAlignment = Alignment.Center) {
        Text(
            text = "GAME OVER",
            style = MaterialTheme.typography.headlineLarge,
            color = RetroColors.NeonMagenta.copy(alpha = 0.55f),
            modifier = Modifier.graphicsLayer {
                translationX = dx
                translationY = -dy
            },
        )
        Text(
            text = "GAME OVER",
            style = MaterialTheme.typography.headlineLarge,
            color = RetroColors.NeonCyan.copy(alpha = 0.55f),
            modifier = Modifier.graphicsLayer {
                translationX = -dx
                translationY = dy
            },
        )
        Text(
            text = "GAME OVER",
            style = MaterialTheme.typography.headlineLarge,
            color = RetroColors.TextPrimary,
            fontWeight = FontWeight.Bold,
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

private fun sfxFor(event: TetrisEvent): Sfx? = when (event) {
    is TetrisEvent.Moved -> TetrisSfx.move
    is TetrisEvent.Rotated -> TetrisSfx.rotate
    is TetrisEvent.PieceLocked -> TetrisSfx.lock
    is TetrisEvent.HardDropped -> TetrisSfx.hardDrop
    is TetrisEvent.LineCleared -> if (event.lines == 4) TetrisSfx.tetris else TetrisSfx.lineClear
    is TetrisEvent.LevelUp -> TetrisSfx.levelUp
    is TetrisEvent.GameOver -> TetrisSfx.gameOver
    is TetrisEvent.HoldUsed -> TetrisSfx.rotate
}

private val scoreFormat = NumberFormat.getIntegerInstance(Locale.FRANCE)

private fun formatScore(score: Int): String =
    scoreFormat.format(score).padStart(6, '0')

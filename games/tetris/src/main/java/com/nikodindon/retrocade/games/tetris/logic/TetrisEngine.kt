package com.nikodindon.retrocade.games.tetris.logic

import com.nikodindon.retrocade.core.common.Rng
/**
 * Pure simulation of a Tetris game. It owns no Compose or Android types and never
 * touches a clock: callers drive it with fixed-ish deltas and observe state/events.
 */
class TetrisEngine(seed: Long) {
    val board: Board = Board()
    val tracker: ScoreTracker = ScoreTracker()

    private val bag = PieceBag(Rng(seed))
    private val upcoming = ArrayDeque<PieceType>(8)

    var current: Tetromino? = null
        private set

    var holdPiece: PieceType? = null
        private set

    var canHold: Boolean = true
        private set

    var gameOver: Boolean = false
        private set

    var eventSink: ((TetrisEvent) -> Unit)? = null

    private var gravityAccumulator = 0.0
    private var softDropAccumulator = 0.0
    private var softDropHeld = false
    private var lockTimer = 0L
    private var lockResets = 0

    private var leftHeld = false
    private var rightHeld = false
    private var heldDirection = 0
    private var dasTimer = 0L
    private var arrTimer = 0L
    private var dasTriggered = false

    init {
        ensureUpcoming()
        spawnNext()
    }

    val preview: List<PieceType>
        get() {
            ensureUpcoming()
            return upcoming.take(TetrisTiming.PREVIEW_COUNT)
        }

    fun update(dtMillis: Long) {
        if (gameOver) return
        val piece = current ?: return
        val dt = dtMillis.coerceAtLeast(0L)
        if (dt == 0L) return

        updateAutoRepeat(dt)

        if (softDropHeld) {
            softDropAccumulator += dt
            while (softDropAccumulator >= TetrisTiming.SOFT_DROP_INTERVAL_MS) {
                softDropAccumulator -= TetrisTiming.SOFT_DROP_INTERVAL_MS
                if (!stepDown(score = true)) break
            }
        } else if (!board.grounded(current ?: piece)) {
            gravityAccumulator += dt
            val interval = TetrisTiming.gravityIntervalMs(tracker.level)
            while (gravityAccumulator >= interval) {
                gravityAccumulator -= interval
                if (!stepDown(score = false)) {
                    gravityAccumulator = 0.0
                    break
                }
            }
        }

        val active = current ?: return
        if (board.grounded(active)) {
            lockTimer += dt
            if (lockTimer >= TetrisTiming.LOCK_DELAY_MS) lockPiece()
        } else {
            lockTimer = 0
        }
    }

    fun onHorizontalHold(direction: Int, pressed: Boolean) {
        if (direction != -1 && direction != 1) return
        if (gameOver) return
        if (pressed) {
            if (direction == -1) leftHeld = true else rightHeld = true
            heldDirection = direction
            dasTimer = 0
            arrTimer = 0
            dasTriggered = false
            moveHorizontal(direction)
        } else {
            if (direction == -1) leftHeld = false else rightHeld = false
            if (heldDirection == direction) {
                heldDirection = when {
                    leftHeld -> -1
                    rightHeld -> 1
                    else -> 0
                }
                dasTimer = 0
                arrTimer = 0
                dasTriggered = false
            }
        }
    }

    fun moveHorizontal(direction: Int): Boolean {
        val piece = current ?: return false
        val candidate = piece.moved(dx = direction)
        if (board.collides(candidate)) return false
        current = candidate
        registerPlayerAction()
        emit(TetrisEvent.Moved(direction))
        return true
    }

    fun rotate(direction: Int): Boolean {
        val piece = current ?: return false
        val from = piece.rotation
        val target = Math.floorMod(from + direction, 4)
        val type = piece.type
        for (kick in SrsKicks.candidates(type, from, target)) {
            val (dx, dy) = SrsKicks.offset(kick)
            val candidate = piece.copy(x = piece.x + dx, y = piece.y + dy, rotation = target)
            if (!board.collides(candidate)) {
                current = candidate
                registerPlayerAction()
                emit(TetrisEvent.Rotated)
                return true
            }
        }
        return false
    }

    fun setSoftDrop(pressed: Boolean) {
        softDropHeld = pressed
        if (!pressed) softDropAccumulator = 0.0
    }

    fun hardDrop() {
        val piece = current ?: return
        var distance = 0
        var candidate = piece
        while (!board.collides(candidate.moved(dy = 1))) {
            candidate = candidate.moved(dy = 1)
            distance += 1
        }
        current = candidate
        tracker.addHardDrop(distance)
        emit(TetrisEvent.HardDropped(distance))
        lockPiece()
    }

    fun hold() {
        val piece = current ?: return
        if (!canHold || gameOver) return
        val previousHold = holdPiece
        holdPiece = piece.type
        canHold = false
        emit(TetrisEvent.HoldUsed)
        if (previousHold == null) {
            spawnNext()
        } else {
            spawn(previousHold)
        }
    }

    private fun updateAutoRepeat(dt: Long) {
        if (heldDirection == 0) return
        dasTimer += dt
        if (dasTimer < TetrisTiming.DAS_MS) return
        if (!dasTriggered) {
            dasTriggered = true
            arrTimer = 0
            moveHorizontal(heldDirection)
            return
        }
        arrTimer += dt
        while (arrTimer >= TetrisTiming.ARR_MS) {
            arrTimer -= TetrisTiming.ARR_MS
            if (!moveHorizontal(heldDirection)) break
        }
    }

    private fun stepDown(score: Boolean): Boolean {
        val piece = current ?: return false
        val candidate = piece.moved(dy = 1)
        if (board.collides(candidate)) return false
        current = candidate
        if (score) tracker.addSoftDrop(1)
        return true
    }

    private fun registerPlayerAction() {
        val piece = current ?: return
        if (board.grounded(piece)) {
            if (lockResets < TetrisTiming.MAX_LOCK_RESETS) {
                lockTimer = 0
                lockResets += 1
            }
        } else {
            lockTimer = 0
        }
    }

    private fun lockPiece() {
        val piece = current ?: return
        board.merge(piece)
        emit(TetrisEvent.PieceLocked)
        val rows = board.fullRows()
        val outcome = tracker.registerLineClear(rows.size)
        if (rows.isNotEmpty()) {
            board.clearRows(rows)
            emit(
                TetrisEvent.LineCleared(
                    lines = rows.size,
                    rows = rows,
                    combo = outcome.combo,
                    backToBack = outcome.backToBackApplied,
                ),
            )
            if (outcome.levelAfter > outcome.levelBefore) {
                emit(TetrisEvent.LevelUp(outcome.levelAfter))
            }
        }
        canHold = true
        current = null
        spawnNext()
    }

    private fun spawnNext() {
        ensureUpcoming()
        val type = upcoming.removeFirst()
        ensureUpcoming()
        spawn(type)
    }

    private fun spawn(type: PieceType) {
        val piece = Tetromino.spawn(type)
        gravityAccumulator = 0.0
        softDropAccumulator = 0.0
        lockTimer = 0
        lockResets = 0
        if (board.collides(piece)) {
            current = null
            gameOver = true
            emit(TetrisEvent.GameOver(tracker.score))
        } else {
            current = piece
        }
    }

    private fun ensureUpcoming() {
        while (upcoming.size < TetrisTiming.PREVIEW_COUNT + 1) {
            upcoming.addLast(bag.next())
        }
    }

    private fun emit(event: TetrisEvent) {
        eventSink?.invoke(event)
    }

    internal fun forcePiece(type: PieceType, rotation: Int = 0, x: Int, y: Int) {
        current = Tetromino(type, rotation, x, y)
        gravityAccumulator = 0.0
        softDropAccumulator = 0.0
        lockTimer = 0
        lockResets = 0
        gameOver = false
    }

    internal fun forceSpawn(type: PieceType) {
        spawn(type)
    }

    internal fun remainingLockResets(): Int = lockResets

    companion object {
        fun previewForSeed(seed: Long, count: Int): List<PieceType> {
            val bag = PieceBag(Rng(seed))
            return List(count) { bag.next() }
        }
    }
}

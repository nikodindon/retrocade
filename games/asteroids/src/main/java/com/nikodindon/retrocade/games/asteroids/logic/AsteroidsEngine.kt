package com.nikodindon.retrocade.games.asteroids.logic

import com.nikodindon.retrocade.core.common.Rng
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

const val STARTING_LIVES = 3
const val MAX_LIVES = 5
const val EXTRA_LIFE_SCORE = 10_000
const val BASE_ASTEROIDS_PER_WAVE = 4
const val ASTEROIDS_PER_ADDITIONAL_WAVE = 2
const val MAX_ASTEROIDS_PER_WAVE = 20
const val WAVE_DELAY_MS = 900L

const val SHIP_RADIUS = 12f
const val SAUCER_RADIUS = 25f
const val BULLET_RADIUS = 3f

private const val TWO_PI = (PI * 2.0).toFloat()
private const val SHIP_TURN_RATE = 3.4f
private const val SHIP_THRUST = 280f
private const val SHIP_FRICTION_PER_SECOND = 1.5f
private const val SHIP_MAX_SPEED = 330f
private const val SHIP_INVULNERABILITY_MS = 2_200f
private const val FIRE_INTERVAL_MS = 170f
private const val BULLET_SPEED = 560f
private const val BULLET_LIFE_MS = 900f
private const val SAUCER_BULLET_SPEED = 300f
private const val SAUCER_BULLET_LIFE_MS = 1_600f

/** Number of large asteroids spawned at the start of [wave]. */
fun asteroidsForWave(wave: Int): Int =
    (BASE_ASTEROIDS_PER_WAVE + (wave.coerceAtLeast(1) - 1) * ASTEROIDS_PER_ADDITIONAL_WAVE)
        .coerceAtMost(MAX_ASTEROIDS_PER_WAVE)

/** Saucers appear more often as the wave increases. */
fun saucerSpawnIntervalMs(wave: Int): Long =
    (9_000L - wave.coerceAtLeast(1) * 500L).coerceAtLeast(3_200L)

/**
 * Deterministic Asteroids simulation.
 *
 * All random choices come from the seeded [Rng]; the engine has no clock,
 * Android imports or rendering concerns. Callers provide fixed milestones.
 */
class AsteroidsEngine(
    seed: Long,
    startingWave: Int = 1,
) {
    private val rng = Rng(seed)
    private var nextEntityId = 0

    val ship = Ship(
        x = WORLD_WIDTH / 2f,
        y = WORLD_HEIGHT / 2f,
        vx = 0f,
        vy = 0f,
        angle = -PI.toFloat() / 2f,
        invulnerabilityMs = SHIP_INVULNERABILITY_MS,
        alive = true,
    )

    val asteroids: MutableList<Asteroid> = mutableListOf()
    val bullets: MutableList<Bullet> = mutableListOf()

    var saucer: Saucer? = null
        private set

    var score: Int = 0
        private set

    var lives: Int = STARTING_LIVES
        private set

    var wave: Int = startingWave.coerceAtLeast(1)
        private set

    var gameOver: Boolean = false
        private set

    var elapsedMs: Long = 0L
        private set

    var eventSink: ((AsteroidsEvent) -> Unit)? = null

    val isThrusting: Boolean get() = thrusting

    private var fireCooldownMs = 0f
    private var turnDirection = 0
    private var thrusting = false
    private var waveClearTimerMs = 0f
    private var saucerSpawnTimerMs = saucerSpawnIntervalMs(wave).toFloat()
    private var nextExtraLifeScore = EXTRA_LIFE_SCORE

    init {
        startWave()
    }

    fun update(dtMillis: Long) {
        if (gameOver) return
        val dt = dtMillis.coerceAtLeast(0L)
        if (dt == 0L) return

        elapsedMs += dt
        val seconds = dt / 1_000f
        fireCooldownMs = (fireCooldownMs - dt).coerceAtLeast(0f)

        updateShip(dt, seconds)
        updateAsteroids(seconds)
        updateBullets(dt, seconds)
        updateSaucer(dt, seconds)
        resolvePlayerBullets()
        resolveEnemyBullets()
        resolveShipCollisions()
        updateWaveTimer(dt)
    }

    /** -1 turns left, +1 turns right, 0 stops turning. */
    fun setTurn(direction: Int) {
        turnDirection = direction.coerceIn(-1, 1)
    }

    fun setThrust(pressed: Boolean) {
        if (thrusting == pressed) return
        thrusting = pressed
        if (pressed) emit(AsteroidsEvent.ThrustStarted)
    }

    fun fire() {
        if (gameOver || !ship.alive || fireCooldownMs > 0f) return
        fireCooldownMs = FIRE_INTERVAL_MS
        val directionX = cos(ship.angle)
        val directionY = sin(ship.angle)
        bullets += Bullet(
            id = nextEntityId++,
            x = wrapCoordinate(ship.x + directionX * 18f, WORLD_WIDTH),
            y = wrapCoordinate(ship.y + directionY * 18f, WORLD_HEIGHT),
            vx = directionX * BULLET_SPEED + ship.vx * 0.25f,
            vy = directionY * BULLET_SPEED + ship.vy * 0.25f,
            lifeMs = BULLET_LIFE_MS,
            fromPlayer = true,
        )
        emit(AsteroidsEvent.Shot)
    }

    private fun updateShip(dt: Long, seconds: Float) {
        if (!ship.alive) return
        ship.invulnerabilityMs = (ship.invulnerabilityMs - dt).coerceAtLeast(0f)
        if (turnDirection != 0) {
            ship.angle = wrapAngle(ship.angle + turnDirection * SHIP_TURN_RATE * seconds)
        }
        if (thrusting) {
            ship.vx += cos(ship.angle) * SHIP_THRUST * seconds
            ship.vy += sin(ship.angle) * SHIP_THRUST * seconds
        }

        val drag = exp(-SHIP_FRICTION_PER_SECOND * seconds)
        ship.vx *= drag
        ship.vy *= drag

        val speed = sqrt(ship.vx * ship.vx + ship.vy * ship.vy)
        if (speed > SHIP_MAX_SPEED) {
            val factor = SHIP_MAX_SPEED / speed
            ship.vx *= factor
            ship.vy *= factor
        }

        ship.x = wrapCoordinate(ship.x + ship.vx * seconds, WORLD_WIDTH)
        ship.y = wrapCoordinate(ship.y + ship.vy * seconds, WORLD_HEIGHT)
    }

    private fun updateAsteroids(seconds: Float) {
        var index = 0
        while (index < asteroids.size) {
            val asteroid = asteroids[index]
            asteroid.x = wrapCoordinate(asteroid.x + asteroid.vx * seconds, WORLD_WIDTH)
            asteroid.y = wrapCoordinate(asteroid.y + asteroid.vy * seconds, WORLD_HEIGHT)
            asteroid.angle = wrapAngle(asteroid.angle + asteroid.spin * seconds)
            index++
        }
    }

    private fun updateBullets(dt: Long, seconds: Float) {
        var index = bullets.lastIndex
        while (index >= 0) {
            val bullet = bullets[index]
            bullet.lifeMs -= dt
            if (bullet.lifeMs <= 0f) {
                bullets.removeAt(index)
            } else {
                bullet.x = wrapCoordinate(bullet.x + bullet.vx * seconds, WORLD_WIDTH)
                bullet.y = wrapCoordinate(bullet.y + bullet.vy * seconds, WORLD_HEIGHT)
            }
            index--
        }
    }

    private fun updateSaucer(dt: Long, seconds: Float) {
        val current = saucer
        if (current == null) {
            saucerSpawnTimerMs -= dt
            if (saucerSpawnTimerMs <= 0f) {
                spawnSaucer()
                saucerSpawnTimerMs = saucerSpawnIntervalMs(wave).toFloat()
            }
            return
        }

        current.x = wrapCoordinate(current.x + current.vx * seconds, WORLD_WIDTH)
        current.y = wrapCoordinate(current.y + current.vy * seconds, WORLD_HEIGHT)
        current.remainingMs -= dt
        if (current.remainingMs <= 0f) {
            saucer = null
            saucerSpawnTimerMs = saucerSpawnIntervalMs(wave).toFloat()
            return
        }

        current.shootTimerMs -= dt
        while (current.shootTimerMs <= 0f) {
            shootFromSaucer(current)
            current.shootTimerMs += saucerShootIntervalMs()
        }
    }

    private fun spawnSaucer() {
        val fromLeft = rng.nextInt(2) == 0
        val speed = (70f + wave * 4f).coerceAtMost(140f)
        saucer = Saucer(
            x = if (fromLeft) 18f else WORLD_WIDTH - 18f,
            y = rng.nextFloat(120f, WORLD_HEIGHT - 120f),
            vx = if (fromLeft) speed else -speed,
            vy = rng.nextFloat(-18f, 18f),
            shootTimerMs = 850f,
            remainingMs = 12_000f,
        )
        emit(AsteroidsEvent.SaucerAppeared)
    }

    private fun shootFromSaucer(source: Saucer) {
        val aimX = torusDelta(source.x, ship.x, WORLD_WIDTH)
        val aimY = torusDelta(source.y, ship.y, WORLD_HEIGHT)
        val angle = atan2(aimY, aimX) + rng.nextFloat(-0.16f, 0.16f)
        bullets += Bullet(
            id = nextEntityId++,
            x = source.x,
            y = source.y,
            vx = cos(angle) * SAUCER_BULLET_SPEED,
            vy = sin(angle) * SAUCER_BULLET_SPEED,
            lifeMs = SAUCER_BULLET_LIFE_MS,
            fromPlayer = false,
        )
    }

    private fun resolvePlayerBullets() {
        var bulletIndex = bullets.lastIndex
        while (bulletIndex >= 0) {
            val bullet = bullets[bulletIndex]
            if (bullet.fromPlayer) {
                var hit = false

                var asteroidIndex = asteroids.lastIndex
                while (asteroidIndex >= 0) {
                    val asteroid = asteroids[asteroidIndex]
                    if (torusCirclesOverlap(
                            asteroid.x,
                            asteroid.y,
                            asteroid.size.radius,
                            bullet.x,
                            bullet.y,
                            BULLET_RADIUS,
                        )
                    ) {
                        destroyAsteroidAt(asteroidIndex)
                        hit = true
                        break
                    }
                    asteroidIndex--
                }

                if (!hit) {
                    val currentSaucer = saucer
                    if (currentSaucer != null &&
                        torusCirclesOverlap(
                            currentSaucer.x,
                            currentSaucer.y,
                            SAUCER_RADIUS,
                            bullet.x,
                            bullet.y,
                            BULLET_RADIUS,
                        )
                    ) {
                        destroySaucer()
                        hit = true
                    }
                }

                if (hit) bullets.removeAt(bulletIndex)
            }
            bulletIndex--
        }
    }

    private fun resolveEnemyBullets() {
        if (!ship.alive || ship.invulnerabilityMs > 0f) return
        var index = bullets.lastIndex
        while (index >= 0) {
            val bullet = bullets[index]
            if (!bullet.fromPlayer &&
                torusCirclesOverlap(
                    ship.x,
                    ship.y,
                    SHIP_RADIUS,
                    bullet.x,
                    bullet.y,
                    BULLET_RADIUS,
                )
            ) {
                bullets.removeAt(index)
                loseLife()
                return
            }
            index--
        }
    }

    private fun resolveShipCollisions() {
        if (!ship.alive || ship.invulnerabilityMs > 0f) return

        var index = 0
        while (index < asteroids.size) {
            val asteroid = asteroids[index]
            if (torusCirclesOverlap(
                    ship.x,
                    ship.y,
                    SHIP_RADIUS,
                    asteroid.x,
                    asteroid.y,
                    asteroid.size.radius,
                )
            ) {
                loseLife()
                return
            }
            index++
        }

        val currentSaucer = saucer
        if (currentSaucer != null &&
            torusCirclesOverlap(
                ship.x,
                ship.y,
                SHIP_RADIUS,
                currentSaucer.x,
                currentSaucer.y,
                SAUCER_RADIUS,
            )
        ) {
            destroySaucer(awardScore = false)
            loseLife()
        }
    }

    private fun updateWaveTimer(dt: Long) {
        if (asteroids.isNotEmpty() || gameOver) {
            waveClearTimerMs = 0f
            return
        }
        waveClearTimerMs += dt
        if (waveClearTimerMs >= WAVE_DELAY_MS) {
            wave += 1
            startWave()
        }
    }

    private fun startWave() {
        waveClearTimerMs = 0f
        repeat(asteroidsForWave(wave)) {
            spawnAsteroidAtEdge()
        }
        emit(AsteroidsEvent.WaveStarted(wave))
    }

    private fun spawnAsteroidAtEdge(size: AsteroidSize = AsteroidSize.LARGE): Asteroid {
        val edge = rng.nextInt(4)
        val x: Float
        val y: Float
        when (edge) {
            0 -> {
                x = rng.nextFloat(0f, WORLD_WIDTH)
                y = rng.nextFloat(0f, 24f)
            }

            1 -> {
                x = rng.nextFloat(WORLD_WIDTH - 24f, WORLD_WIDTH)
                y = rng.nextFloat(0f, WORLD_HEIGHT)
            }

            2 -> {
                x = rng.nextFloat(0f, WORLD_WIDTH)
                y = rng.nextFloat(WORLD_HEIGHT - 24f, WORLD_HEIGHT)
            }

            else -> {
                x = rng.nextFloat(0f, 24f)
                y = rng.nextFloat(0f, WORLD_HEIGHT)
            }
        }

        val toCenterX = torusDelta(x, WORLD_WIDTH / 2f, WORLD_WIDTH)
        val toCenterY = torusDelta(y, WORLD_HEIGHT / 2f, WORLD_HEIGHT)
        val angle = atan2(toCenterY, toCenterX) + rng.nextFloat(-0.9f, 0.9f)
        val speed = baseAsteroidSpeed() * rng.nextFloat(0.75f, 1.25f)

        return addAsteroid(
            x = x,
            y = y,
            vx = cos(angle) * speed,
            vy = sin(angle) * speed,
            size = size,
        )
    }

    private fun addAsteroid(
        x: Float,
        y: Float,
        vx: Float,
        vy: Float,
        size: AsteroidSize,
    ): Asteroid {
        val asteroid = Asteroid(
            id = nextEntityId++,
            x = wrapCoordinate(x, WORLD_WIDTH),
            y = wrapCoordinate(y, WORLD_HEIGHT),
            vx = vx,
            vy = vy,
            size = size,
            shape = generateShape(),
            angle = rng.nextFloat(0f, TWO_PI),
            spin = rng.nextFloat(-0.9f, 0.9f),
            colorIndex = rng.nextInt(0, 4),
        )
        asteroids += asteroid
        return asteroid
    }

    private fun generateShape(): FloatArray {
        val vertices = 9 + rng.nextInt(5)
        return FloatArray(vertices) { rng.nextFloat(0.72f, 1.28f) }
    }

    private fun baseAsteroidSpeed(): Float = (40f + wave * 6f).coerceAtMost(120f)

    private fun saucerShootIntervalMs(): Float =
        (1_200L - wave * 50L).coerceAtLeast(550L).toFloat()

    private fun destroyAsteroidAt(index: Int, awardScore: Boolean = true): Asteroid {
        val asteroid = asteroids.removeAt(index)
        if (awardScore) addScore(asteroid.size.score)
        emit(
            AsteroidsEvent.AsteroidDestroyed(
                size = asteroid.size,
                x = asteroid.x,
                y = asteroid.y,
                colorIndex = asteroid.colorIndex,
            ),
        )

        val childSize = asteroid.size.smaller()
        if (childSize != null) {
            val count = asteroid.size.minFragments +
                rng.nextInt(asteroid.size.maxFragments - asteroid.size.minFragments + 1)
            repeat(count) {
                val direction = rng.nextFloat(0f, TWO_PI)
                val speed = baseAsteroidSpeed() * rng.nextFloat(1.0f, 1.55f)
                addAsteroid(
                    x = asteroid.x + cos(direction) * asteroid.size.radius * 0.45f,
                    y = asteroid.y + sin(direction) * asteroid.size.radius * 0.45f,
                    vx = asteroid.vx * 0.35f + cos(direction) * speed,
                    vy = asteroid.vy * 0.35f + sin(direction) * speed,
                    size = childSize,
                )
            }
        }
        return asteroid
    }

    private fun destroySaucer(awardScore: Boolean = true) {
        if (saucer == null) return
        val destroyed = saucer
        saucer = null
        saucerSpawnTimerMs = saucerSpawnIntervalMs(wave).toFloat()
        if (awardScore) addScore(200)
        emit(AsteroidsEvent.SaucerDestroyed(destroyed?.x ?: 0f, destroyed?.y ?: 0f))
    }

    private fun loseLife() {
        if (gameOver) return
        lives -= 1
        emit(AsteroidsEvent.ShipHit(lives = lives, x = ship.x, y = ship.y))
        if (lives <= 0) {
            lives = 0
            ship.alive = false
            gameOver = true
            emit(AsteroidsEvent.GameOver(score = score, wave = wave))
            return
        }
        respawnShip()
    }

    private fun respawnShip() {
        ship.x = WORLD_WIDTH / 2f
        ship.y = WORLD_HEIGHT / 2f
        ship.vx = 0f
        ship.vy = 0f
        ship.angle = -PI.toFloat() / 2f
        ship.invulnerabilityMs = SHIP_INVULNERABILITY_MS
        ship.alive = true
        turnDirection = 0
        thrusting = false
    }

    private fun addScore(points: Int) {
        score += points
        while (score >= nextExtraLifeScore) {
            if (lives < MAX_LIVES) {
                lives += 1
                emit(AsteroidsEvent.ExtraLife)
            }
            nextExtraLifeScore += EXTRA_LIFE_SCORE
        }
    }

    private fun wrapAngle(angle: Float): Float {
        var value = angle % TWO_PI
        if (value < -PI.toFloat()) value += TWO_PI
        if (value > PI.toFloat()) value -= TWO_PI
        return value
    }

    private fun emit(event: AsteroidsEvent) {
        eventSink?.invoke(event)
    }

    // Test-only hooks keep the public simulation surface small without hiding
    // the entities needed by deterministic assertions.

    internal fun spawnAsteroidForTest(
        x: Float,
        y: Float,
        size: AsteroidSize,
        vx: Float = 0f,
        vy: Float = 0f,
    ): Asteroid = addAsteroid(x, y, vx, vy, size)

    internal fun spawnBulletForTest(
        x: Float,
        y: Float,
        vx: Float = 0f,
        vy: Float = 0f,
        fromPlayer: Boolean = true,
        lifeMs: Float = 900f,
    ): Bullet {
        val bullet = Bullet(
            id = nextEntityId++,
            x = wrapCoordinate(x, WORLD_WIDTH),
            y = wrapCoordinate(y, WORLD_HEIGHT),
            vx = vx,
            vy = vy,
            lifeMs = lifeMs,
            fromPlayer = fromPlayer,
        )
        bullets += bullet
        return bullet
    }

    internal fun spawnSaucerForTest(x: Float, y: Float): Saucer {
        val created = Saucer(
            x = wrapCoordinate(x, WORLD_WIDTH),
            y = wrapCoordinate(y, WORLD_HEIGHT),
            vx = 0f,
            vy = 0f,
            shootTimerMs = 10_000f,
            remainingMs = 10_000f,
        )
        saucer = created
        return created
    }

    internal fun clearAsteroidsForTest() {
        asteroids.clear()
    }

    internal fun clearBulletsForTest() {
        bullets.clear()
    }

    internal fun destroyAsteroidForTest(id: Int): Asteroid? {
        val index = asteroids.indexOfFirst { it.id == id }
        if (index < 0) return null
        return destroyAsteroidAt(index)
    }

    internal fun setScoreForTest(value: Int) {
        score = value
    }

    internal fun setLivesForTest(value: Int) {
        lives = value
    }

    internal fun debugSnapshot(): String = buildString {
        append("score=").append(score)
        append(" lives=").append(lives)
        append(" wave=").append(wave)
        append(" gameOver=").append(gameOver)
        append(" ship=").append(quantize(ship.x)).append(',').append(quantize(ship.y))
            .append(',').append(quantize(ship.angle))
            .append(',').append(quantize(ship.vx)).append(',').append(quantize(ship.vy))
            .append(',').append(quantize(ship.invulnerabilityMs))
        append(" asteroids=")
        asteroids.sortedBy { it.id }.forEach { asteroid ->
            append(asteroid.id).append(':').append(asteroid.size.name).append(':')
                .append(quantize(asteroid.x)).append(',').append(quantize(asteroid.y)).append(',')
                .append(quantize(asteroid.vx)).append(',').append(quantize(asteroid.vy)).append(';')
        }
        append(" bullets=")
        bullets.sortedBy { it.id }.forEach { bullet ->
            append(bullet.id).append(':').append(bullet.fromPlayer).append(':')
                .append(quantize(bullet.x)).append(',').append(quantize(bullet.y)).append(',')
                .append(quantize(bullet.vx)).append(',').append(quantize(bullet.vy)).append(',')
                .append(quantize(bullet.lifeMs)).append(';')
        }
        append(" saucer=")
        saucer?.let {
            append(quantize(it.x)).append(',').append(quantize(it.y)).append(',')
                .append(quantize(it.vx)).append(',').append(quantize(it.vy)).append(',')
                .append(quantize(it.shootTimerMs)).append(',').append(quantize(it.remainingMs))
        }
    }

    private fun quantize(value: Float): Int = (value * 1_000f).roundToInt()
}

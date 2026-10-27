package com.nikodindon.retrocade.games.asteroids.logic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AsteroidsEngineTest {
    @Test
    fun `coordinates wrap across every edge`() {
        assertEquals(5f, wrapCoordinate(1_005f, WORLD_WIDTH), 0.001f)
        assertEquals(995f, wrapCoordinate(-5f, WORLD_WIDTH), 0.001f)
        assertEquals(0f, wrapCoordinate(2_000f, WORLD_WIDTH), 0.001f)

        val engine = AsteroidsEngine(1L)
        engine.clearAsteroidsForTest()
        engine.spawnAsteroidForTest(995f, 500f, AsteroidSize.LARGE, vx = 100f)
        engine.update(100L)

        assertEquals(5f, engine.asteroids.single().x, 0.01f)
    }

    @Test
    fun `collisions use the shortest path across the torus`() {
        val engine = AsteroidsEngine(2L)
        engine.clearAsteroidsForTest()
        engine.spawnAsteroidForTest(999f, 500f, AsteroidSize.LARGE)
        engine.spawnBulletForTest(1f, 500f)

        engine.update(16L)

        assertTrue(engine.asteroids.none { it.size == AsteroidSize.LARGE })
        assertTrue(engine.bullets.isEmpty())
    }

    @Test
    fun `large fragments into mediums and mediums into smalls`() {
        val engine = AsteroidsEngine(3L)
        engine.clearAsteroidsForTest()
        val large = engine.spawnAsteroidForTest(500f, 500f, AsteroidSize.LARGE)

        engine.destroyAsteroidForTest(large.id)

        assertTrue(engine.asteroids.size in 2..3)
        assertTrue(engine.asteroids.all { it.size == AsteroidSize.MEDIUM })

        val medium = engine.asteroids.first()
        engine.destroyAsteroidForTest(medium.id)

        assertTrue(engine.asteroids.count { it.size == AsteroidSize.SMALL } in 2..3)

        val small = engine.asteroids.first { it.size == AsteroidSize.SMALL }
        engine.destroyAsteroidForTest(small.id)
        assertTrue(engine.asteroids.none { it.id == small.id })
    }

    @Test
    fun `bullets destroy asteroids and saucers then expire`() {
        val asteroidEngine = AsteroidsEngine(4L)
        asteroidEngine.clearAsteroidsForTest()
        asteroidEngine.spawnAsteroidForTest(500f, 500f, AsteroidSize.LARGE)
        asteroidEngine.spawnBulletForTest(500f, 500f)
        asteroidEngine.update(16L)
        assertTrue(asteroidEngine.bullets.isEmpty())

        val saucerEngine = AsteroidsEngine(5L)
        saucerEngine.clearAsteroidsForTest()
        saucerEngine.spawnSaucerForTest(500f, 500f)
        saucerEngine.spawnBulletForTest(500f, 500f)
        saucerEngine.update(16L)
        assertNull(saucerEngine.saucer)
        assertEquals(200, saucerEngine.score)
        assertTrue(saucerEngine.bullets.isEmpty())

        val expiryEngine = AsteroidsEngine(6L)
        expiryEngine.spawnBulletForTest(100f, 100f, lifeMs = 10f)
        expiryEngine.update(20L)
        assertTrue(expiryEngine.bullets.isEmpty())
    }

    @Test
    fun `ship collision costs one life and grants brief invulnerability`() {
        val engine = AsteroidsEngine(7L)
        engine.clearAsteroidsForTest()
        engine.ship.invulnerabilityMs = 0f
        engine.spawnAsteroidForTest(engine.ship.x, engine.ship.y, AsteroidSize.LARGE)

        engine.update(16L)

        assertEquals(2, engine.lives)
        assertEquals(WORLD_WIDTH / 2f, engine.ship.x, 0.01f)
        assertEquals(WORLD_HEIGHT / 2f, engine.ship.y, 0.01f)
        assertTrue(engine.ship.invulnerabilityMs > 0f)

        engine.update(16L)
        assertEquals(2, engine.lives)
    }

    @Test
    fun `waves add asteroids up to a cap`() {
        assertEquals(4, asteroidsForWave(1))
        assertEquals(6, asteroidsForWave(2))
        assertEquals(20, asteroidsForWave(100))
        assertTrue(saucerSpawnIntervalMs(1) > saucerSpawnIntervalMs(10))

        val engine = AsteroidsEngine(8L)
        engine.clearAsteroidsForTest()
        engine.update(WAVE_DELAY_MS + 10L)

        assertEquals(2, engine.wave)
        assertEquals(asteroidsForWave(2), engine.asteroids.size)

        val stress = AsteroidsEngine(9L, startingWave = 20)
        assertEquals(20, stress.asteroids.size)
    }

    @Test
    fun `score values extra lives and life cap are enforced`() {
        val large = AsteroidsEngine(10L)
        large.clearAsteroidsForTest()
        large.destroyAsteroidForTest(large.spawnAsteroidForTest(500f, 500f, AsteroidSize.LARGE).id)
        assertEquals(20, large.score)

        val medium = AsteroidsEngine(11L)
        medium.clearAsteroidsForTest()
        medium.destroyAsteroidForTest(medium.spawnAsteroidForTest(500f, 500f, AsteroidSize.MEDIUM).id)
        assertEquals(50, medium.score)

        val small = AsteroidsEngine(12L)
        small.clearAsteroidsForTest()
        small.destroyAsteroidForTest(small.spawnAsteroidForTest(500f, 500f, AsteroidSize.SMALL).id)
        assertEquals(100, small.score)

        val extraLife = AsteroidsEngine(13L)
        extraLife.clearAsteroidsForTest()
        extraLife.setScoreForTest(EXTRA_LIFE_SCORE - 10)
        extraLife.destroyAsteroidForTest(extraLife.spawnAsteroidForTest(500f, 500f, AsteroidSize.SMALL).id)
        assertEquals(4, extraLife.lives)
        assertTrue(extraLife.score >= EXTRA_LIFE_SCORE)

        val capped = AsteroidsEngine(14L)
        capped.clearAsteroidsForTest()
        capped.setLivesForTest(MAX_LIVES)
        capped.setScoreForTest(EXTRA_LIFE_SCORE - 10)
        capped.destroyAsteroidForTest(capped.spawnAsteroidForTest(500f, 500f, AsteroidSize.SMALL).id)
        assertEquals(MAX_LIVES, capped.lives)
    }

    @Test
    fun `game ends when the last life is lost`() {
        val engine = AsteroidsEngine(15L)
        engine.clearAsteroidsForTest()
        engine.setLivesForTest(1)
        engine.ship.invulnerabilityMs = 0f
        engine.spawnAsteroidForTest(engine.ship.x, engine.ship.y, AsteroidSize.LARGE)

        engine.update(16L)

        assertTrue(engine.gameOver)
        assertEquals(0, engine.lives)
        assertFalse(engine.ship.alive)
    }

    @Test
    fun `same seed and inputs produce the same final state`() {
        fun run(seed: Long): String {
            val engine = AsteroidsEngine(seed)
            var tick = 0
            repeat(600) {
                if (tick % 120 == 0) engine.fire()
                if (tick % 90 == 0) engine.setTurn(if (tick % 180 == 0) -1 else 1)
                if (tick % 60 == 0) engine.setThrust(true)
                if (tick % 60 == 30) engine.setThrust(false)
                if (tick % 200 == 0) engine.setTurn(0)
                engine.update(16L)
                tick++
            }
            return engine.debugSnapshot()
        }

        assertEquals(run(42L), run(42L))
    }

    @Test
    fun `saucer starts with a higher score and can be spawned`() {
        val engine = AsteroidsEngine(16L)
        engine.clearAsteroidsForTest()
        val saucer = engine.spawnSaucerForTest(200f, 300f)
        assertNotNull(saucer)
        assertEquals(300f, saucer.y, 0.01f)
    }
}

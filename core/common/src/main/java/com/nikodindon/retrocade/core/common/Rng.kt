package com.nikodindon.retrocade.core.common

/**
 * Small, fast, deterministic pseudo-random generator (SplitMix64).
 *
 * Two instances created with the same seed produce the same sequence on every
 * platform and JVM version, which makes replays and tests reliable.
 */
class Rng(seed: Long) {
    private var state: Long = seed

    fun nextLong(): Long {
        state += GOLDEN_GAMMA
        var z = state
        z = (z xor (z ushr 30)) * MIX_1
        z = (z xor (z ushr 27)) * MIX_2
        return z xor (z ushr 31)
    }

    fun nextInt(bound: Int): Int {
        require(bound > 0) { "bound must be positive" }
        return ((nextLong() ushr 1) % bound).toInt()
    }

    fun nextInt(from: Int, until: Int): Int {
        require(until > from) { "until must be greater than from" }
        return from + nextInt(until - from)
    }

    /** Float in [0, 1). */
    fun nextFloat(): Float = (nextLong() ushr 40) / 16_777_216f

    fun nextFloat(from: Float, until: Float): Float = from + nextFloat() * (until - from)

    fun <T> shuffled(source: List<T>): List<T> {
        val result = source.toMutableList()
        for (i in result.lastIndex downTo 1) {
            val j = nextInt(i + 1)
            val tmp = result[i]
            result[i] = result[j]
            result[j] = tmp
        }
        return result
    }

    private companion object {
        val GOLDEN_GAMMA = 0x9E3779B97F4A7C15uL.toLong()
        val MIX_1 = 0xBF58476D1CE4E5B9uL.toLong()
        val MIX_2 = 0x94D049BB133111EBuL.toLong()
    }
}

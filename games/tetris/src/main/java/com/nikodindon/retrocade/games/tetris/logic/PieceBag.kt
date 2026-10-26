package com.nikodindon.retrocade.games.tetris.logic

import com.nikodindon.retrocade.core.common.Rng

/** Standard 7-bag randomizer: each group of 7 contains every piece exactly once. */
class PieceBag(private val rng: Rng) {
    private val queue = ArrayDeque<PieceType>(14)

    fun next(): PieceType {
        if (queue.isEmpty()) refill()
        return queue.removeFirst()
    }

    fun peek(count: Int): List<PieceType> {
        require(count >= 0)
        while (queue.size < count) refill()
        return queue.take(count)
    }

    private fun refill() {
        queue.addAll(rng.shuffled(PieceType.entries))
    }
}

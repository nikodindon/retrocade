package com.nikodindon.retrocade.games.tetris.logic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GeometryTest {
    @Test
    fun `T rotations match SRS states`() {
        assertEquals(
            setOf(Cell(1, 0), Cell(0, 1), Cell(1, 1), Cell(2, 1)),
            PieceType.T.cells(0).toSet(),
        )
        assertEquals(
            setOf(Cell(1, 0), Cell(1, 1), Cell(1, 2), Cell(2, 1)),
            PieceType.T.cells(1).toSet(),
        )
        assertEquals(
            setOf(Cell(0, 1), Cell(1, 1), Cell(2, 1), Cell(1, 2)),
            PieceType.T.cells(2).toSet(),
        )
        assertEquals(
            setOf(Cell(0, 1), Cell(1, 0), Cell(1, 1), Cell(1, 2)),
            PieceType.T.cells(3).toSet(),
        )
    }

    @Test
    fun `I rotations match SRS states`() {
        assertEquals(
            setOf(Cell(0, 1), Cell(1, 1), Cell(2, 1), Cell(3, 1)),
            PieceType.I.cells(0).toSet(),
        )
        assertEquals(
            setOf(Cell(2, 0), Cell(2, 1), Cell(2, 2), Cell(2, 3)),
            PieceType.I.cells(1).toSet(),
        )
        assertEquals(
            setOf(Cell(0, 2), Cell(1, 2), Cell(2, 2), Cell(3, 2)),
            PieceType.I.cells(2).toSet(),
        )
        assertEquals(
            setOf(Cell(1, 0), Cell(1, 1), Cell(1, 2), Cell(1, 3)),
            PieceType.I.cells(3).toSet(),
        )
    }

    @Test
    fun `O never changes shape`() {
        val expected = setOf(Cell(0, 0), Cell(1, 0), Cell(0, 1), Cell(1, 1))
        repeat(4) { rotation ->
            assertEquals(expected, PieceType.O.cells(rotation).toSet())
        }
    }

    @Test
    fun `four rotations return every piece to its spawn shape`() {
        PieceType.entries.forEach { type ->
            assertEquals(type.cells(0).toSet(), type.cells(4).toSet(), "piece $type")
        }
    }

    @Test
    fun `every SRS state has four cells and stays in its box`() {
        PieceType.entries.forEach { type ->
            val box = if (type == PieceType.I) 4 else if (type == PieceType.O) 2 else 3
            repeat(4) { rotation ->
                val cells = type.cells(rotation)
                assertEquals(4, cells.size, "$type/$rotation")
                cells.forEach { cell ->
                    assertTrue(cell.x in 0 until box, "$type/$rotation x=${cell.x}")
                    assertTrue(cell.y in 0 until box, "$type/$rotation y=${cell.y}")
                }
            }
        }
    }

    @Test
    fun `kicks start with a zero offset for every transition`() {
        PieceType.entries.forEach { type ->
            repeat(4) { from ->
                listOf(1, -1).forEach { direction ->
                    val target = Math.floorMod(from + direction, 4)
                    val first = SrsKicks.candidates(type, from, target).first()
                    assertEquals(0, first.dx, "$type $from->$target")
                    assertEquals(0, first.dyUp, "$type $from->$target")
                }
            }
        }
    }

    @Test
    fun `O has exactly one kick candidate`() {
        assertEquals(1, SrsKicks.candidates(PieceType.O, 0, 1).size)
    }
}

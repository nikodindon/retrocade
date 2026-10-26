package com.nikodindon.retrocade.core.common

/**
 * An icon described as a pixel grid. Every 'X' is a lit pixel and every '.' is
 * empty; the UI paints it with Canvas, so there is no bitmap asset.
 */
data class ProceduralIcon(
    val rows: List<String>,
    val color: Long,
)

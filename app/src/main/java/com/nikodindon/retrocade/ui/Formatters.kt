package com.nikodindon.retrocade.ui

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val numberFormat = NumberFormat.getIntegerInstance(Locale.FRANCE)
private val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.FRANCE)

fun formatScore(score: Int): String = numberFormat.format(score)

fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))

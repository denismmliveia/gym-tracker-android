package com.gymtracker.domain.voice

import org.junit.Assert.*
import org.junit.Test

class SessionParserTest {
    private val parser = SessionParser()

    @Test
    fun `parses 'X series de Y con Z kilos'`() {
        val result = parser.parse("press banca, 3 series de 8 con 80 kilos")
        assertEquals(3, result.sets)
        assertEquals(8, result.reps)
        assertEquals(80f, result.weightKg)
    }

    @Test
    fun `parses 'X por Y a Z'`() {
        val result = parser.parse("sentadilla 4 por 10 a 60")
        assertEquals(4, result.sets)
        assertEquals(10, result.reps)
        assertEquals(60f, result.weightKg)
    }

    @Test
    fun `parses 'sin peso'`() {
        val result = parser.parse("dominadas, 5 series de 6 sin peso")
        assertEquals(5, result.sets)
        assertEquals(6, result.reps)
        assertEquals(0f, result.weightKg)
    }

    @Test
    fun `parses weight before sets 'Z kilos, X de Y'`() {
        val result = parser.parse("peso muerto 100 kilos, 3 de 5")
        assertEquals(3, result.sets)
        assertEquals(5, result.reps)
        assertEquals(100f, result.weightKg)
    }

    @Test
    fun `parses 'X por Y' with kg suffix`() {
        val result = parser.parse("curl 3 por 12 con 15 kg")
        assertEquals(3, result.sets)
        assertEquals(12, result.reps)
        assertEquals(15f, result.weightKg)
    }

    @Test
    fun `returns nulls when unparseable`() {
        val result = parser.parse("hola mundo")
        assertNull(result.sets)
        assertNull(result.reps)
        assertNull(result.weightKg)
    }
}

package com.gymtracker.domain.voice

data class ParsedSession(
    val sets: Int?,
    val reps: Int?,
    val weightKg: Float?
) {
    val isComplete: Boolean get() = sets != null && reps != null && weightKg != null
}

class SessionParser {
    fun parse(text: String): ParsedSession {
        val t = text.lowercase().trim()
        return ParsedSession(
            sets = extractSets(t),
            reps = extractReps(t),
            weightKg = extractWeight(t)
        )
    }

    private fun extractSets(t: String): Int? {
        Regex("""(\d+)\s*series""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        Regex("""(\d+)\s*[xX×]\s*\d+""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        Regex("""(\d+)\s*por\s*\d+""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        // "Z kilos, X de Y" — weight comes first, sets is the number after comma/kilos
        Regex("""(?:kilos?|kg)[^0-9]*(\d+)\s*de\s*\d+""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        Regex("""(\d+)\s*de\s*\d+""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        return null
    }

    private fun extractReps(t: String): Int? {
        Regex("""series\s+de\s+(\d+)""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        Regex("""\d+\s*[xX×]\s*(\d+)""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        Regex("""\d+\s*por\s*(\d+)""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        Regex("""(?:kilos?|kg)[^0-9]*\d+\s*de\s*(\d+)""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        Regex("""\d+\s*de\s*(\d+)""").find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        return null
    }

    private fun extractWeight(t: String): Float? {
        if (Regex("""sin\s*peso|peso\s*corporal|cuerpo""").containsMatchIn(t)) return 0f
        Regex("""(\d+(?:[.,]\d+)?)\s*(?:kilos?|kg)""").find(t)?.groupValues?.get(1)
            ?.replace(',', '.')?.toFloatOrNull()?.let { return it }
        Regex("""\b(?:con|a)\s+(\d+(?:[.,]\d+)?)(?!\s*(?:series?|reps?|repeticiones?))""").find(t)
            ?.groupValues?.get(1)?.replace(',', '.')?.toFloatOrNull()?.let { return it }
        return null
    }
}

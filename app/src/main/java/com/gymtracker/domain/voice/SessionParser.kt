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
        SETS_SERIES.find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        SETS_X.find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        SETS_POR.find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        SETS_AFTER_KG.find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        SETS_DE.find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        return null
    }

    private fun extractReps(t: String): Int? {
        REPS_SERIES_DE.find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        REPS_X.find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        REPS_POR.find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        REPS_AFTER_KG.find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        REPS_DE.find(t)?.groupValues?.get(1)?.toIntOrNull()?.let { return it }
        return null
    }

    private fun extractWeight(t: String): Float? {
        if (WEIGHT_ZERO.containsMatchIn(t)) return 0f
        WEIGHT_KG.find(t)?.groupValues?.get(1)?.replace(',', '.')?.toFloatOrNull()?.let { return it }
        WEIGHT_CON_A.find(t)?.groupValues?.get(1)?.replace(',', '.')?.toFloatOrNull()?.let { return it }
        return null
    }

    companion object {
        private val SETS_SERIES = Regex("""(\d+)\s*series""")
        private val SETS_X = Regex("""(\d+)\s*[xX×]\s*\d+""")
        private val SETS_POR = Regex("""(\d+)\s*por\s*\d+""")
        private val SETS_AFTER_KG = Regex("""(?:kilos?|kg)[^0-9]*(\d+)\s*de\s*\d+""")
        private val SETS_DE = Regex("""(\d+)\s*de\s*\d+""")

        private val REPS_SERIES_DE = Regex("""series\s+de\s+(\d+)""")
        private val REPS_X = Regex("""\d+\s*[xX×]\s*(\d+)""")
        private val REPS_POR = Regex("""\d+\s*por\s*(\d+)""")
        private val REPS_AFTER_KG = Regex("""(?:kilos?|kg)[^0-9]*\d+\s*de\s*(\d+)""")
        private val REPS_DE = Regex("""\d+\s*de\s*(\d+)""")

        private val WEIGHT_ZERO = Regex("""sin\s*peso|peso\s*corporal|cuerpo""")
        private val WEIGHT_KG = Regex("""(\d+(?:[.,]\d+)?)\s*(?:kilos?|kg)""")
        private val WEIGHT_CON_A = Regex("""\b(?:con|a)\s+(\d+(?:[.,]\d+)?)(?!\s*(?:series?|reps?|repeticiones?))""")
    }
}

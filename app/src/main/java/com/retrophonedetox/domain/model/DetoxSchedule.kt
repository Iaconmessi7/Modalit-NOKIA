package com.retrophonedetox.domain.model

import java.util.Calendar

/**
 * US 3.1 - Opzioni di attivazione manuale rapida con timer a scadenza automatica.
 */
enum class QuickTimerPreset(
    val minutes: Int,
    val label: String,
    val description: String
) {
    MIN_30(30, "Sessione 30 Min ⏱", "Attiva subito il Nokia per 30 minuti"),
    HOUR_1(60, "Sessione 1 Ora ⏱", "Attiva subito il Nokia per 60 minuti"),
    HOUR_2(120, "Sessione 2 Ore ⏱", "Attiva subito il Nokia per 2 ore")
}

/**
 * US 3.2 - Rappresenta una fascia oraria programmata ricorrente (stile Sveglia)
 * che attiva e disattiva automaticamente la Modalità NOKIA tramite `AlarmManager`.
 *
 * @property id Identificativo univoco (es. "morning_work", "afternoon_study", "night_detox")
 * @property title Nome breve mostrato sul display LCD (es. "Mattina 09:00-13:00")
 * @property daysTag Etichetta giorni (es. "L-V", "7gg", "S-D")
 * @property startHour Ora di inizio (0..23)
 * @property startMinute Minuto di inizio (0..59)
 * @property endHour Ora di fine (0..23)
 * @property endMinute Minuto di fine (0..59)
 * @property daysOfWeek Giorni della settimana (`Calendar.MONDAY` .. `Calendar.SUNDAY`)
 * @property isEnabled Se true, la fascia oraria si attiva automaticamente
 */
data class DetoxSchedule(
    val id: String,
    val title: String,
    val daysTag: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: Set<Int>,
    val isEnabled: Boolean
) {
    /**
     * Verifica se l'orario attuale (`now`) cade dentro questa fascia oraria attiva.
     * Gestisce correttamente anche le fasce notturne a cavallo della mezzanotte (es. 22:30 -> 07:00).
     */
    fun isCurrentlyInWindow(now: Calendar = Calendar.getInstance()): Boolean {
        if (!isEnabled) return false

        val dayOfWeek = now.get(Calendar.DAY_OF_WEEK)
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val startTotal = startHour * 60 + startMinute
        val endTotal = endHour * 60 + endMinute

        return if (startTotal <= endTotal) {
            // Fascia diurna nello stesso giorno (es. 09:00 - 13:00)
            (dayOfWeek in daysOfWeek) && (currentMinutes in startTotal until endTotal)
        } else {
            // Fascia notturna a cavallo della mezzanotte (es. 22:30 - 07:00)
            val isLateEveningToday = (dayOfWeek in daysOfWeek) && (currentMinutes >= startTotal)
            val yesterday = if (dayOfWeek == Calendar.SUNDAY) Calendar.SATURDAY else dayOfWeek - 1
            val isEarlyMorningFromYesterday = (yesterday in daysOfWeek) && (currentMinutes < endTotal)
            isLateEveningToday || isEarlyMorningFromYesterday
        }
    }
}

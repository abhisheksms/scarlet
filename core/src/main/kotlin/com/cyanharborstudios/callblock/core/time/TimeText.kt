package com.cyanharborstudios.callblock.core.time

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

/**
 * Turns times into text the way the phone is set: 24-hour ("17:05") or 12-hour ("5:05 PM").
 * The app passes in the system's 12/24-hour setting; nothing here reads it.
 */
class TimeText(private val is24Hour: Boolean, private val locale: Locale, private val zone: ZoneId) {

    private val timeFormat = DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "h:mm a", locale)
    private val hourFormat = DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "h a", locale)
    private val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)

    /** The time of day of a moment: "17:05" or "5:05 PM". One word: it never breaks across lines. */
    fun time(atMillis: Long): String = oneWord(timeFormat.format(Instant.ofEpochMilli(atMillis).atZone(zone)))

    /** An hour of the day (0 to 23) as a label: "20:00" or "8 PM". */
    fun hour(hourOfDay: Int): String = oneWord(hourFormat.format(LocalTime.of(hourOfDay, 0)))

    /** The space before AM or PM becomes a no-break space, so a time wraps as a whole. */
    private fun oneWord(text: String): String = text.replace(' ', '\u00A0').replace('\u202F', '\u00A0')

    /** A calendar date: "2 Oct 2026" or "Oct 2, 2026", by locale. */
    fun date(date: LocalDate): String = dateFormat.format(date)

    /** Date and time together: "2 Oct 2026, 17:05". */
    fun dateAndTime(atMillis: Long): String = "${date(dateOf(atMillis))}, ${time(atMillis)}"

    /** A short weekday name: "Mon". */
    fun weekday(date: LocalDate): String = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)

    fun dateOf(atMillis: Long): LocalDate = Instant.ofEpochMilli(atMillis).atZone(zone).toLocalDate()

    /** Whether a moment falls today, tomorrow, or on some other day, as calendar days in this zone. */
    fun dayRelation(atMillis: Long, nowMillis: Long): DayRelation {
        val day = dateOf(atMillis)
        val today = dateOf(nowMillis)
        return when (day) {
            today -> DayRelation.TODAY
            today.plusDays(1) -> DayRelation.TOMORROW
            else -> DayRelation.OTHER
        }
    }
}

/** A pause or an allow entry ends today ("20:30"), tomorrow ("tomorrow, 10:05"), or later (the date and time). */
enum class DayRelation { TODAY, TOMORROW, OTHER }

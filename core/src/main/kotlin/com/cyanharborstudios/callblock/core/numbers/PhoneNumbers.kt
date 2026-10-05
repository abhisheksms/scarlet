package com.cyanharborstudios.callblock.core.numbers

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat

/**
 * Turns the text of a phone number into a [PhoneNumber].
 *
 * [homeRegion] is the two-letter country of the phone ("IN", "US"). A number written
 * without a country code is read as a number in that country.
 */
class PhoneNumbers(homeRegion: String) {

    private val util = PhoneNumberUtil.getInstance()
    private val region = homeRegion.trim().uppercase()
    private val homeCountryCode = util.getCountryCodeForRegion(region) // 0 for an unknown region

    /** What a local number is written with in front in this country, "0" in India; null where there is none. */
    private val nationalPrefix: String? = util.getNddPrefixForRegion(region, true)?.takeIf { it.isNotEmpty() }

    fun parse(raw: String?): PhoneNumber {
        val text = raw?.trim().orEmpty()
        if (text.isEmpty()) {
            return PhoneNumber(raw = "", key = "", isInternational = false, display = "")
        }

        val parsed = try {
            util.parse(text, region)
        } catch (e: NumberParseException) {
            null
        }
        if (parsed == null || !util.isPossibleNumber(parsed)) {
            return unparsed(text)
        }

        val isInternational = homeCountryCode != 0 && parsed.countryCode != homeCountryCode
        val writtenWithCountryCode = text.startsWith("+")
        val format = if (isInternational || writtenWithCountryCode) {
            PhoneNumberFormat.INTERNATIONAL
        } else {
            PhoneNumberFormat.NATIONAL
        }
        return PhoneNumber(
            raw = text,
            key = util.format(parsed, PhoneNumberFormat.E164),
            isInternational = isInternational,
            display = util.format(parsed, format),
        )
    }

    /**
     * The start of a number as someone typed it, in the form the start of a [PhoneNumber.key]
     * has: on an Indian phone "080 4567" and "+91 80 4567" are both "+91804567", and "+92"
     * is a whole country. Null when the text holds no digits to go by.
     */
    fun startKey(typed: String?): String? {
        val text = typed?.trim().orEmpty()
        val digits = PhoneNumberUtil.normalizeDigitsOnly(text)
        if (digits.isEmpty()) return null
        if (text.startsWith("+")) return "+$digits"
        // "00" is how most of the world dials abroad, and no local number starts with it.
        if (digits.startsWith("00")) return if (digits.length > 2) "+" + digits.substring(2) else null
        val prefix = nationalPrefix
        val local = if (prefix != null && digits.startsWith(prefix)) digits.substring(prefix.length) else digits
        if (local.isEmpty() || homeCountryCode == 0) return null
        return "+$homeCountryCode$local"
    }

    /** A start as [startKey] gives it, spaced for reading the way a number is as it is typed: "+91 80 4567". */
    fun startDisplay(startKey: String): String {
        val formatter = util.getAsYouTypeFormatter(region)
        var shown = ""
        for (character in startKey) shown = formatter.inputDigit(character)
        return shown.trim()
    }

    /** A short code or anything else that is not a full phone number: keep its digits as the key. */
    private fun unparsed(text: String): PhoneNumber {
        val digits = text.filter { it.isDigit() }
        val key = if (text.startsWith("+") && digits.isNotEmpty()) "+$digits" else digits
        return PhoneNumber(raw = text, key = key, isInternational = false, display = text)
    }
}

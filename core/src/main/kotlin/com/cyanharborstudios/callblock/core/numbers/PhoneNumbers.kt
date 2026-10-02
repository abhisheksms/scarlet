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

    /** A short code or anything else that is not a full phone number: keep its digits as the key. */
    private fun unparsed(text: String): PhoneNumber {
        val digits = text.filter { it.isDigit() }
        val key = if (text.startsWith("+") && digits.isNotEmpty()) "+$digits" else digits
        return PhoneNumber(raw = text, key = key, isInternational = false, display = text)
    }
}

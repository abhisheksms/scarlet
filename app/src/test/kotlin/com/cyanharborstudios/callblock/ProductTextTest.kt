package com.cyanharborstudios.callblock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Words that must never reach a user or a store reviewer, checked against what ships.
 *
 * This app rebuilds the features of an existing app. Naming that app or its developer in
 * the product would be impersonation under Google Play's policy, so neither may appear in
 * any shipped source or resource. The studio also ships no urgency copy, no payment
 * wording outside Google Play, and never the word "AI" in front of a user.
 */
class ProductTextTest {

    // Gradle runs unit tests from the module directory (app/).
    private val shippedTrees = listOf(File("src/main"), File("../core/src/main"))

    private val shippedFiles: List<File> = shippedTrees.flatMap { tree ->
        tree.walkTopDown().filter { it.isFile && it.extension in setOf("kt", "xml", "kts", "pro") }.toList()
    }

    /** Every user-facing string and plural item in the default language. */
    private val userFacingText: List<String> = run {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File("src/main/res/values/strings.xml"))
        listOf("string", "item").flatMap { tag ->
            val nodes = document.getElementsByTagName(tag)
            (0 until nodes.length).map { nodes.item(it).textContent }
        }
    }

    private fun filesContaining(term: String): List<String> =
        shippedFiles.filter { it.readText().contains(term, ignoreCase = true) }.map { it.path }

    @Test
    fun `the checks look at real files`() {
        assertTrue("found only ${shippedFiles.size} files", shippedFiles.size > 30)
        assertTrue("found only ${userFacingText.size} strings", userFacingText.size > 50)
    }

    @Test
    fun `the reference app and its developer are never named in what ships`() {
        val terms = listOf("Block Unknown Callers", "Life Software Lab", "lifesoftwarelab", "incomingcallcontrol")
        val found = terms.associateWith(::filesContaining).filterValues { it.isNotEmpty() }
        assertEquals(emptyMap<String, List<String>>(), found)
    }

    @Test
    fun `no competitor's brand appears in user-facing text`() {
        val brands = listOf("Truecaller", "Hiya", "CallApp", "Whoscall", "RoboKiller", "Nomorobo", "Should I Answer")
        val found = userFacingText.filter { text -> brands.any { text.contains(it, ignoreCase = true) } }
        assertEquals(emptyList<String>(), found)
    }

    @Test
    fun `there is no urgency or scarcity copy`() {
        val phrases = listOf(
            "hurry", "limited time", "last chance", "act now", "only today", "don't miss", "expires soon",
            "offer ends", "while stocks last", "% off",
        )
        val found = userFacingText.filter { text -> phrases.any { text.contains(it, ignoreCase = true) } }
        assertEquals(emptyList<String>(), found)
    }

    @Test
    fun `nothing points to a payment outside Google Play`() {
        val acronyms = listOf("UPI", "BHIM")
        val phrases = listOf(
            "Paytm", "PhonePe", "Google Pay", "GPay", "Razorpay", "Stripe", "PayPal", "bank transfer", "pay via",
            "pay directly", "payment link", "buy on our website", "donate",
        )
        val found = userFacingText.filter { text ->
            acronyms.any { Regex("\\b$it\\b").containsMatchIn(text) } || phrases.any { text.contains(it, ignoreCase = true) }
        }
        assertEquals(emptyList<String>(), found)
    }

    @Test
    fun `the word AI is not shown to users`() {
        val found = userFacingText.filter { Regex("\\bAI\\b").containsMatchIn(it) }
        assertEquals(emptyList<String>(), found)
    }
}

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
        // The reference's listing uses two names for the app: its title, and another in its description.
        val terms = listOf(
            "Block Unknown Callers", "Easy Call Blocker", "Life Software Lab", "lifesoftwarelab", "incomingcallcontrol",
        )
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

    /**
     * A plan is sold by what it holds and what it costs. Nothing calls one popular, the best or
     * a bargain: with no sales behind it that would be made up, and a price struck through or a
     * saving has to be Google Play's own figure, never a string of ours.
     */
    @Test
    fun `no plan is called popular or the best, and no saving is claimed`() {
        val patterns = listOf(
            "\\bpopular\\b", "\\bbest (value|seller|deal|price)\\b", "\\bbestseller\\b", "\\btop (rated|choice|pick)\\b", "#1",
            "\\busers love\\b", "\\btrusted by\\b", "\\byou save\\b", "\\bsave (₹|rs|\\d)", "\\bwas (₹|rs)", "\\bdiscount(ed)?\\b", "\\bspecial offer\\b",
            "\\bdeal\\b", "\\bbargain\\b", "\\bcheap(er|est)?\\b",
        ).map { Regex(it, RegexOption.IGNORE_CASE) }
        val found = userFacingText.filter { text -> patterns.any { it.containsMatchIn(text) } }
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

    /**
     * The founder's copy rule (falcon, FOUNDER_TASTE.md §14): the product shows what it does and
     * never defends itself. A line like "Your contacts are never read" reads as a confession;
     * privacy facts live in the privacy policy and the store's Data safety form.
     */
    @Test
    fun `nothing reassures or denies`() {
        val patterns = listOf(
            "\\bnever\\b", "\\bnothing about\\b", "\\bno one\\b", "\\bnobody\\b", "\\bwe (do not|don['’]t|never|won['’]t)\\b",
            "\\bnot (shared|collected|read|stored|uploaded|sold|tracked)\\b", "\\bno account\\b", "\\bleaves? (this|your) phone\\b",
        ).map { Regex(it, RegexOption.IGNORE_CASE) }
        val found = userFacingText.filter { text -> patterns.any { it.containsMatchIn(text) } }
        assertEquals(emptyList<String>(), found)
    }

    /** No chummy or apologetic copy, no exclamation marks, no vague phrases: a label names the thing. */
    @Test
    fun `nothing is chatty or vague`() {
        val patterns = listOf(
            "\\bplease\\b", "\\boops\\b", "\\bsorry\\b", "\\bhey\\b", "\\bawesome\\b", "\\bgreat\\b", "\\byay\\b", "\\bwelcome\\b",
            "\\benjoy\\b", "\\blet['’]s\\b", "!", "\\bfor a while\\b", "\\bby themselves\\b", "\\bby itself\\b", "\\bunder the hood\\b",
        ).map { Regex(it, RegexOption.IGNORE_CASE) }
        val found = userFacingText.filter { text -> patterns.any { it.containsMatchIn(text) } }
        assertEquals(emptyList<String>(), found)
    }
}

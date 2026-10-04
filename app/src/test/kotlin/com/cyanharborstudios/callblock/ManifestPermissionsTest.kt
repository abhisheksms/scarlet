package com.cyanharborstudios.callblock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Pins every permission in the manifest that ships: the app's own and those its libraries
 * merge in. Play reviews this list. A new entry fails here until someone decides it belongs
 * (and, for anything sensitive, writes the ADR). See docs/SECURITY_CHECKLIST.md section 0.
 */
class ManifestPermissionsTest {

    private val android = "http://schemas.android.com/apk/res/android"

    private val manifest = run {
        val path = System.getProperty("mergedManifest") ?: error("mergedManifest is not set; run through Gradle")
        val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        factory.newDocumentBuilder().parse(File(path)).documentElement
    }

    private fun names(tag: String): List<String> {
        val nodes = manifest.getElementsByTagName(tag)
        return (0 until nodes.length).map { nodes.item(it).attributes.getNamedItemNS(android, "name").nodeValue }
    }

    @Test
    fun `the manifest requests exactly these permissions`() {
        val expected = setOf(
            // The app's own: notifications, and keeping the daily report check alive across a restart.
            "android.permission.POST_NOTIFICATIONS",
            "android.permission.RECEIVE_BOOT_COMPLETED",
            // Merged in by WorkManager (the daily report check).
            "android.permission.WAKE_LOCK",
            "android.permission.FOREGROUND_SERVICE",
            // Merged in by the Google Mobile Ads SDK.
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE",
            "com.google.android.gms.permission.AD_ID",
            "android.permission.ACCESS_ADSERVICES_AD_ID",
            "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
            "android.permission.ACCESS_ADSERVICES_TOPICS",
            // AndroidX's guard for its own non-exported broadcast receivers.
            "com.cyanharborstudios.callblock.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
        )
        assertEquals(expected, names("uses-permission").toSet())
    }

    @Test
    fun `nothing about contacts, the call log, the phone or SMS is requested`() {
        val forbiddenWords = listOf("CONTACTS", "CALL_LOG", "PHONE", "SMS", "PROCESS_OUTGOING_CALLS", "ANSWER_PHONE_CALLS")
        val offending = names("uses-permission").filter { name -> forbiddenWords.any { it in name } }
        assertEquals(emptyList<String>(), offending)
    }

    @Test
    fun `the screening service can only be bound by the system`() {
        val services = manifest.getElementsByTagName("service")
        val screening = (0 until services.length).map { services.item(it) }.single {
            it.attributes.getNamedItemNS(android, "name").nodeValue.endsWith(".screening.ScreeningService")
        }
        assertEquals(
            "android.permission.BIND_SCREENING_SERVICE",
            screening.attributes.getNamedItemNS(android, "permission").nodeValue,
        )
    }

    @Test
    fun `the quick settings tile can only be bound by the system`() {
        val services = manifest.getElementsByTagName("service")
        val tile = (0 until services.length).map { services.item(it) }.single {
            it.attributes.getNamedItemNS(android, "name").nodeValue.endsWith(".tile.PauseTileService")
        }
        assertEquals(
            "android.permission.BIND_QUICK_SETTINGS_TILE",
            tile.attributes.getNamedItemNS(android, "permission").nodeValue,
        )
    }

    @Test
    fun `the AdMob app id in the manifest is Google's sample app id`() {
        val metaData = manifest.getElementsByTagName("meta-data")
        val appId = (0 until metaData.length).map { metaData.item(it) }.single {
            it.attributes.getNamedItemNS(android, "name").nodeValue == "com.google.android.gms.ads.APPLICATION_ID"
        }
        assertEquals(
            "ca-app-pub-3940256099942544~3347511713",
            appId.attributes.getNamedItemNS(android, "value").nodeValue,
        )
    }

    @Test
    fun `the app's data is not backed up off the device`() {
        val application = manifest.getElementsByTagName("application").item(0)
        assertEquals("false", application.attributes.getNamedItemNS(android, "allowBackup").nodeValue)
    }

    @Test
    fun `only the launcher activity and the two system-bound services of our own are exported`() {
        val exportedOfOurs = listOf("activity", "service", "receiver", "provider").flatMap { tag ->
            val nodes = manifest.getElementsByTagName(tag)
            (0 until nodes.length).map { nodes.item(it) }
                .filter { it.attributes.getNamedItemNS(android, "exported")?.nodeValue == "true" }
                .map { it.attributes.getNamedItemNS(android, "name").nodeValue }
                .filter { it.startsWith("com.cyanharborstudios.callblock") }
        }
        assertEquals(
            setOf(
                "com.cyanharborstudios.callblock.MainActivity",
                "com.cyanharborstudios.callblock.screening.ScreeningService",
                "com.cyanharborstudios.callblock.tile.PauseTileService",
            ),
            exportedOfOurs.toSet(),
        )
        assertTrue(exportedOfOurs.size == 3)
    }
}

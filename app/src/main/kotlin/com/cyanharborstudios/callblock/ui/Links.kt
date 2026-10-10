package com.cyanharborstudios.callblock.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.cyanharborstudios.callblock.BuildConfig

/** Every address the app can send the user to, in one place. */
object Links {
    /**
     * Whether this app's privacy page is on the studio's site yet. A row that opens a
     * missing page is a dead control, so About shows the Privacy Policy row only once this
     * is true. Google Play requires that row in every build uploaded to it: switch this on
     * when the page is deployed, before the first upload (TASKS.md, "For launch").
     * LaunchGateTest refuses live ad ids while it is off.
     */
    val PRIVACY_PAGE_LIVE = false

    /**
     * Whether the app has a page on Google Play yet. Until it has, Share App and Rate App
     * have nowhere to go, and shared statistics carry no link.
     */
    val STORE_PAGE_LIVE = false

    /** Placeholder path on the studio's site; the page is published with the listing (PLAN gate G3). */
    const val PRIVACY_POLICY = "https://cyanharborstudios.com/call-blocker/privacy/"
    const val CONTACT_EMAIL = "contact@cyanharborstudios.com"
    const val APACHE_LICENSE = "https://www.apache.org/licenses/LICENSE-2.0"
    const val OPEN_FONT_LICENSE = "https://openfontlicense.org"
    const val STORE_PAGE = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
    private const val STORE_APP = "market://details?id=${BuildConfig.APPLICATION_ID}"

    /**
     * Google Play's own page for this app's subscription, where it is cancelled or changed.
     * Google Play's policy asks every app that sells a subscription for a way there.
     */
    const val SUBSCRIPTIONS = "https://play.google.com/store/account/subscriptions?sku=plus&package=${BuildConfig.APPLICATION_ID}"

    fun open(context: Context, url: String) = start(context, Intent(Intent.ACTION_VIEW, url.toUri()))

    fun openSubscriptions(context: Context) = open(context, SUBSCRIPTIONS)

    fun email(context: Context) = start(context, Intent(Intent.ACTION_SENDTO, "mailto:$CONTACT_EMAIL".toUri()))

    /** The Play Store app if there is one, else the store's web page. */
    fun openStorePage(context: Context) {
        if (!start(context, Intent(Intent.ACTION_VIEW, STORE_APP.toUri()))) open(context, STORE_PAGE)
    }

    fun shareText(context: Context, text: String) {
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        start(context, Intent.createChooser(send, null))
    }

    /** False when nothing on the phone can handle the intent. */
    private fun start(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}

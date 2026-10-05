package com.cyanharborstudios.callblock.core.plans

import com.cyanharborstudios.callblock.core.rules.Mode
import com.cyanharborstudios.callblock.core.rules.ScreeningSettings

/** What the user has paid for. Each tier holds everything the one before it holds. */
enum class Tier { FREE, NO_ADS, PRO }

/** What only Pro has. */
enum class ProFeature {
    /** Silence or Block for a while. (A pause, the timer at Off, is free.) */
    TIMER,

    /** Hours of the week that set the mode by themselves. */
    SCHEDULE,

    /** The user's own rules about how a number starts: always ring it, or always block it. */
    NUMBER_RULES,
}

/** One purchase as Google Play reports it: the products it is for, and whether it has been paid for. */
data class StorePurchase(val products: List<String>, val paid: Boolean)

/**
 * What each tier holds, in one place. The screens, the ads and the screening engine all
 * ask here, so they cannot disagree about what has been paid for.
 */
object Plans {

    /**
     * The three products on sale in Google Play, each bought once. The upgrade is Pro for
     * someone who already bought No Ads, at the difference in price.
     */
    const val NO_ADS_PRODUCT = "no_ads"
    const val PRO_PRODUCT = "pro"
    const val PRO_UPGRADE_PRODUCT = "pro_upgrade"

    /** Every product, for asking Google Play what is on sale. */
    val PRODUCTS = listOf(NO_ADS_PRODUCT, PRO_PRODUCT, PRO_UPGRADE_PRODUCT)

    fun showsAds(tier: Tier): Boolean = tier == Tier.FREE

    fun has(tier: Tier, feature: ProFeature): Boolean = when (feature) {
        ProFeature.TIMER, ProFeature.SCHEDULE, ProFeature.NUMBER_RULES -> tier == Tier.PRO
    }

    /** The tier a set of owned products adds up to. Anyone who paid for an upgrade has Pro. */
    fun tierFor(ownedProducts: Set<String>): Tier = when {
        PRO_PRODUCT in ownedProducts || PRO_UPGRADE_PRODUCT in ownedProducts -> Tier.PRO
        NO_ADS_PRODUCT in ownedProducts -> Tier.NO_ADS
        else -> Tier.FREE
    }

    /**
     * The tier a user's purchases add up to, as Google Play reports them. One that is still
     * waiting for its payment (cash at a shop, a slow approval) grants nothing until it is paid.
     */
    fun tierFor(purchases: List<StorePurchase>): Tier =
        tierFor(purchases.filter { it.paid }.flatMap { it.products }.toSet())

    /**
     * The product that takes someone on [from] to [to], or null when there is nothing to buy:
     * a plan they have, or one below it. From No Ads, Pro is the upgrade at the difference.
     */
    fun productFor(from: Tier, to: Tier): String? = when {
        from == Tier.FREE && to == Tier.NO_ADS -> NO_ADS_PRODUCT
        from == Tier.FREE && to == Tier.PRO -> PRO_PRODUCT
        from == Tier.NO_ADS && to == Tier.PRO -> PRO_UPGRADE_PRODUCT
        else -> null
    }

    /**
     * The settings as [tier] may use them. Without Pro the schedule is as if switched off,
     * a timer at Silence or Block is as if it had never been started (a pause still counts),
     * and there are no number rules. Nothing stored is thrown away, so buying Pro brings a
     * schedule and the rules back.
     */
    fun limit(settings: ScreeningSettings, tier: Tier): ScreeningSettings {
        var limited = settings
        if (!has(tier, ProFeature.SCHEDULE) && limited.scheduleOn) {
            limited = limited.copy(scheduleOn = false)
        }
        if (!has(tier, ProFeature.TIMER) && limited.timerMode != Mode.OFF) {
            limited = limited.copy(timerUntilMillis = 0, timerMode = Mode.OFF)
        }
        if (!has(tier, ProFeature.NUMBER_RULES) && limited.numberRules.isNotEmpty()) {
            limited = limited.copy(numberRules = emptyList())
        }
        return limited
    }
}

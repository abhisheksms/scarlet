package com.cyanharborstudios.callblock.core.plans

import com.cyanharborstudios.callblock.core.rules.Mode
import com.cyanharborstudios.callblock.core.rules.Scope
import com.cyanharborstudios.callblock.core.rules.ScreeningSettings

/** What the user has paid for. Each tier holds everything the one before it holds. */
enum class Tier { FREE, NO_ADS, PRO, PLUS }

/** What only a paid plan has. */
enum class ProFeature {
    /** Silence or Block for a while. (A pause, the timer at Off, is free.) Pro and Plus. */
    TIMER,

    /** Hours of the week that set the mode by themselves. Pro and Plus. */
    SCHEDULE,

    /** The user's own rules about how a number starts: always ring it, or always block it. Pro and Plus. */
    NUMBER_RULES,

    /** The frequent callers found in the app's own record: blocking one, blocking new ones unasked, and the scope that acts on those alone. Plus only. */
    FREQUENT_CALLERS,
}

/** One purchase as Google Play reports it: the products it is for, and whether it has been paid for. */
data class StorePurchase(val products: List<String>, val paid: Boolean)

/**
 * What each tier holds, in one place. The screens, the ads and the screening engine all
 * ask here, so they cannot disagree about what has been paid for.
 */
object Plans {

    /**
     * The three products bought once in Google Play. The upgrade is Pro for someone who
     * already bought No Ads, at the difference in price.
     */
    const val NO_ADS_PRODUCT = "no_ads"
    const val PRO_PRODUCT = "pro"
    const val PRO_UPGRADE_PRODUCT = "pro_upgrade"

    /** The one subscription: Plus, paid monthly or yearly, which holds Pro and the frequent callers. */
    const val PLUS_PRODUCT = "plus"

    /** Every product bought once, for asking Google Play what is on sale. */
    val PRODUCTS = listOf(NO_ADS_PRODUCT, PRO_PRODUCT, PRO_UPGRADE_PRODUCT)

    /** Every subscription, asked for separately: Google Play lists the two kinds apart. */
    val SUBSCRIPTIONS = listOf(PLUS_PRODUCT)

    fun showsAds(tier: Tier): Boolean = tier == Tier.FREE

    fun has(tier: Tier, feature: ProFeature): Boolean = when (feature) {
        ProFeature.TIMER, ProFeature.SCHEDULE, ProFeature.NUMBER_RULES -> tier >= Tier.PRO
        ProFeature.FREQUENT_CALLERS -> tier == Tier.PLUS
    }

    /**
     * The tier a set of owned products adds up to. Anyone who paid for an upgrade has Pro;
     * anyone whose subscription is running has Plus, whatever else they bought.
     */
    fun tierFor(ownedProducts: Set<String>): Tier = when {
        PLUS_PRODUCT in ownedProducts -> Tier.PLUS
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
     * Plus is the same subscription from every plan below it.
     */
    fun productFor(from: Tier, to: Tier): String? = when {
        to == Tier.PLUS && from < Tier.PLUS -> PLUS_PRODUCT
        from == Tier.FREE && to == Tier.NO_ADS -> NO_ADS_PRODUCT
        from == Tier.FREE && to == Tier.PRO -> PRO_PRODUCT
        from == Tier.NO_ADS && to == Tier.PRO -> PRO_UPGRADE_PRODUCT
        else -> null
    }

    /**
     * The settings as [tier] may use them. Without Pro the schedule is as if switched off,
     * a timer at Silence or Block is as if it had never been started (a pause still counts),
     * and there are no number rules. Without Plus there are no blocked frequent callers,
     * none is blocked unasked, and a scope of frequent callers only is read as all unknown
     * numbers, the lever's plain meaning. Nothing stored is thrown away, so buying the plan
     * again brings everything back.
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
        if (!has(tier, ProFeature.FREQUENT_CALLERS)) {
            if (limited.frequentCallers.isNotEmpty()) limited = limited.copy(frequentCallers = emptyList())
            if (limited.frequentAutoBlock) limited = limited.copy(frequentAutoBlock = false)
            if (limited.scope == Scope.FREQUENT_ONLY) limited = limited.copy(scope = Scope.ALL_UNKNOWN)
        }
        return limited
    }
}

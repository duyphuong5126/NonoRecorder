package com.nonoka.nonorecorder.constant

const val foreignExchangeRouteName = "foreignExchange"
const val assetMarketRouteName = "assetMarket"

sealed class MainNavigationRoute {
    abstract val label: String
    abstract val id: String

    data class ForeignExchangeRoute(override val label: String) : MainNavigationRoute() {
        override val id: String
            get() = foreignExchangeRouteName
    }

    data class AssetMarketRoute(override val label: String) : MainNavigationRoute() {
        override val id: String
            get() = assetMarketRouteName
    }
}
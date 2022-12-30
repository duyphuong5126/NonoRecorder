package com.nonoka.nonorecorder.feature.main

const val homeRouteName = "home"
const val recordedListRouteName = "recorded_list"

sealed class MainNavigationRoute {
    abstract val label: String
    abstract val id: String

    data class HomeRouteMain(override val label: String) : MainNavigationRoute() {
        override val id: String
            get() = homeRouteName
    }

    data class RecordedListRouteMain(override val label: String) : MainNavigationRoute() {
        override val id: String
            get() = recordedListRouteName
    }
}
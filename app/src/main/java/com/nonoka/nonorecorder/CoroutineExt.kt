package com.nonoka.nonorecorder

import kotlinx.coroutines.delay

suspend fun doWhile(action: () -> Unit, checker: () -> Boolean, interval: Long) {
    if (checker.invoke()) {
        delay(interval)
        action()
        doWhile(action, checker, interval)
    }
}
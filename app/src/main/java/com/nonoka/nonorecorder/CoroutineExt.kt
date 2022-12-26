package com.nonoka.nonorecorder

import kotlinx.coroutines.delay

suspend fun doWhile(action: () -> Unit, checker: () -> Boolean, interval: Long): Boolean {
    return if (checker.invoke()) {
        delay(interval)
        action()
        doWhile(action, checker, interval)
    } else {
        true
    }
}
package com.nonoka.nonorecorder

import android.app.Activity
import android.os.Bundle

class RedirectActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isTaskRoot) {
            finish()
        }
    }
}
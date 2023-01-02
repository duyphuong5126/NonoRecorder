package com.nonoka.nonorecorder

import android.app.Activity
import android.os.Bundle
import com.nonoka.nonorecorder.feature.main.MainActivity

class RedirectActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isTaskRoot) {
            MainActivity.start(this)
        }
        finish()
    }
}
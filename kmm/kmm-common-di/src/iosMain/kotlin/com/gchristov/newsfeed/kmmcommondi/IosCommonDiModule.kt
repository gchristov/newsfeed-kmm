package com.gchristov.newsfeed.kmmcommondi

import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger
import co.touchlab.kermit.crashlytics.setupCrashlyticsExceptionHook

@OptIn(ExperimentalKermitApi::class)
internal actual fun registerNativeLogger(logger: Logger) {
    setupCrashlyticsExceptionHook(logger)
}
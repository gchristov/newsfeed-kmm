package com.gchristov.newsfeed.kmmcommondi

import co.touchlab.kermit.*
import co.touchlab.kermit.crashlytics.CrashlyticsLogWriter
import org.kodein.di.DI
import org.kodein.di.bindSingleton

object CommonDiModule : DiModule() {
    override fun name() = "kmm-common-di"

    override fun bindLocalDependencies(builder: DI.Builder) {
        builder.apply {
            bindSingleton { provideLogger() }
        }
    }

    @OptIn(ExperimentalKermitApi::class)
    private fun provideLogger(): Logger = Logger(
        config = StaticConfig(
            minSeverity = Severity.Debug,
            logWriterList = listOf(
                CommonWriter(),
                CrashlyticsLogWriter()
            )
        )
    )
}

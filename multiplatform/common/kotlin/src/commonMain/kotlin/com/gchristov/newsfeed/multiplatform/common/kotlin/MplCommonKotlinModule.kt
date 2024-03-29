package com.gchristov.newsfeed.multiplatform.common.kotlin

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.gchristov.newsfeed.multiplatform.common.kotlin.di.DependencyModule
import org.kodein.di.DI
import org.kodein.di.bindSingleton

object MplCommonKotlinModule : DependencyModule() {
    override fun name() = "mpl-common-kotlin"

    override fun bindDependencies(builder: DI.Builder) {
        builder.apply {
            bindSingleton { provideLogger() }
            bindSingleton { provideDefaultJsonSerializer() }
            bindSingleton { provideExplicitNullsJsonSerializer() }
        }
    }

    private fun provideLogger(): Logger {
        Logger.setMinSeverity(Severity.Debug)
        Logger.setTag("Log")
        return Logger
    }

    private fun provideDefaultJsonSerializer() = JsonSerializer.Default

    private fun provideExplicitNullsJsonSerializer() = JsonSerializer.ExplicitNulls
}
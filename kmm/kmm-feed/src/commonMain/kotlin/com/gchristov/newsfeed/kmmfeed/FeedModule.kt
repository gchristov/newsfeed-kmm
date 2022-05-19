package com.gchristov.newsfeed.kmmfeed

import com.gchristov.newsfeed.kmmcommondi.DiModule
import com.gchristov.newsfeed.kmmcommondi.inject
import com.gchristov.newsfeed.kmmcommonfirebase.CommonFirebaseModule
import com.gchristov.newsfeed.kmmfeeddata.FeedDataModule
import kotlinx.coroutines.Dispatchers
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

object FeedModule : DiModule() {
    override fun name() = "kmm-feed"

    override fun bindLocalDependencies(builder: DI.Builder) {
        builder.apply {
            bindProvider {
                FeedViewModel(
                    dispatcher = Dispatchers.Main,
                    feedRepository = instance(),
                    getSectionedFeedUseCase = instance(),
                    redecorateSectionedFeedUseCase = instance(),
                    firestore = instance(),
                    logger = instance()
                )
            }
        }
    }

    override fun moduleDependencies(): List<DI.Module> {
        return listOf(FeedDataModule.module, CommonFirebaseModule.module)
    }

    fun injectFeedViewModel(): FeedViewModel = inject()
}

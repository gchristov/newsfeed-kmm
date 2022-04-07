package com.gchristov.newsfeed.kmmcommonfirebase

import com.gchristov.newsfeed.kmmcommondi.DiModule
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

object CommonFirebaseModule : DiModule() {
    override fun name() = "kmm-firebase-module"

    override fun bindLocalDependencies(builder: DI.Builder) {
        builder.apply {
            bindSingleton { provideFirebaseApp() }
            bindSingleton { provideFirestore(app = instance()) }
        }
    }

    private fun provideFirestore(app: FirebaseApp): FirebaseFirestore = Firebase.firestore(app)
}

internal expect fun provideFirebaseApp(): FirebaseApp

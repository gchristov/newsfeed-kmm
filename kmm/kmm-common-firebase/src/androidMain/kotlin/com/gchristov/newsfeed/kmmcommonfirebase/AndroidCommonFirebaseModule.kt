package com.gchristov.newsfeed.kmmcommonfirebase

import com.gchristov.newsfeed.kmmcommondi.AppContext
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.initialize

internal actual fun provideFirebaseApp(): FirebaseApp =
    requireNotNull(Firebase.initialize(context = AppContext))
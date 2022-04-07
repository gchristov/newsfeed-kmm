import com.gchristov.newsfeed.kmmgradleplugins.Deps

plugins {
    id("kmm-module-plugin")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(Deps.Firebase.firestore)
            }
        }
    }
}

import com.gchristov.newsfeed.gradleplugins.Deps

plugins {
    id("android-module-plugin")
    id("android-compose-plugin")
}

android {
    defaultConfig {
        namespace = "com.gchristov.newsfeed.android.common.composetest"
    }
}

/*
This module is used in other test modules. Common dependencies are linked to the 'main'
source sets, and marked as `api`, rather than 'test'. This is because 'test' source-specific
dependencies and code are local to the relevant module and cannot be accesses by other modules.
 */
dependencies {
    implementation(projects.android.common.compose)
    api(Deps.Android.Compose.uiTestJunit)
    debugApi(Deps.Android.Compose.uiTestManifest)
}

import com.gchristov.newsfeed.gradleplugins.Deps

plugins {
    id("android-application-plugin")
}

android {
    defaultConfig {
        applicationId = "com.gchristov.newsfeed"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(projects.kmmCommonDi)
    implementation(projects.commonDesign)
    implementation(projects.feed)
    // Firebase
    implementation(platform(Deps.Firebase.bom))
    implementation(Deps.Firebase.analytics)
    implementation(Deps.Firebase.crashlytics)
}

apply(plugin = "com.google.gms.google-services")
apply(plugin = "com.google.firebase.crashlytics")
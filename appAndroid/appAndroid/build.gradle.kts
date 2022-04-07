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
}

apply(plugin = "com.google.gms.google-services")
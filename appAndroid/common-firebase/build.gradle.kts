import com.gchristov.newsfeed.gradleplugins.Deps

plugins {
    id("android-module-plugin")
}

dependencies {
    implementation(platform(Deps.Firebase.bom))
    implementation(Deps.Firebase.analytics)
    implementation(Deps.Firebase.crashlytics)
}

apply(plugin = "com.google.gms.google-services")
apply(plugin = "com.google.firebase.crashlytics")
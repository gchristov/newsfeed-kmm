plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins.register("kmm-platform-plugin") {
        id = "kmm-platform-plugin"
        implementationClass = "com.gchristov.newsfeed.kmmgradleplugins.KmmPlatformPlugin"
    }
    plugins.register("kmm-module-plugin") {
        id = "kmm-module-plugin"
        implementationClass = "com.gchristov.newsfeed.kmmgradleplugins.KmmModulePlugin"
    }
    plugins.register("kmm-data-plugin") {
        id = "kmm-data-plugin"
        implementationClass = "com.gchristov.newsfeed.kmmgradleplugins.KmmDataPlugin"
    }
    plugins.register("kmm-feature-plugin") {
        id = "kmm-feature-plugin"
        implementationClass = "com.gchristov.newsfeed.kmmgradleplugins.KmmFeaturePlugin"
    }
}

repositories {
    google()
    mavenCentral()
}

// Needed, otherwise we get build errors (not picked up from project/build.gradle.kts for some reason)
dependencies {
    implementation("com.android.tools.build:gradle:7.2.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
}

// Needed to make all Kotlin versions the same during Xcode build (not picked up from project/build.gradle.kts for some reason)
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}
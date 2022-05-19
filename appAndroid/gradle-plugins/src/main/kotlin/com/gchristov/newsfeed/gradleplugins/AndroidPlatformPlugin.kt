package com.gchristov.newsfeed.gradleplugins

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationPlugin : AndroidPlatformPlugin() {
    override fun apply(target: Project) {
        target.plugins.apply("com.android.application")
        super.apply(target)
    }
}

open class AndroidLibraryPlugin : AndroidPlatformPlugin() {
    override fun apply(target: Project) {
        target.plugins.apply("com.android.library")
        super.apply(target)
    }
}

abstract class AndroidPlatformPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.configureAndroid()
    }
}

private fun Project.configureAndroid() {
    plugins.apply("kotlin-android")
    plugins.apply("kotlin-parcelize")
    extensions.configure<BaseExtension> {
        compileSdkVersion(Deps.Android.compileSdk)
        defaultConfig {
            minSdk = Deps.Android.minSdk
            targetSdk = Deps.Android.targetSdk
            testInstrumentationRunner = Deps.Tests.testInstrumentationRunner
        }
        buildTypes {
            getByName("release") {
                isMinifyEnabled = true
            }
        }
        // Resolves "N files found with path 'META-INF/XXX'" errors
        packagingOptions {
            resources.excludes.add("META-INF/AL2.0")
            resources.excludes.add("META-INF/LGPL2.1")
        }
    }
}

@Suppress("unused")
class Deps : Plugin<Project> {
    override fun apply(target: Project) {
        // No-op
    }

    object Android {
        const val minSdk = 21
        const val targetSdk = 31
        const val compileSdk = 31
    }

    object Accompanist {
        const val swipeRefresh = "com.google.accompanist:accompanist-swiperefresh:0.19.0"
    }

    object Compose {
        private const val composeVersion = "1.1.0-rc03"
        const val liveData = "androidx.compose.runtime:runtime-livedata:$composeVersion"
        const val compiler = "androidx.compose.compiler:compiler:$composeVersion"
        const val compilerExtension = composeVersion
        const val activity = "androidx.activity:activity-compose:1.3.1"
        const val uiTooling = "androidx.compose.ui:ui-tooling:$composeVersion"
        const val foundation = "androidx.compose.foundation:foundation:$composeVersion"
        const val foundationLayout = "androidx.compose.foundation:foundation-layout:$composeVersion"
        const val material = "androidx.compose.material:material:$composeVersion"
        const val materialIcons = "androidx.compose.material:material-icons-core:$composeVersion"
        const val uiTestJunit = "androidx.compose.ui:ui-test-junit4:$composeVersion"
        const val uiTestManifest = "androidx.compose.ui:ui-test-manifest:$composeVersion"
        const val coil = "io.coil-kt:coil-compose:1.4.0"
        const val html = "com.github.ireward:compose-html:1.0.1"
    }

    object Firebase {
        private const val firebaseVersion = "30.0.1"
        const val bom = "com.google.firebase:firebase-bom:$firebaseVersion"
        const val analytics = "com.google.firebase:firebase-analytics-ktx"
        const val crashlytics = "com.google.firebase:firebase-crashlytics-ktx"
    }

    object Tests {
        const val testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        const val junit = "junit:junit:4.13.2"
    }
}

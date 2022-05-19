package com.gchristov.newsfeed.kmmgradleplugins

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

/**
 * Plugin containing common setup for the KMM and Android Library plugins. Should be inherited by
 * all other plugins and specifically applied to common modules exposed through [KmmModulePlugin],
 * to avoid circular dependencies.
 */
open class KmmPlatformPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.configureLibrary()
        target.configureMultiplatform()
    }
}

private fun Project.configureLibrary() {
    plugins.apply("com.android.library")
    extensions.configure<BaseExtension> {
        compileSdkVersion(Deps.Android.compileSdk)
        defaultConfig {
            minSdk = Deps.Android.minSdk
            targetSdk = Deps.Android.targetSdk
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        // Include AndroidManifest.xml, if it exists.
        sourceSets {
            maybeCreate("main").manifest {
                srcFile("src/androidMain/AndroidManifest.xml")
            }
        }
        // Resolves "N files found with path 'META-INF/XXX'" errors
        packagingOptions {
            resources.excludes.add("META-INF/AL2.0")
            resources.excludes.add("META-INF/LGPL2.1")
        }
    }
}

private fun Project.configureMultiplatform() {
    plugins.apply("org.jetbrains.kotlin.multiplatform")
    extensions.configure<KotlinMultiplatformExtension> {
        android()
        val iosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget = when {
            System.getenv("SDK_NAME")?.startsWith("iphoneos") == true -> ::iosArm64
            System.getenv("NATIVE_ARCH")?.startsWith("arm") == true -> ::iosSimulatorArm64
            else -> ::iosX64
        }
        iosTarget("ios") {}
        // Allows libraries to not have to specify all source sets unless required.
        sourceSets.maybeCreate("commonMain")
        sourceSets.maybeCreate("androidMain")
        sourceSets.maybeCreate("iosMain")
    }
}

@Suppress("unused")
class Deps {
    object Android {
        const val minSdk = 21
        const val targetSdk = 31
        const val compileSdk = 31
    }

    object Firebase {
        private const val firebaseVersion = "1.5.0"
        const val firestore = "dev.gitlive:firebase-firestore:$firebaseVersion"
    }

    object Kermit {
        private const val kermitVersion = "1.1.2"
        const val logger = "co.touchlab:kermit:$kermitVersion"
        const val crashLogger = "co.touchlab:kermit-crashlytics:$kermitVersion"
    }

    object Kodein {
        const val di = "org.kodein.di:kodein-di:7.9.0"
    }

    object Kotlin {
        private const val coroutinesVersion = "1.6.0"

        // "-native-mt" is required here, otherwise iOS fails with runtime exception
        const val coroutinesCore =
            "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion-native-mt"
        const val coroutinesAndroid =
            "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion"
        const val dateTime = "org.jetbrains.kotlinx:kotlinx-datetime:0.3.2"
    }

    object Ktor {
        private const val ktorVersion = "1.6.7"
        const val clientCore = "io.ktor:ktor-client-core:$ktorVersion"
        const val clientSerialization = "io.ktor:ktor-client-serialization:$ktorVersion"
        const val clientLogging = "io.ktor:ktor-client-logging:$ktorVersion"
        const val clientAndroid = "io.ktor:ktor-client-android:$ktorVersion"
        const val clientIos = "io.ktor:ktor-client-ios:$ktorVersion"
        const val logbackClassic = "ch.qos.logback:logback-classic:1.2.10"
    }

    object Mvvm {
        private const val mvvmVersion = "0.12.0"
        const val liveData = "dev.icerock.moko:mvvm-livedata:$mvvmVersion"
        const val test = "dev.icerock.moko:mvvm-test:$mvvmVersion"
    }

    object SqlDelight {
        private const val sqlDelightVersion = "1.5.3"
        const val runtime = "com.squareup.sqldelight:runtime:$sqlDelightVersion"
        const val driverAndroid = "com.squareup.sqldelight:android-driver:$sqlDelightVersion"
        const val driverNative = "com.squareup.sqldelight:native-driver:$sqlDelightVersion"
    }

    object SharedPreferences {
        const val multiplatformSettings = "com.russhwolf:multiplatform-settings:0.8.1"
    }

    object Tests {
        // Assertions for use in common code
        const val testCommon = "test-common"

        // Test annotations for use in common code
        const val testCommonAnnotations = "test-annotations-common"

        /*
        Provides an implementation of Asserter on top of JUnit and maps the test
        annotations from kotlin-test-annotations-common to JUnit test annotations
         */
        const val testJunit = "test-junit"
        const val junit = "junit:junit:4.13.2"

        // Used to set a test instrumentation runner for Android
        const val junitRunner = "androidx.test:runner:1.4.0"

        // Used for InstantTaskExecutorRule
        const val archCoreTesting = "androidx.arch.core:core-testing:2.1.0"
    }
}

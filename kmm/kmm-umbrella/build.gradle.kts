import com.gchristov.newsfeed.kmmgradleplugins.Deps
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    // Normally, this would be kmm-module-plugin but since this is an umbrella module this is enough
    id("kmm-platform-plugin")
}

kotlin {
    /*
     Dependencies that should be exported to native clients (e.g. iOS) to avoid having their module
     name added as prefix to their class names. More info here:
     https://touchlab.co/multiple-kotlin-frameworks-in-application/
     */
    val exportedDependencies = listOf(
        Deps.Kotlin.coroutinesCore, // Needed for coroutine dispatchers
        projects.kmmCommonTest,
        projects.kmmFeed,
        projects.kmmFeedData,
        projects.kmmFeedTestFixtures,
        projects.kmmPost,
        projects.kmmPostData,
        projects.kmmPostTestFixtures,
    )

    val iosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget = when {
        System.getenv("SDK_NAME")?.startsWith("iphoneos") == true -> ::iosArm64
        System.getenv("NATIVE_ARCH")?.startsWith("arm") == true -> ::iosSimulatorArm64
        else -> ::iosX64
    }
    iosTarget("ios") {
        binaries {
            framework {
                baseName = "KmmShared"
                isStatic = true
                exportedDependencies.forEach { export(it) }
                // Required for SQLDelight
                freeCompilerArgs = freeCompilerArgs + arrayOf("-linker-options", "-lsqlite3")
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                exportedDependencies.forEach { api(it) }
            }
        }
    }
}
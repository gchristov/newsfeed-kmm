plugins {
    id("mpl-feature-plugin")
}

android {
    defaultConfig {
        namespace = "com.gchristov.newsfeed.multiplatform.post.feature"
    }
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.multiplatform.post.data)
                api(projects.multiplatform.post.testFixtures)
            }
        }
    }
}

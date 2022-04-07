/**
 * Prefix KMM modules with 'kmm-'.
 * Suffix Gradle plugin modules with '-plugins'.
 */
val projects = listOf(
    "kmm-common-di",
    "kmm-common-firebase",
    "kmm-common-kotlin",
    "kmm-common-mvvm",
    "kmm-common-mvvm-test",
    "kmm-common-network",
    "kmm-common-persistence",
    "kmm-common-test",
    "kmm-feed",
    "kmm-feed-data",
    "kmm-feed-test-fixtures",
    "kmm-gradle-plugins",
    "kmm-post",
    "kmm-post-data",
    "kmm-post-test-fixtures",
    "kmm-umbrella",
    "appAndroid",
    "common-compose",
    "common-compose-test",
    "common-design",
    "common-navigation",
    "common-test",
    "feed",
    "feed-test-fixtures",
    "gradle-plugins",
    "post",
    "post-test-fixtures",
)
/**
 * Android modules are under the 'appAndroid' directory.
 * KMM modules are under the 'kmm' directory.
 */
projects.forEach { project ->
    if (project.isGradlePlugin()) {
        includeBuild(project.projectDir())
    } else {
        include(":$project")
        project(":$project").projectDir = java.io.File(project.projectDir())
    }
}

fun String.projectRoot() = if (startsWith("kmm-")) "kmm" else "appAndroid"

fun String.projectDir() = "${projectRoot()}/$this"

fun String.isGradlePlugin() = endsWith("-plugins")
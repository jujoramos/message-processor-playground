pluginManagement {
    // Manually parse the Toml file to workaround issues with version catalog not supporting direct use in settings.gradle
    val catalog = file("gradle/libs.versions.toml").readText()
    fun extractVersion(id: String): String {
        val matcher = java.util.regex.Pattern.compile("${id}\\s*=\\s*\"([^\"]+)\"").matcher(catalog)
        return if (matcher.find()) {
            matcher.group(1)
        } else {
            throw IllegalArgumentException("No version found for dependency with $id.")
        }
    }

    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version extractVersion("foojay-resolver-convention-plugin")
    }
}


plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}

rootProject.name = "message-processor-playground"
include("app", "utilities")

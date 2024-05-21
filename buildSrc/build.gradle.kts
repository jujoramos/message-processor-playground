plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.spotless.gradle.plugin)
    implementation(libs.spotbugs.gradle.plugin)

    // HACK https://stackoverflow.com/questions/76713758/use-version-catalog-inside-precompiled-gradle-plugin
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

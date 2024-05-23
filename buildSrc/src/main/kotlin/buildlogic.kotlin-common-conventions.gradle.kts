import com.github.spotbugs.snom.SpotBugsTask
import com.diffplug.gradle.spotless.SpotlessCheck
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    idea
    id("org.jetbrains.kotlin.jvm")
    id("com.github.spotbugs")
    id("com.diffplug.spotless")
}

repositories {
    mavenCentral()
}

val libs = the<LibrariesForLibs>()
dependencies {
    implementation(libs.bundles.logging)
    testImplementation(libs.bundles.testing)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }

    withSourcesJar()
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

spotless {
    kotlin {
        ktlint()
        endWithNewline()
        indentWithTabs()
    }

    java {
        importOrder()
        endWithNewline()
        indentWithTabs()
        formatAnnotations()
        removeUnusedImports()
        trimTrailingWhitespace()
        googleJavaFormat().aosp().reflowLongStrings().skipJavadocFormatting()
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    outputs.upToDateWhen { false }
}

val configDirectory = "${rootDir}/config"
tasks.withType<SpotBugsTask> {
    tasks.getByName("assemble").dependsOn(this)
    excludeFilter = file("${configDirectory}/spotbugs-exclude.xml")
}
project.gradle.startParameter.excludedTaskNames.add("spotbugsTest")

tasks.withType<SpotlessCheck> {
    tasks.getByName("assemble").dependsOn(this)
}

import gradle.kotlin.dsl.accessors._c5a1b2808732718201c96d23252852b9.implementation
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("buildlogic.kotlin-common-conventions")
    application
}

val libs = the<LibrariesForLibs>()
dependencies {
    implementation(libs.picocli)
}

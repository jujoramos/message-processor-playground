plugins {
    id("buildlogic.kotlin-application-conventions")
}

version = "1.0.0"
group = "com.playground.app"

dependencies {
    implementation(project(":utilities"))
}

application {
    mainClass = "com.playground.app.CliAppKt"
}

plugins {
    id("buildlogic.kotlin-application-conventions")
}

version = "1.0.0"
group = "com.logitech.app"

dependencies {
    implementation(project(":utilities"))
}

application {
    mainClass = "com.logitech.app.CliAppKt"
}

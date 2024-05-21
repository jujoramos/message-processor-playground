plugins {
    id("buildlogic.kotlin-application-conventions")
}

dependencies {
    implementation(project(":utilities"))
}

application {
    mainClass = "com.logitech.app.AppKt"
}

// Pure Kotlin: the screening rules, phone-number handling and statistics.
// Nothing here may depend on Android, so all of it runs in plain JVM unit tests.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.libphonenumber)
    testImplementation(libs.junit)
}

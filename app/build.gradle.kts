plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

android {
    namespace = "com.cyanharborstudios.callblock"
    // Compiled against Android 17's SDK because current AndroidX libraries require it.
    // What the app targets (and how it behaves) is still targetSdk below.
    compileSdk = 37

    defaultConfig {
        // Permanent after the first Play upload. Pinned by LaunchGateTest.
        applicationId = "com.cyanharborstudios.callblock"
        // 29: the call-screening role (RoleManager) and silencing a call both arrived in Android 10.
        minSdk = 29
        // 36: what Google Play requires of new apps since 31 August 2026.
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        // targetSdk is 36 on purpose: it is what Play requires (ADR-005), not the newest SDK.
        disable += "OldTargetApi"
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

// ManifestPermissionsTest reads the merged manifest (ours plus what libraries add): hand
// each variant's unit-test task that file's path, and make the task wait for it.
androidComponents {
    onVariants { variant ->
        val mergedManifest = variant.artifacts.get(com.android.build.api.artifact.SingleArtifact.MERGED_MANIFEST)
        val unitTestTask = "test${variant.name.replaceFirstChar { it.uppercase() }}UnitTest"
        tasks.withType<Test>().matching { it.name == unitTestTask }.configureEach {
            inputs.file(mergedManifest)
            doFirst { systemProperty("mergedManifest", mergedManifest.get().asFile.path) }
        }
    }
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

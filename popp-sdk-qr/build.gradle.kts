import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kover)
}

kotlin {
    androidLibrary {
        namespace = "de.servicehealth.poppmodule.sdk.qr"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
        withHostTest {}
    }

    sourceSets {
        androidMain.dependencies {
            api(projects.poppSdk)
            implementation(libs.kotlinx.coroutines.core)
            api(libs.androidx.camera.core)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.mlkit.barcode.scanning)
        }
    }
}

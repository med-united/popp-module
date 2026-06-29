import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kover)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    androidLibrary {
        namespace = "de.servicehealth.poppmodule.demo"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.compose.material.icons)
        }
        commonMain.dependencies {
            api(projects.poppSdk)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        val androidHostTest by getting {
            dependencies {
                implementation(libs.robolectric)
                implementation(libs.roborazzi.core)
                implementation(libs.roborazzi.compose)
                implementation(libs.roborazzi.preview.scanner)
                implementation(libs.compose.ui.test.manifest)
                implementation(libs.compose.ui.test)
                implementation(libs.compose.ui.test.junit4)
                implementation(libs.androidx.testExt.junit)
                // Required at runtime by AndroidComposePreviewTester to scan @Preview functions
                runtimeOnly(libs.composable.preview.scanner.android)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "de.servicehealth.poppmodule.demo.generated.resources"
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

// Mode control — read at configuration time (configuration-cache safe)
tasks.withType<Test>().configureEach {
    val requestedTasks = gradle.startParameter.taskNames
    val mode =
        when {
            requestedTasks.any { it.contains("recordSnapshots") } -> "record"
            requestedTasks.any { it.contains("verifySnapshots") } -> "verify"
            else -> project.findProperty("roborazziMode")?.toString()
        }
    when (mode) {
        "record" -> systemProperty("roborazzi.test.record", "true")
        "verify" -> systemProperty("roborazzi.test.verify", "true")
        else -> systemProperty("roborazzi.test.compare", "true")
    }
}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kover)
}

kotlin {
    // Silence the beta warning for expect/actual classes — we use them
    // deliberately (PoppSdkContext) and the API is stable enough for our use.
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    val xcf = XCFramework("PoppSdk")
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "PoppSdk"
            isStatic = true
            xcf.add(this)
        }
    }

    androidLibrary {
        namespace = "de.servicehealth.poppmodule.sdk"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        androidMain.dependencies {
            // zeta-sdk 1.0.1 currently publishes only JVM + Android variants on Maven Central,
            // so the wiring lives in androidMain. The iOS source set ships a stub until an
            // iOS native variant becomes available.
            implementation(libs.gematik.zetaSdk)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

dependencies {
    add("androidHostTestImplementation", libs.robolectric)
}

tasks.matching { it.name == "testAndroidHostTest" }.configureEach {
    (this as? Test)?.apply {
        if (project.hasProperty("integration")) {
            include("**/*IntegrationTest*")
            System.getProperty("popp.integration.fqdn")?.let { systemProperty("popp.integration.fqdn", it) }
            System.getProperty("popp.integration.ca.pem.file")?.let { systemProperty("popp.integration.ca.pem.file", it) }
        } else {
            exclude("**/*IntegrationTest*")
        }
    }
}
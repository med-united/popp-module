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
        iosSimulatorArm64(),
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
        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        androidMain.dependencies {
            // zeta-sdk 1.0.1 currently publishes only JVM + Android variants on Maven Central,
            // so the wiring lives in androidMain. The iOS source set ships a stub until an
            // iOS native variant becomes available.
            implementation(libs.gematik.zetaSdk)
            implementation(libs.androidx.security.crypto)
            // Ktor JVM/Android engine for the WebSocket scenario transport.
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.nimbus.jose.jwt)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
    }
}

// com.android.kotlin.multiplatform.library (AGP 9.0.1) does not expose isCoreLibraryDesugaringEnabled
// in its DSL. The metadata check is suppressed because minSdk=28 covers the Java 8 APIs that
// zeta-sdk requires, and the device test only exercises standard Android APIs — zeta-sdk
// is never called by the test.
tasks.matching { it.name == "checkAndroidDeviceTestAarMetadata" }.configureEach {
    enabled = false
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    add("androidHostTestImplementation", libs.robolectric)
    add("androidDeviceTestImplementation", libs.androidx.testExt.junit)
    add("androidDeviceTestImplementation", libs.androidx.test.runner)
    add("androidDeviceTestImplementation", libs.kotlin.testJunit)
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

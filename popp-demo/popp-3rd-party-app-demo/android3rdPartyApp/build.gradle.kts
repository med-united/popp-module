import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(projects.poppDemo.popp3rdPartyAppDemo.shared3rdPartyApp)
    implementation(projects.poppSdk)

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "de.servicehealth.poppmodule.demo.thirdparty"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        applicationId = "de.servicehealth.poppmodule.demo.thirdparty"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    flavorDimensions += "popp_server"
    // The 3rd-party demo drives the eGK read loop via checkInWithEgk, which runs over the *direct*
    // WebSocket transport (ZETA routing is dormant — see PoppSdk.checkInWithEgk TODO + POPPM-180). That
    // direct transport can't use the ZETA-gated ingress yet (HTTP 401), so `local` points at the direct
    // ws://localhost:8443/ws endpoint here — unlike the insurance demo, whose `local` uses the ZETA
    // ingress because it only calls init(fqdn) and never runs the eGK loop.
    productFlavors {
        // Local dockerized PoPP-Server, reached directly (eGK read loop bypasses the ZETA ingress).
        // On a phone use `adb reverse tcp:8443 tcp:8443` so localhost:8443 reaches the host stack.
        create("local") {
            dimension = "popp_server"
            isDefault = true
            buildConfigField("String", "POPP_SERVER_FQDN", "\"ws://localhost:8443/ws\"")
        }
        // RISE intermediate PoPP-Server (dev environment)
        create("rise") {
            dimension = "popp_server"
            buildConfigField("String", "POPP_SERVER_FQDN", "\"wss://popp.dev.poppservice.de:443/popp/practitioner/api/v1/token-generation-ehc\"")
        }
        // gematik RU PoPP-Server (todo: update URL when available)
        create("ru") {
            dimension = "popp_server"
            buildConfigField("String", "POPP_SERVER_FQDN", "\"wss://TODO_RU_POPP_SERVER_FQDN\"")
        }
        // gematik PU PoPP-Server (todo: update URL when available) — select explicitly for release builds
        create("pu") {
            dimension = "popp_server"
            buildConfigField("String", "POPP_SERVER_FQDN", "\"wss://TODO_PU_POPP_SERVER_FQDN\"")
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

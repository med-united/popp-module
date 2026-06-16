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
    // RECONCILE ON POPPM-115 MERGE: POPPM-115 sets `local` = wss://popp-zeta-ingress:443/ws for its
    // ZETA `init(fqdn)`, but the eGK read loop (PoppSdk.directTransport → checkInWithEgk) can't use the
    // ZETA-gated ingress yet (HTTP 401), so `local` here is the direct ws://…:8443/ws endpoint. When
    // 115 lands these are genuinely different endpoints for different purposes — reconcile the split
    // (e.g. two BuildConfig fields, or route the eGK transport through a ZETA-authenticated transport).
    productFlavors {
        // Local dockerized PoPP-Server, reached directly (eGK read loop bypasses the ZETA ingress).
        // On a phone use `adb reverse tcp:8443 tcp:8443` so localhost:8443 reaches the host stack.
        create("local") {
            dimension = "popp_server"
            isDefault = true
            buildConfigField("String", "POPP_SERVER_FQDN", "\"ws://localhost:8443/ws\"")
        }
        create("rise") {
            dimension = "popp_server"
            buildConfigField("String", "POPP_SERVER_FQDN", "\"wss://popp.dev.poppservice.de:443/popp/practitioner/api/v1/token-generation-ehc\"")
        }
        create("ru") {
            dimension = "popp_server"
            buildConfigField("String", "POPP_SERVER_FQDN", "\"wss://TODO_RU_POPP_SERVER_FQDN\"")
        }
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

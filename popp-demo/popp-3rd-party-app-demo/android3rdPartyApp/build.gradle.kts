import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.playPublisher)
}

// Populated only in CI (release.yml); local/dev builds fall back to unsigned + placeholder version.
val releaseKeystorePath = providers.environmentVariable("ANDROID_KEYSTORE_PATH").orNull
val releaseKeystorePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").orNull
val releaseKeyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS").orNull
val releaseKeyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD").orNull
val releaseVersionCode = providers.environmentVariable("RELEASE_VERSION_CODE").orNull?.toIntOrNull() ?: 1
val releaseVersionName = providers.environmentVariable("RELEASE_VERSION_NAME").orNull ?: "1.0"
val googlePlayServiceAccountJsonPath = providers.environmentVariable("GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_PATH").orNull

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
        versionCode = releaseVersionCode
        versionName = releaseVersionName
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
    signingConfigs {
        if (releaseKeystorePath != null) {
            create("release") {
                storeFile = file(releaseKeystorePath)
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            if (releaseKeystorePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

play {
    // Only set in CI (release.yml); left unconfigured for local/dev builds so unrelated Gradle
    // tasks don't require Play Store credentials to exist.
    if (googlePlayServiceAccountJsonPath != null) {
        serviceAccountCredentials.set(file(googlePlayServiceAccountJsonPath))
    }
    track.set("internal")
    defaultToAppBundles.set(true)
}

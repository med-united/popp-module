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

    defaultConfig {
        applicationId = "de.servicehealth.poppmodule.demo.thirdparty"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        buildConfig = true
    }
    flavorDimensions += "popp_server"
    productFlavors {
        // Local ZetaGuard + PoPP-Server (docker-compose from popp-sample-code)
        create("local") {
            dimension = "popp_server"
            buildConfigField("String", "POPP_SERVER_FQDN", "\"wss://popp-zeta-ingress:443/ws\"")
        }
        // RISE intermediate PoPP-Server (dev environment)
        create("rise") {
            dimension = "popp_server"
            isDefault = true
            buildConfigField("String", "POPP_SERVER_FQDN", "\"wss://popp.dev.poppservice.de:443/popp/practitioner/api/v1/token-generation-ehc\"")
        }
        // gematik RU PoPP-Server (todo: update URL when available) — default for debug builds
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

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

dependencies {
    kover(projects.poppSdk)
    kover(projects.poppDemo.shared)
    kover(projects.poppDemo.popp3rdPartyAppDemo.shared3rdPartyApp)
}

kover {
    reports {
        filters {
            excludes {
                classes("*.generated.resources.*") // Res, fonts, drawables,...
                classes("*ComposableSingletons*")
                classes("de.servicehealth.poppmodule.sdk.qr.AndroidQrScanner*")
            }
        }
    }
}
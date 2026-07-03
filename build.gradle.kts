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

allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        filter {
            exclude { element -> element.file.path.replace('\\', '/').contains("/build/generated/") }
        }
    }
}

dependencies {
    kover(projects.poppSdk)
    kover(projects.poppSdkQr)
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

// --- iOS release packaging (3rd-party demo app; CI-only, invoked from build-ios in release.yml) ---
val ios3rdPartyAppDir = layout.projectDirectory.dir("popp-demo/popp-3rd-party-app-demo/ios3rdPartyApp")
val iosReleaseBuildDir = layout.buildDirectory.dir("ios-release")

val xcodeArchiveIos3rdPartyApp by tasks.registering(Exec::class) {
    group = "ios release"
    description = "Archives + code-signs the 3rd-party iOS demo app (manual signing, CI-only)."
    notCompatibleWithConfigurationCache("shells out to xcodebuild")
    workingDir(ios3rdPartyAppDir)
    val archivePath = iosReleaseBuildDir.map { it.dir("iosApp.xcarchive") }
    outputs.dir(archivePath)
    doFirst {
        val provisioningSpecifier =
            System.getenv("IOS_PROVISIONING_PROFILE_SPECIFIER")
                ?: error("IOS_PROVISIONING_PROFILE_SPECIFIER env var is required for manual signing")
        commandLine(
            "xcodebuild", "archive",
            "-project", "iosApp.xcodeproj",
            "-scheme", "iosApp",
            "-configuration", "Release",
            "-archivePath", archivePath.get().asFile.absolutePath,
            "-destination", "generic/platform=iOS",
            "CODE_SIGN_STYLE=Manual",
            "CODE_SIGN_IDENTITY=Apple Distribution",
            "PROVISIONING_PROFILE_SPECIFIER=$provisioningSpecifier",
            "DEVELOPMENT_TEAM=YX6NS7XNPL",
            // Release builds use a clean, registered bundle ID — Config.xcconfig's $(TEAM_ID)
            // suffix is a local-dev-only convention to dodge automatic-signing collisions.
            "PRODUCT_BUNDLE_IDENTIFIER=de.servicehealth.poppmodule.demo.thirdparty",
        )
    }
}

val xcodeExportIpaIos3rdPartyApp by tasks.registering(Exec::class) {
    group = "ios release"
    description = "Exports a .ipa from the archive built by xcodeArchiveIos3rdPartyApp (export only, no upload)."
    dependsOn(xcodeArchiveIos3rdPartyApp)
    notCompatibleWithConfigurationCache("shells out to xcodebuild")
    workingDir(ios3rdPartyAppDir)
    val archivePath = iosReleaseBuildDir.map { it.dir("iosApp.xcarchive") }
    val exportPath = iosReleaseBuildDir.map { it.dir("export") }
    inputs.dir(archivePath)
    outputs.dir(exportPath)
    doFirst {
        commandLine(
            "xcodebuild",
            "-exportArchive",
            "-archivePath",
            archivePath.get().asFile.absolutePath,
            "-exportPath",
            exportPath.get().asFile.absolutePath,
            "-exportOptionsPlist",
            "exportOptions.plist",
        )
    }
}

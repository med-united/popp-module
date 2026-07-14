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
        // Falls back to the static Config.xcconfig values (MARKETING_VERSION=1.0,
        // CURRENT_PROJECT_VERSION=1) for local/manual archives outside CI.
        val releaseVersionName = System.getenv("RELEASE_VERSION_NAME")
        val releaseVersionCode = System.getenv("RELEASE_VERSION_CODE")
        commandLine(
            buildList {
                addAll(
                    listOf(
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
                        // Release builds use a clean, registered bundle ID — Config.xcconfig's
                        // $(TEAM_ID) suffix is a local-dev-only convention to dodge
                        // automatic-signing collisions.
                        "PRODUCT_BUNDLE_IDENTIFIER=de.servicehealth.poppmodule.demo.thirdparty",
                    ),
                )
                if (releaseVersionName != null) add("MARKETING_VERSION=$releaseVersionName")
                if (releaseVersionCode != null) add("CURRENT_PROJECT_VERSION=$releaseVersionCode")
            },
        )
    }
}

// xcodebuild -exportArchive doesn't reuse the PROVISIONING_PROFILE_SPECIFIER passed at archive
// time — with manual signing it needs its own explicit bundle-id -> profile-name mapping, so the
// plist is generated here instead of using a static committed file. Shared by both the plain
// export task and the upload task below, which differ only in `destination`.
fun writeIosExportOptionsPlist(
    plistFile: File,
    provisioningSpecifier: String,
    destination: String,
) {
    plistFile.parentFile.mkdirs()
    plistFile.writeText(
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
        <plist version="1.0">
        <dict>
            <key>method</key>
            <string>app-store-connect</string>
            <key>destination</key>
            <string>$destination</string>
            <key>teamID</key>
            <string>YX6NS7XNPL</string>
            <key>signingStyle</key>
            <string>manual</string>
            <key>uploadSymbols</key>
            <false/>
            <key>provisioningProfiles</key>
            <dict>
                <key>de.servicehealth.poppmodule.demo.thirdparty</key>
                <string>$provisioningSpecifier</string>
            </dict>
        </dict>
        </plist>
        """.trimIndent(),
    )
}

val xcodeExportIpaIos3rdPartyApp by tasks.registering(Exec::class) {
    group = "ios release"
    description = "Exports a signed .ipa from the archive built by xcodeArchiveIos3rdPartyApp (export only, no upload)."
    dependsOn(xcodeArchiveIos3rdPartyApp)
    notCompatibleWithConfigurationCache("shells out to xcodebuild")
    workingDir(ios3rdPartyAppDir)
    val archivePath = iosReleaseBuildDir.map { it.dir("iosApp.xcarchive") }
    val exportPath = iosReleaseBuildDir.map { it.dir("export") }
    val generatedExportOptionsPlist = iosReleaseBuildDir.map { it.file("exportOptions.plist") }
    inputs.dir(archivePath)
    outputs.dir(exportPath)
    doFirst {
        val provisioningSpecifier =
            System.getenv("IOS_PROVISIONING_PROFILE_SPECIFIER")
                ?: error("IOS_PROVISIONING_PROFILE_SPECIFIER env var is required to export a signed archive")
        val plistFile = generatedExportOptionsPlist.get().asFile
        writeIosExportOptionsPlist(plistFile, provisioningSpecifier, destination = "export")
        commandLine(
            "xcodebuild",
            "-exportArchive",
            "-archivePath",
            archivePath.get().asFile.absolutePath,
            "-exportPath",
            exportPath.get().asFile.absolutePath,
            "-exportOptionsPlist",
            plistFile.absolutePath,
        )
    }
}

val xcodeUploadIpaIos3rdPartyApp by tasks.registering(Exec::class) {
    group = "ios release"
    description = "Uploads the .ipa exported by xcodeExportIpaIos3rdPartyApp to App Store Connect. " +
        "Only meant to run on real tag releases, not manual workflow_dispatch runs."
    dependsOn(xcodeExportIpaIos3rdPartyApp)
    notCompatibleWithConfigurationCache("shells out to xcodebuild")
    workingDir(ios3rdPartyAppDir)
    val archivePath = iosReleaseBuildDir.map { it.dir("iosApp.xcarchive") }
    val uploadPath = iosReleaseBuildDir.map { it.dir("upload") }
    val generatedUploadOptionsPlist = iosReleaseBuildDir.map { it.file("uploadOptions.plist") }
    inputs.dir(archivePath)
    outputs.dir(uploadPath)
    doFirst {
        val provisioningSpecifier =
            System.getenv("IOS_PROVISIONING_PROFILE_SPECIFIER")
                ?: error("IOS_PROVISIONING_PROFILE_SPECIFIER env var is required to export a signed archive")
        val apiKeyPath =
            System.getenv("APP_STORE_CONNECT_API_KEY_PATH")
                ?: error("APP_STORE_CONNECT_API_KEY_PATH env var is required to upload to App Store Connect")
        val apiKeyId =
            System.getenv("APP_STORE_CONNECT_KEY_ID")
                ?: error("APP_STORE_CONNECT_KEY_ID env var is required to upload to App Store Connect")
        val apiKeyIssuerId =
            System.getenv("APP_STORE_CONNECT_ISSUER_ID")
                ?: error("APP_STORE_CONNECT_ISSUER_ID env var is required to upload to App Store Connect")
        val plistFile = generatedUploadOptionsPlist.get().asFile
        writeIosExportOptionsPlist(plistFile, provisioningSpecifier, destination = "upload")
        commandLine(
            "xcodebuild",
            "-exportArchive",
            "-archivePath",
            archivePath.get().asFile.absolutePath,
            "-exportPath",
            uploadPath.get().asFile.absolutePath,
            "-exportOptionsPlist",
            plistFile.absolutePath,
            "-authenticationKeyPath",
            apiKeyPath,
            "-authenticationKeyID",
            apiKeyId,
            "-authenticationKeyIssuerID",
            apiKeyIssuerId,
        )
    }
}

// --- Roborazzi snapshot testing (main-branch addition, unrelated to iOS release packaging above) ---
tasks.register("recordSnapshots") {
    group = "roborazzi"
    description = "Record Roborazzi snapshot baselines in both demo modules"
    dependsOn(
        ":popp-demo:shared:testAndroidHostTest",
        ":popp-demo:popp-3rd-party-app-demo:shared3rdPartyApp:testAndroidHostTest",
    )
}

tasks.register("verifySnapshots") {
    group = "roborazzi"
    description = "Verify Roborazzi snapshots against recorded baselines in both demo modules"
    dependsOn(
        ":popp-demo:shared:testAndroidHostTest",
        ":popp-demo:popp-3rd-party-app-demo:shared3rdPartyApp:testAndroidHostTest",
    )
}

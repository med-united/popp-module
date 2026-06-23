rootProject.name = "PoPP-Module"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":popp-sdk")
include(":popp-sdk-qr")
include(":popp-demo:shared")
include(":popp-demo:popp-3rd-party-app-demo:shared3rdPartyApp")
include(":popp-demo:popp-3rd-party-app-demo:android3rdPartyApp")
include(":popp-demo:popp-insurance-app-demo:sharedInsuranceApp")
include(":popp-demo:popp-insurance-app-demo:androidInsuranceApp")

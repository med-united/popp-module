package de.servicehealth.poppmodule.sdk

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

package de.servicehealth.poppmodule

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
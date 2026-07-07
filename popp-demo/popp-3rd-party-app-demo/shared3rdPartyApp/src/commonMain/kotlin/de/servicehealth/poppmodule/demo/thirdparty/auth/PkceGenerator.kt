package de.servicehealth.poppmodule.demo.thirdparty.auth

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

expect object PkceGenerator {
    fun generateCodeVerifier(): String

    fun generateCodeChallenge(verifier: String): String
}

@OptIn(ExperimentalEncodingApi::class)
internal fun ByteArray.encodeBase64Url(): String = Base64.UrlSafe.encode(this).trimEnd('=')

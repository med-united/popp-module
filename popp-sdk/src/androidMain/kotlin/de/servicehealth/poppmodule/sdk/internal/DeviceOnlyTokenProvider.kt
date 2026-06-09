package de.servicehealth.poppmodule.sdk.internal

import de.gematik.zeta.sdk.authentication.SubjectTokenProvider
import de.gematik.zeta.sdk.tpm.TpmProvider

/**
 * Placeholder SubjectTokenProvider for the device-only ZETA session.
 *
 * Returns a hardcoded JWT accepted by dev/test server configurations.
 * Replace once the PoPP spec clarifies the device-level access token format.
 */
internal class DeviceOnlyTokenProvider : SubjectTokenProvider {
    override suspend fun createSubjectToken(
        clientId: String,
        dpopKey: String,
        nonceBytes: ByteArray,
        audience: String,
        now: Long,
        expiration: Long,
        tpmProvider: TpmProvider,
    ): String = HARDCODED_TOKEN

    companion object {
        private const val HARDCODED_TOKEN =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ild1WjhsUmh0dm9vWXF4cTFTcDdIczlmZThvYUVIVXpEY0VyRFg5QnY5aE0ifQ.eyJleHAiOjE3NjU5OTExMjAsImlhdCI6MTc1ODYxMDg2OCwianRpIjoib25ydHJvOjU5OWNmYjAyLTI0YTktNDViZi0xNDRlLTZjNDg5YTYxMzI2NSIsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6MTgwODAvcmVhbG1zL3NtYy1iIiwiYXVkIjpbInJlcXVlc3Rlci1jbGllbnQiLCJhY2NvdW50Il0sInN1YiI6ImQwYWFjYzljLTJkOTMtNDM4YS1hNzAzLWI4Nzc4OTIxODNmOCIsInR5cCI6IkJlYXJlciIsImF6cCI6InNtYy1iLWNsaWVudCIsInNpZCI6IjY5ZDgxODA4LTY2ZTYtNDlmMi04OWRiLTdiODBlOGU4OTlmYiIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1zbWMtYiIsIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6ImVtYWlsIHByb2ZpbGUiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6IlVzZXIgRXh0ZXJuYWwiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJ1c2VyIiwiZ2l2ZW5fbmFtZSI6IlVzZXIiLCJmYW1pbHlfbmFtZSI6IkV4dGVybmFsIiwiZW1haWwiOiJ1c2VyQGJhci5mb28uY29tIn0.V-zP1JRCX47tnTHGHUmECUkFD4XfPLnda7vNoufpIxs6eYj3_u0YISw6VbXXFmttO_w3xQ7zAJB3RioSewuVyQ"
    }
}
package de.servicehealth.poppmodule.sdk

/**
 * Platform-specific entry token required to bootstrap the SDK.
 *
 * On Android it aliases [android.content.Context] — host apps pass their
 * `Application` (or any long-lived) context. On iOS it is an empty marker
 * class; Swift callers construct one with `PoppSdkContext()`.
 */
expect class PoppSdkContext
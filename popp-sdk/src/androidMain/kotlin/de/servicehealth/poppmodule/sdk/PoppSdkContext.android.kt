package de.servicehealth.poppmodule.sdk

import android.content.Context

/**
 * Wraps an Android [Context] (typically the host app's `Application`) so the
 * SDK can build EncryptedSharedPreferences against it. Use the long-lived
 * application context to avoid leaking activities.
 */
actual class PoppSdkContext(val androidContext: Context)

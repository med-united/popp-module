package de.servicehealth.poppmodule.demo.thirdparty.can

import android.content.Context
import de.servicehealth.poppmodule.sdk.PoppSdkContext
import de.servicehealth.poppmodule.sdk.storage.createSecureStorage

/** Builds the encrypted (EncryptedSharedPreferences-backed) CAN store for Android. */
fun createSecureCanStore(context: Context): CanStore =
    SecureStorageCanStore(createSecureStorage(PoppSdkContext(context), CAN_NAMESPACE))

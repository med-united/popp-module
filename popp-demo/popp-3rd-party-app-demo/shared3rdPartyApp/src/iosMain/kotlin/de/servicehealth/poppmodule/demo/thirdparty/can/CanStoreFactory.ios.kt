package de.servicehealth.poppmodule.demo.thirdparty.can

import de.servicehealth.poppmodule.sdk.PoppSdkContext
import de.servicehealth.poppmodule.sdk.storage.createSecureStorage

/**
 * Builds the CAN store for iOS. The SDK SecureStorage is an in-memory placeholder on
 * iOS (Keychain deferred), so the remembered CAN is process-lifetime only for now.
 */
fun createSecureCanStore(): CanStore =
    SecureStorageCanStore(createSecureStorage(PoppSdkContext(), CAN_NAMESPACE))

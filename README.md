# Android Biometrics Integration Sample

This project provides a sample implementation for integrating biometric authentication in Android apps. It covers both normal and cryptographic biometric operations, serving as a practical guide for developers.

## Overview

The application is structured into several main components, each handling different aspects of biometric integration:

### Components

- **Biometric Verification Callback**: Interfaces to handle success, failure, and error events during biometric operations.
- **CryptoBiometricsHelper**: Manages cryptographic operations with biometric authentication.
- **NormalBiometricHelper**: Handles basic biometric authentication processes.

## Key Components

### BiometricVerificationCallback Interface

- `onSuccess(result: String?)`: Triggered when biometric verification is successfully completed.
- `onFailure(isLock: Boolean)`: Triggered when verification fails or is canceled, includes a flag for biometric lockout.
- `onError(error: String)`: Called when an error occurs during biometric processing.

### CryptoBiometricsHelper

Manages cryptographic operations using biometrics:

- `requestBiometricsForEncryption(activity, key, data, callback)`: Starts an encryption process using biometrics.
- `requestBiometricsForDecryption(activity, key, callback)`: Starts a decryption process using biometrics.
- `encryptAndStoreBiometricData(context, authResult, data)`: Encrypts and securely stores data.

### NormalBiometricHelper

Handles basic biometric authentication:

- `isBiometricsAvailable(context)`: Checks if biometric authentication is available and enabled on the device.
- `requestNormalBiometrics(activity, callback)`: Begins a biometric authentication process.

## Usage Examples

### Normal Biometric Authentication

```kotlin
if (NormalBiometricHelper.isBiometricsAvailable(context)) {
    NormalBiometricHelper.requestNormalBiometrics(activity, object : BiometricVerificationCallback {
        override fun onSuccess(result: String?) {
            // Handle success
        }

        override fun onFailure(isLock: Boolean) {
            // Handle failure
        }

        override fun onError(error: String) {
            // Handle error
        }
    })
}
```

### Cryptographic Biometric Authentication

#### Encrypting Data

```kotlin
CryptoBiometricsHelper.requestBiometricsForEncryption(activity, "key_example", "data_to_encrypt", object : BiometricVerificationCallback {
    override fun onSuccess(result: String?) {
        // Data encrypted successfully
    }

    override fun onFailure(isLock: Boolean) {
        // Handle failure
    }

    override fun onError(error: String) {
        // Handle error
    }
})
```

#### Encrypting Data

```kotlin
CryptoBiometricsHelper.requestBiometricsForDecryption(activity, "key_example", object : BiometricVerificationCallback {
    override fun onSuccess(result: String?) {
        // Data decrypted successfully
    }

    override fun onFailure(isLock: Boolean) {
        // Handle failure
    }

    override fun onError(error: String) {
        // Handle error
    }
})
```

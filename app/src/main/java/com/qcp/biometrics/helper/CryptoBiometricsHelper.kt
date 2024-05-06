package com.qcp.biometrics.helper

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.qcp.biometrics.helper.callback.BiometricVerificationCallback
import com.qcp.biometrics.helper.crypto.CiphertextWrapper
import com.qcp.biometrics.helper.crypto.CryptographyManager
/**
 * @author chienpham
 * @since 06/05/2024
 */

/**
 * Provides methods to perform cryptographic operations using biometric authentication.
 *
 * This class manages the encryption and decryption of data using biometrically authenticated sessions, handling key management and data storage securely.
 */
object CryptoBiometricsHelper {

    // Constants for shared preferences storage
    private const val SHARED_PREFS_FILENAME = "biometric_prefs"
    private const val CIPHERTEXT_WRAPPER = "ciphertext_wrapper"
    private const val SECRET_KEY_NAME = "biometric_sample_encryption_key"

    // CryptographyManager to handle all cryptographic operations
    private lateinit var cryptographyManager: CryptographyManager
    // Stores encrypted data details
    private var ciphertextWrapper: CiphertextWrapper? = null

    // Current cryptographic session key
    private var key: String? = null

    /**
     * Requests biometric authentication for the purpose of encrypting data.
     * @param activity The activity context used for managing biometric prompt lifecycle.
     * @param key The key used for identifying the cryptographic session.
     * @param data The data to be encrypted upon successful authentication.
     * @param callback Callback interface to handle the results of the encryption attempt.
     */
    fun requestBiometricsForEncryption(
        activity: FragmentActivity,
        key: String,
        data: String,
        callback: BiometricVerificationCallback
    ) {
        initBiometricIfNeeded(activity, key)
        val canAuthenticate = BiometricManager.from(activity)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val cipher = cryptographyManager.getInitializedCipherForEncryption(SECRET_KEY_NAME)

            val biometricPrompt = createBiometricPrompt(
                activity = activity,
                data = data,
                callback = callback
            )
            val promptInfo = createPromptInfo(activity)

            activity.runOnUiThread {
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
        }
    }

    /**
     * Requests biometric authentication for decrypting data previously encrypted.
     * @param activity The activity context used for managing biometric prompt lifecycle.
     * @param key The key used for identifying the cryptographic session.
     * @param callback Callback interface to handle the results of the decryption attempt.
     */
    fun requestBiometricsForDecryption(
        activity: FragmentActivity,
        key: String,
        callback: BiometricVerificationCallback
    ) {
        initBiometricIfNeeded(activity, key)

        ciphertextWrapper?.let { textWrapper ->
            val cipher = cryptographyManager.getInitializedCipherForDecryption(
                SECRET_KEY_NAME, textWrapper.initializationVector
            )
            val biometricPrompt = createBiometricPrompt(
                activity = activity,
                callback = callback
            )
            val promptInfo = createPromptInfo(activity)

            activity.runOnUiThread {
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
        }
    }

    /**
     * Decrypts data after successful biometric authentication.
     * @param authResult The authentication result containing the crypto object.
     * @return The decrypted string or an empty string if decryption fails.
     */
    private fun decryptBiometricDataFromStorage(
        authResult: BiometricPrompt.AuthenticationResult
    ): String {
        ciphertextWrapper?.let { textWrapper ->
            authResult.cryptoObject?.cipher?.let {
                val plaintext = cryptographyManager.decryptData(textWrapper.ciphertext, it)
                if (plaintext == "") {
                    return ""
                } else {
                    return plaintext
                }
            }
        } ?: return ""
    }

    // Creates a biometric prompt with necessary callbacks for handling authentication results
    private fun createBiometricPrompt(
        activity: FragmentActivity,
        data: String? = null,
        callback: BiometricVerificationCallback
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)
        var isVerifyFailed = false

        val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationFailed() {
                isVerifyFailed = true
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                when (errorCode) {
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_USER_CANCELED -> {
                        callback.onFailure()
                    }

                    BiometricPrompt.ERROR_LOCKOUT,
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                        if (!isVerifyFailed) {
                            callback.onFailure(isLock = true)
                        } else {
                            callback.onError(errString.toString())
                        }
                    }

                    else -> {
                        if (isVerifyFailed) {
                            callback.onFailure()
                        } else {
                            callback.onError(errString.toString())
                        }
                    }
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                if (data != null) {
                    encryptAndStoreBiometricData(
                        context = activity,
                        authResult = result,
                        data = data
                    )
                    callback.onSuccess()
                } else {
                    callback.onSuccess(decryptBiometricDataFromStorage(result))
                }
            }
        }
        return BiometricPrompt(activity, executor, authenticationCallback)
    }

    /**
     * Encrypts data and stores it securely using SharedPreferences.
     * @param context The context for accessing SharedPreferences.
     * @param authResult The result containing the encryption cipher.
     * @param data The data to encrypt.
     */
    private fun encryptAndStoreBiometricData(
        context: Context,
        authResult: BiometricPrompt.AuthenticationResult,
        data: String
    ) {
        authResult.cryptoObject?.cipher?.apply {
            cryptographyManager.clearToSharedPrefs(
                context,
                "${SHARED_PREFS_FILENAME}_$key",
                Context.MODE_PRIVATE,
                CIPHERTEXT_WRAPPER
            )
            val encryptedServerTokenWrapper =
                cryptographyManager.encryptData(data, this)
            cryptographyManager.persistCiphertextWrapperToSharedPrefs(
                encryptedServerTokenWrapper,
                context,
                "${SHARED_PREFS_FILENAME}_$key",
                Context.MODE_PRIVATE,
                CIPHERTEXT_WRAPPER
            )
            ciphertextWrapper = cryptographyManager.getCiphertextWrapperFromSharedPrefs(
                context,
                "${SHARED_PREFS_FILENAME}_$key",
                Context.MODE_PRIVATE,
                CIPHERTEXT_WRAPPER
            )
            if (ciphertextWrapper == null) {
                ciphertextWrapper = cryptographyManager.getCiphertextWrapperFromSharedPrefs(
                    context,
                    "${SHARED_PREFS_FILENAME}_$key",
                    Context.MODE_PRIVATE,
                    CIPHERTEXT_WRAPPER
                )
            }
        }
    }

    private fun createPromptInfo(context: Context): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(context.getString(R.string.biometrics_title))
            setSubtitle(context.getString(R.string.biometrics_subtitle))
            setNegativeButtonText(context.getString(R.string.biometrics_negative_button_text))
            setDescription(context.getString(R.string.biometrics_description))
            setConfirmationRequired(false)
        }.build()
    }

    private fun initBiometricIfNeeded(context: Context, key: String) {
        if (!this::cryptographyManager.isInitialized) {
            cryptographyManager = CryptographyManager()
        }
        if (key == this.key) {
            if (ciphertextWrapper == null) {
                ciphertextWrapper = cryptographyManager.getCiphertextWrapperFromSharedPrefs(
                    context,
                    "${SHARED_PREFS_FILENAME}_$key",
                    Context.MODE_PRIVATE,
                    CIPHERTEXT_WRAPPER
                )
            }
        } else {
            this.key = key
            ciphertextWrapper = cryptographyManager.getCiphertextWrapperFromSharedPrefs(
                context,
                "${SHARED_PREFS_FILENAME}_$key",
                Context.MODE_PRIVATE,
                CIPHERTEXT_WRAPPER
            )
        }
    }
}
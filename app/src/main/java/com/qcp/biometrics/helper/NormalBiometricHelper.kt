package com.qcp.biometrics.helper

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.qcp.biometrics.helper.callback.BiometricVerificationCallback
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * @author chienpham
 * @since 06/05/2024
 */

/**
 * Provides methods to facilitate basic biometric authentication.
 *
 * This class includes functions to check biometric capabilities and to initiate biometric authentication processes without involving cryptographic operations.
 */
object NormalBiometricHelper {
    // Executor to run biometric operations on a single background thread
    private val executor: Executor = Executors.newSingleThreadExecutor()

    /**
     * Checks if biometric authentication is available and enabled on the device.
     * @param context Context in which the availability is checked.
     * @return Boolean indicating if biometrics can be used for authentication.
     */
    fun isBiometricsAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Initiates a biometric authentication process using the provided activity context and callback.
     * @param activity The activity context used for managing biometric prompt lifecycle.
     * @param callback Callback interface to handle the authentication results.
     */
    fun requestNormalBiometrics(
        activity: FragmentActivity,
        callback: BiometricVerificationCallback
    ) {

        var isVerifyFailed = false // Flag to track if authentication explicitly failed
        val biometricPrompt =
            BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
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
                    super.onAuthenticationSucceeded(result)
                    callback.onSuccess()
                }

                override fun onAuthenticationFailed() {
                    isVerifyFailed = true
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.biometrics_title))
            .setSubtitle(activity.getString(R.string.biometrics_subtitle))
            .setNegativeButtonText(activity.getString(R.string.biometrics_negative_button_text))
            .setDescription(activity.getString(R.string.biometrics_description))
            .setConfirmationRequired(false)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
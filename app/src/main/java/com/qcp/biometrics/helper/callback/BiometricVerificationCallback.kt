package com.qcp.biometrics.helper.callback

/**
 * @author chienpham
 * @since 06/05/2024
 */

/**
 * Interface defining callbacks for biometric verification results.
 *
 * This interface manages the communication of the results from biometric operations including success, failure due to cancellation or lockout, and errors due to hardware or other issues.
 */
interface BiometricVerificationCallback {
    /**
     * Called when biometric verification is successfully completed.
     * @param result Optional result that may be provided after successful authentication.
     */
    fun onSuccess(result: String? = null)

    /**
     * Called when biometric verification fails or is cancelled.
     * @param isLock Indicates if the device is locked out of biometric attempts due to repeated failures.
     */
    fun onFailure(isLock: Boolean = false)

    /**
     * Called when an error occurs during the biometric process.
     * @param error Descriptive error string providing details about the error.
     */
    fun onError(error: String)
}
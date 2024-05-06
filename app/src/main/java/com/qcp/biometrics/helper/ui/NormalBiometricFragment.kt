package com.qcp.biometrics.helper.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.qcp.biometrics.helper.NormalBiometricHelper
import com.qcp.biometrics.helper.R
import com.qcp.biometrics.helper.base.BaseFragment
import com.qcp.biometrics.helper.callback.BiometricVerificationCallback
import com.qcp.biometrics.helper.databinding.FragmentNormalBiometricsBinding

/**
 * @author chienpham
 * @since 06/05/2024
 */
class NormalBiometricFragment : BaseFragment() {

    private val binding: FragmentNormalBiometricsBinding by lazy {
        FragmentNormalBiometricsBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpViews()
    }

    private fun setUpViews() {
        binding.btnRequestBiometrics.setOnClickListener {
            if (NormalBiometricHelper.isBiometricsAvailable(requireContext())) {
                requestBiometrics()
            } else {
                showMessage(getString(R.string.biometrics_device_not_available_error))
            }
        }
    }

    private fun requestBiometrics() {
        NormalBiometricHelper.requestNormalBiometrics(requireActivity(), object :
            BiometricVerificationCallback {
            override fun onSuccess(result: String?) {
                showMessage(getString(R.string.biometrics_success))
            }

            override fun onFailure(isLock: Boolean) {
                if (isLock) {
                    showMessage(getString(R.string.biometrics_lock_due_to_exceed_attempt_error))
                } else {
                    showMessage(getString(R.string.biometrics_verify_failed))
                }
            }

            override fun onError(error: String) {
                showMessage(getString(R.string.biometrics_error, error))
            }
        })
    }

    private fun showMessage(message: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun getName(): String {
        return context?.getString(R.string.fragment_normal_name) ?: "Normal Biometrics"
    }
}
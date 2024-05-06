package com.qcp.biometrics.helper.ui

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.qcp.biometrics.helper.CryptoBiometricsHelper
import com.qcp.biometrics.helper.R
import com.qcp.biometrics.helper.base.BaseFragment
import com.qcp.biometrics.helper.callback.BiometricVerificationCallback
import com.qcp.biometrics.helper.databinding.FragmentCryptoBiometricsBinding

/**
 * @author chienpham
 * @since 06/05/2024
 */
class CryptoObjectBiometricFragment : BaseFragment() {

    private val binding: FragmentCryptoBiometricsBinding by lazy {
        FragmentCryptoBiometricsBinding.inflate(layoutInflater)
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

        binding.btnEncryptBiometrics.setOnClickListener {
            showInputDialog()
        }

        binding.btnDecryptBiometrics.setOnClickListener {
            if (binding.edtKey.text.isNullOrEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.key_must_not_empty),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                getDecryptedData()
            }
        }
    }

    override fun getName(): String {
        return context?.getString(R.string.fragment_crypto_name) ?: "Crypto Biometrics"
    }

    private fun getDecryptedData() {
        CryptoBiometricsHelper.requestBiometricsForDecryption(
            activity = requireActivity(),
            key = binding.edtKey.text?.toString() ?: "",
            callback = object : BiometricVerificationCallback {
                override fun onSuccess(result: String?) {
                    showDataDialog(result ?: "")
                }

                override fun onFailure(isLock: Boolean) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.cancel),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(error: String) {
                    Toast.makeText(
                        requireContext(),
                        error,
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        )
    }

    private fun showInputDialog() {
        val inputEditText = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            hint = getString(R.string.enter_data)
        }

        AlertDialog.Builder(context)
            .setTitle(getString(R.string.input_data))
            .setView(inputEditText)
            .setPositiveButton(getString(R.string.submit)) { dialog, which ->
                val userInput = inputEditText.text.toString()
                handleUserInput(userInput)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
            .show()
    }

    private fun handleUserInput(data: String) {
        CryptoBiometricsHelper.requestBiometricsForEncryption(
            activity = requireActivity(),
            key = binding.edtKey.text?.toString() ?: "",
            data = data,
            callback = object : BiometricVerificationCallback {
                override fun onSuccess(result: String?) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.data_encrypted_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onFailure(isLock: Boolean) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.data_encrypted_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(error: String) {
                    Toast.makeText(
                        requireContext(),
                        error,
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        )
    }

    private fun showDataDialog(data: String) {
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.data))
            .setMessage(data)
            .setPositiveButton(getString(R.string.confirm), null)
            .create()
            .show()
    }
}
package com.rohitthebest.passwordsaver.ui.fragments

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.databinding.FragmentAppSetupBinding
import com.rohitthebest.passwordsaver.other.Constants.EDITTEXT_EMPTY_MESSAGE
import com.rohitthebest.passwordsaver.other.encryption.EncryptData
import com.rohitthebest.passwordsaver.ui.viewModels.AppSettingViewModel
import com.rohitthebest.passwordsaver.util.Functions
import com.rohitthebest.passwordsaver.util.Functions.Companion.checkBiometricSupport
import com.rohitthebest.passwordsaver.util.Functions.Companion.checkForFingerPrintValidation
import com.rohitthebest.passwordsaver.util.Functions.Companion.hideKeyBoard
import com.rohitthebest.passwordsaver.util.Functions.Companion.showToast
import com.rohitthebest.passwordsaver.util.hide
import com.rohitthebest.passwordsaver.util.show
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.random.Random

private const val TAG = "AppSetupFragment"

@SuppressLint("CheckResult")
@AndroidEntryPoint
class AppSetupFragment : Fragment(R.layout.fragment_app_setup), View.OnClickListener {

    private val appSettingViewModel: AppSettingViewModel by viewModels()

    private var _binding: FragmentAppSetupBinding? = null
    private val binding get() = _binding!!

    private var flag = true
    private var appSetting: AppSetting? = null

    private lateinit var securityQuestionsList: List<String>
    private var securityQuestion: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAppSetupBinding.bind(view)

        securityQuestionsList = ArrayList()

        securityQuestionsList = resources.getStringArray(R.array.security_questions).toList()

        securityQuestion = securityQuestionsList[0]

        setUpSecurityQuestionSpinner()

        getAppSettingData()
        initListeners()
        textWatcher()
    }

    private fun setUpSecurityQuestionSpinner() {

        binding.include.securityQuestionsSpinner
            .adapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            securityQuestionsList
        )

        binding.include.securityQuestionsSpinner.apply {

            onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                        setSelection(0)
                        securityQuestion = securityQuestionsList[0]
                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {

                        setSelection(position)
                        securityQuestion = securityQuestionsList[position]
                    }
                }
        }
    }

    private fun getAppSettingData() {

        Log.d(TAG, "getAppSettingData: ")

        appSettingViewModel.getAppSetting().observe(viewLifecycleOwner) { setting ->

            Log.d(TAG, "getAppSettingData: $appSetting")

            try {

                if (setting != null) {

                    appSetting = setting

                    binding.include.fingerprintCL.show()
                    binding.include.setupCL.hide()

                    if (flag) {

                        binding.appbar.hide()
                        if (checkBiometricSupport(requireActivity())) {

                            binding.include.fingerPrintAuthBtn.show()
                            binding.include.passwordAuthBtn.hide()

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                                checkFingerPrintValidation()
                            } else {

                                checkPasswordValidation()
                            }
                        } else {

                            binding.include.fingerPrintAuthBtn.hide()
                            binding.include.passwordAuthBtn.show()
                            checkPasswordValidation()
                        }
                    }

                } else {

                    binding.include.setupCL.show()
                    binding.include.fingerprintCL.hide()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun checkFingerPrintValidation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            checkForFingerPrintValidation(
                requireActivity(),
                authenticationCallback,
            ) {

                // On negative button clicked (Use password instead)
                checkPasswordValidation()
            }
        } else {

            checkPasswordValidation()
        }
    }

    private fun checkPasswordValidation() {

        Functions.checkForPasswordValidation(
            requireContext(),
            appSetting!!,
            "Forgot Password",
            {
                // on Success
                findNavController().navigate(R.id.action_appSetupFragment_to_homeFragment)
            }, {
                // onFailure
                showToast(requireContext(), "Password doesn't match!!!")
                checkPasswordValidation()
            },
            {
                // negative button - here on forgot password clicked
                askForSecurityAnswer()
            }
        )
    }

    private fun askForSecurityAnswer() {

        MaterialDialog(requireContext()).show {

            title(text = "Security Question")
            positiveButton(text = "Confirm")
            negativeButton(text = "Cancel") {

                checkPasswordValidation()
            }
            message(
                text = EncryptData().decryptAES(
                    appSetting?.securityQuestion,
                    appSetting?.secretKey
                )
            )
            input(hint = "Your answer here", allowEmpty = false) { _, inputString ->

                if (inputString.toString().trim()
                        .lowercase(Locale.ROOT) == EncryptData().decryptAES(
                        appSetting?.securityAnswer,
                        appSetting?.secretKey
                    )
                ) {

                    resetPassword()
                } else {

                    showToast(requireContext(), "Oops!! wrong answer")
                    checkPasswordValidation()
                }
            }
        }

    }

    private fun resetPassword() {

        MaterialDialog(requireContext()).show {

            title(text = "Enter new password")
            positiveButton(text = "Reset password")
            negativeButton(text = "Cancel") {

                checkPasswordValidation()
            }

            input(
                hint = "Enter the app password"
            ) { _, charSequence ->

                if (charSequence.toString().trim().isNotEmpty()) {

                    val encryptPassword =
                        EncryptData().encryptWithSHA(charSequence.toString().trim())
                    //flag = false

                    appSetting?.appPassword = encryptPassword

                    appSettingViewModel.insert(appSetting!!)

                    showToast(requireContext(), "Password changed")

                }
            }
        }
    }

    private val authenticationCallback: BiometricPrompt.AuthenticationCallback
        get() = @RequiresApi(Build.VERSION_CODES.P)
        object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                super.onAuthenticationError(errorCode, errString)

                showToast(requireContext(), "Authentication Failed")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                super.onAuthenticationSucceeded(result)

                findNavController().navigate(R.id.action_appSetupFragment_to_homeFragment)
            }
        }

    private fun textWatcher() {

        binding.include.passwordET.editText?.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s?.trim()?.isEmpty()!!) {

                    binding.include.passwordET.error = EDITTEXT_EMPTY_MESSAGE
                } else {

                    binding.include.passwordET.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}

        })

        binding.include.confirmPasswordET.editText?.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s?.trim()?.isEmpty()!!) {

                    binding.include.confirmPasswordET.error = EDITTEXT_EMPTY_MESSAGE
                } else if (binding.include.passwordET.editText?.text.toString()
                        .isNotEmpty()
                ) {

                    if (s.toString()
                            .trim() != binding.include.confirmPasswordET.editText?.text.toString()
                    ) {

                        binding.include.confirmPasswordET.error =
                            "It should be same as the password."
                    } else {

                        binding.include.confirmPasswordET.error = null
                    }
                } else {

                    binding.include.confirmPasswordET.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}

        })

        binding.include.securityAnswerET.editText?.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s?.trim()?.isEmpty()!!) {

                    binding.include.securityAnswerET.error = EDITTEXT_EMPTY_MESSAGE
                } else {

                    binding.include.securityAnswerET.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}

        })

    }

    private fun initListeners() {

        binding.nextBtn.setOnClickListener(this)
        binding.include.fingerPrintAuthBtn.setOnClickListener(this)
        binding.include.passwordAuthBtn.setOnClickListener(this)
        binding.nextBtn.setOnClickListener(this)
        //binding.include.fingerPrintCB.setOnCheckedChangeListener(this)

        if (requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {

            binding.include.fingerPrintCB.show()
        } else {

            binding.include.fingerPrintCB.hide()
        }
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.nextBtn.id -> {

                if (validateForm()) {

                    showProgressBar()

                    saveAppSettingToDatabase()
                }
            }

            binding.include.fingerPrintAuthBtn.id -> {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                    checkFingerPrintValidation()
                }
            }

            binding.include.passwordAuthBtn.id -> {

                checkPasswordValidation()
            }

        }
    }

    private fun saveAppSettingToDatabase() {

        val encryptedSecretKey = EncryptData().encryptWithSHA(
            "${UUID.randomUUID()}_${
                Random.nextLong(100, 9223372036854775).toString(16)
            }"
        )

        val encryptedAppPassword = EncryptData().encryptWithSHA(
            binding.include.passwordET.editText?.text.toString().trim()
        )

        Log.i(TAG, "saveAppSettingToDatabase: Secret key : $encryptedSecretKey")

        val encryptedSecurityQuestion =
            EncryptData().encryptWithAES(
                securityQuestion.lowercase(Locale.ROOT),
                encryptedSecretKey
            )!!

        val encryptedSecurityAnswer = EncryptData().encryptWithAES(
            binding.include.securityAnswerET.editText?.text.toString().trim()
                .lowercase(Locale.ROOT), encryptedSecretKey
        )!!

        val isFingerPrintEnabled =
            if (!requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {

                true
            } else {

                binding.include.fingerPrintCB.isChecked
            }

        val appSetting = AppSetting(
            encryptedAppPassword,
            encryptedSecretKey,
            encryptedSecurityQuestion,
            encryptedSecurityAnswer,
            true,
            isFingerPrintEnabled,
            UUID.randomUUID().toString()
        )

        flag = false
        appSettingViewModel.insert(appSetting)

        hideProgressBar()

        findNavController().navigate(R.id.action_appSetupFragment_to_homeFragment)
    }

    private fun validateForm(): Boolean {

        if (binding.include.passwordET.editText?.text.toString().trim().isEmpty()) {

            binding.include.passwordET.error = EDITTEXT_EMPTY_MESSAGE
            return false
        }

        if (binding.include.confirmPasswordET.editText?.text.toString().trim().isEmpty()) {

            binding.include.confirmPasswordET.error = EDITTEXT_EMPTY_MESSAGE
            return false
        }

        if ((binding.include.confirmPasswordET.editText?.text.toString().trim() !=
                    binding.include.passwordET.editText?.text.toString().trim())
        ) {

            binding.include.confirmPasswordET.error = "It doesn't match with the password!!!"
            return false
        }

        if (binding.include.securityAnswerET.editText?.text.toString().trim().isEmpty()) {

            binding.include.securityAnswerET.error = EDITTEXT_EMPTY_MESSAGE
            return false
        }

        return binding.include.passwordET.error == null
                && binding.include.confirmPasswordET.error == null
                && binding.include.securityAnswerET.error == null
                && (binding.include.confirmPasswordET.editText?.text.toString().trim() ==
                binding.include.passwordET.editText?.text.toString().trim())

    }

    private fun showProgressBar() {

        try {

            binding.progressBar.show()
            binding.nextBtn.isEnabled = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideProgressBar() {

        try {

            binding.progressBar.hide()
            binding.nextBtn.isEnabled = true
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()

        hideKeyBoard(requireActivity())

        _binding = null
    }
}
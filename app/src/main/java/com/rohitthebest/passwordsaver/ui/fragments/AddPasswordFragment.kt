package com.rohitthebest.passwordsaver.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.database.entity.Password
import com.rohitthebest.passwordsaver.databinding.FragmentAddPasswordBinding
import com.rohitthebest.passwordsaver.other.Constants.EDITTEXT_EMPTY_MESSAGE
import com.rohitthebest.passwordsaver.other.encryption.EncryptData
import com.rohitthebest.passwordsaver.ui.viewModels.AppSettingViewModel
import com.rohitthebest.passwordsaver.ui.viewModels.PasswordViewModel
import com.rohitthebest.passwordsaver.util.CheckPasswordPattern
import com.rohitthebest.passwordsaver.util.Functions.Companion.hideKeyBoard
import com.rohitthebest.passwordsaver.util.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

private const val TAG = "AddPasswordFragment"

@AndroidEntryPoint
class AddPasswordFragment : Fragment(R.layout.fragment_add_password), View.OnClickListener {

    private val appSettingViewModel: AppSettingViewModel by viewModels()
    private val passwordViewModel: PasswordViewModel by viewModels()

    private var _binding: FragmentAddPasswordBinding? = null
    private val binding get() = _binding!!

    private var appSetting: AppSetting? = null

    private var isForEditing = false
    private lateinit var receivedPassword: Password

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAddPasswordBinding.bind(view)

        getAppSetting()

        initListeners()
        textWatcher()
    }

    private fun getAppSetting() {

        try {

            appSettingViewModel.getAppSetting().observe(viewLifecycleOwner) { setting ->

                appSetting = setting

                getMessage()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun getMessage() {

        try {
            if (!arguments?.isEmpty!!) {

                val args = arguments?.let {

                    AddPasswordFragmentArgs.fromBundle(it)
                }

                val message = args?.editPasswordMessage

                isForEditing = true

                getReceivedPassword(message)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun getReceivedPassword(passwordKey: String?) {

        passwordKey?.let {

            passwordViewModel.getPasswordByKey(passwordKey)
                .observe(viewLifecycleOwner) { password ->

                    receivedPassword = password
                    updateUI()
                }
        }
    }

    private fun updateUI() {

        binding.titleTV.text = getString(R.string.editPassword)

        if (::receivedPassword.isInitialized) {

            binding.userNameET.editText?.setText(receivedPassword.userName)
            binding.siteNameET.editText?.setText(receivedPassword.siteName)
            binding.siteLinkET.editText?.setText(receivedPassword.siteLink)

            Log.i(TAG, "updateUI encrypted password: ${receivedPassword.password}")
            Log.i(
                TAG, "updateUI: decrypted password : ${
                    EncryptData().decryptAES(
                        receivedPassword.password,
                        appSetting?.secretKey
                    )
                }"
            )
            try {
                binding.passwordET.editText?.setText(
                    EncryptData().decryptAES(
                        receivedPassword.password,
                        appSetting?.secretKey
                    )
                )
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

        }
    }

    private fun initListeners() {

        binding.backBtn.setOnClickListener(this)
        binding.saveBtn.setOnClickListener(this)
        binding.mainCL.setOnClickListener(this) // for closing keyboard
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.saveBtn.id -> {

                if (validateForm()) {

                    savePasswordToDatabase()
                }
            }

            binding.backBtn.id -> {

                requireActivity().onBackPressed()
            }

        }

        hideKeyBoard(requireActivity())
    }

    private fun savePasswordToDatabase() {

        val encryptedPassword =
            encryptPassword(binding.passwordET.editText?.text.toString().trim())

        if (isForEditing) {

            // update password

            receivedPassword.modified = System.currentTimeMillis()
            receivedPassword.siteName = binding.siteNameET.editText?.text.toString().trim()
            receivedPassword.userName = binding.userNameET.editText?.text.toString().trim()
            receivedPassword.password = encryptedPassword ?: ""
            receivedPassword.siteLink = binding.siteLinkET.editText?.text.toString().trim()

            passwordViewModel.update(receivedPassword)

        } else {

            // insert new password

            val password = Password(
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                binding.siteNameET.editText?.text.toString().trim(),
                binding.userNameET.editText?.text.toString().trim(),
                encryptedPassword ?: "",
                UUID.randomUUID().toString(),
                binding.siteLinkET.editText?.text.toString().trim()
            )

            passwordViewModel.insert(password)

        }

        requireActivity().onBackPressed()
        showToast(requireContext(), "Password Saved")
    }


    private fun encryptPassword(password: String): String? {

        return try {

            EncryptData().encryptWithAES(password, appSetting?.secretKey)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun validateForm(): Boolean {

        if (binding.userNameET.editText?.text.toString().trim().isEmpty()) {

            binding.userNameET.error = EDITTEXT_EMPTY_MESSAGE
            return false
        }

        if (binding.passwordET.editText?.text.toString().trim().isEmpty()) {

            binding.passwordET.error = EDITTEXT_EMPTY_MESSAGE
            return false
        }

        return binding.userNameET.error == null &&
                binding.passwordET.error == null
    }

    private fun textWatcher() {

        binding.userNameET.editText?.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(str: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (str?.isEmpty()!!) {

                    binding.userNameET.error = EDITTEXT_EMPTY_MESSAGE
                } else {

                    binding.userNameET.error = null
                }

            }
        })

        binding.passwordET.editText?.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(str: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (str?.isEmpty()!!) {

                    binding.passwordET.error = EDITTEXT_EMPTY_MESSAGE
                } else {

                    checkPasswordStrength(str.toString())
                    binding.passwordET.error = null
                }
            }
        })
    }

    private fun checkPasswordStrength(passwordString: String) {

        if (CheckPasswordPattern().check_for_a_capital_letter(passwordString) &&
            CheckPasswordPattern().check_for_a_small_letter(passwordString)
        ) {

            binding.uppercaseLowercaseIndicator.setImageResource(R.drawable.ic_baseline_check_24)
        } else {

            binding.uppercaseLowercaseIndicator.setImageResource(R.drawable.ic_baseline_close_24)

        }

        if (CheckPasswordPattern().check_for_min_length(passwordString)) {

            binding.atleast8CharacterIndicator.setImageResource(R.drawable.ic_baseline_check_24)

        } else {

            binding.atleast8CharacterIndicator.setImageResource(R.drawable.ic_baseline_close_24)

        }

        if (CheckPasswordPattern().check_for_special_character(passwordString)) {

            binding.specialCharacterIndicator.setImageResource(R.drawable.ic_baseline_check_24)

        } else {

            binding.specialCharacterIndicator.setImageResource(R.drawable.ic_baseline_close_24)

        }

        if (CheckPasswordPattern().check_for_digit(passwordString)) {

            binding.aDigitIndicator.setImageResource(R.drawable.ic_baseline_check_24)
        } else {

            binding.aDigitIndicator.setImageResource(R.drawable.ic_baseline_close_24)

        }

        if (passwordString.contains("<") || passwordString.contains(">")) {

            binding.dontuseIndicator.setImageResource(R.drawable.ic_baseline_close_24)
        } else {

            binding.dontuseIndicator.setImageResource(R.drawable.ic_baseline_check_24)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()

        //closing keyboard if opened
        try {

            hideKeyBoard(requireActivity())

        } catch (e: Exception) {
            e.printStackTrace()
        }

        _binding = null
    }
}

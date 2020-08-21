package com.rohitthebest.passwordsaver.ui.fragments

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.databinding.DialogChangePasswordBinding
import com.rohitthebest.passwordsaver.other.Constants.EDITTEXT_EMPTY_MESSAGE
import com.rohitthebest.passwordsaver.other.Constants.TARGET_FRAGMENT_REQUEST_CODE
import com.rohitthebest.passwordsaver.other.Functions.Companion.showToast
import com.rohitthebest.passwordsaver.other.encryption.EncryptData
import com.rohitthebest.passwordsaver.ui.viewModels.AppSettingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangeAppPasswordDialog : DialogFragment(), View.OnClickListener {

    private val appSettingViewModel: AppSettingViewModel by viewModels()

    private var _binding: DialogChangePasswordBinding? = null
    private val binding get() = _binding!!

    private var appSetting: AppSetting? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = DialogChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getAppSettings()
        textWatcher()
        initListeners()
    }

    fun getInstance(): ChangeAppPasswordDialog {

        return ChangeAppPasswordDialog()
    }


    private fun initListeners() {

        binding.changePasswordBtn.setOnClickListener(this)
    }

    private fun getAppSettings() {

        try {

            appSettingViewModel.getAppSettingByID().observe(viewLifecycleOwner, Observer {

                if (it.isNotEmpty()) {

                    appSetting = it[0]
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(v: View?) {

        if (v?.id == binding.changePasswordBtn.id) {

            if (validateForm()) {

                try {

                    val oldPassword = EncryptData().encryptWithSHA(
                        binding.oldPasswordET.editText?.text.toString().trim()
                    )
                    val newPassword = EncryptData().encryptWithSHA(
                        binding.newPasswordET.editText?.text.toString().trim()
                    )

                    if (oldPassword == appSetting?.appPassword) {
                        if (newPassword != oldPassword) {

                            sendMessage(newPassword)
                        } else {
                            showToast(
                                requireContext(),
                                "New Password cannot be same as old password!!!"
                            )
                            dismiss()
                        }
                    } else {
                        
                        showToast(requireContext(), "Password was incorrect!!!")
                        dismiss()
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    private fun sendMessage(message: String) {

        try {
            if (targetFragment == null)
                return

            val intent = SettingsFragment().newIntent(message)
            targetFragment?.onActivityResult(
                TARGET_FRAGMENT_REQUEST_CODE,
                Activity.RESULT_OK,
                intent
            )

            dismiss()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            showToast(requireContext(), "Something went wrong!!!")
        }
    }

    private fun validateForm(): Boolean {

        if (binding.oldPasswordET.editText?.text.toString().trim().isEmpty()) {

            binding.oldPasswordET.error = EDITTEXT_EMPTY_MESSAGE
            return false
        }

        if (binding.newPasswordET.editText?.text.toString().trim().isEmpty()) {

            binding.newPasswordET.error = EDITTEXT_EMPTY_MESSAGE
            return false
        }

        if (binding.confirmNewPasswordET.editText?.text.toString().trim().isEmpty()) {

            binding.confirmNewPasswordET.error = EDITTEXT_EMPTY_MESSAGE
            return false
        }

        return binding.oldPasswordET.error == null &&
                binding.newPasswordET.error == null &&
                binding.confirmNewPasswordET.error == null
    }

    private fun textWatcher() {

        binding.oldPasswordET.editText?.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(str: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (str?.isEmpty()!!) {

                    binding.oldPasswordET.error = EDITTEXT_EMPTY_MESSAGE
                } else {

                    binding.oldPasswordET.error = null
                }

            }
        })

        binding.newPasswordET.editText?.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(str: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (str?.isEmpty()!!) {

                    binding.newPasswordET.error = EDITTEXT_EMPTY_MESSAGE
                } else {

                    binding.newPasswordET.error = null
                }
            }
        })

        binding.confirmNewPasswordET.editText?.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(str: CharSequence?, p1: Int, p2: Int, p3: Int) {

                when {
                    str?.isEmpty()!! -> {

                        binding.confirmNewPasswordET.error = EDITTEXT_EMPTY_MESSAGE
                    }
                    str.toString().trim() != binding.newPasswordET.editText?.text.toString()
                        .trim() -> {

                        binding.confirmNewPasswordET.error =
                            "Password doesn't match with new password."
                    }
                    else -> {

                        binding.confirmNewPasswordET.error = null
                    }
                }

            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}
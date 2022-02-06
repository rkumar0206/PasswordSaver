package com.rohitthebest.passwordsaver.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.textfield.TextInputLayout
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.database.entity.Password
import com.rohitthebest.passwordsaver.databinding.FragmentSettingsBinding
import com.rohitthebest.passwordsaver.databinding.SettingsFragmentLayoutBinding
import com.rohitthebest.passwordsaver.other.Constants.EDITTEXT_EMPTY_MESSAGE
import com.rohitthebest.passwordsaver.other.encryption.EncryptData
import com.rohitthebest.passwordsaver.ui.viewModels.AppSettingViewModel
import com.rohitthebest.passwordsaver.ui.viewModels.PasswordViewModel
import com.rohitthebest.passwordsaver.util.Functions.Companion.showToast
import com.rohitthebest.passwordsaver.util.onTextChangedListener
import dagger.hilt.android.AndroidEntryPoint

//private const val TAG = "SettingsFragment"

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings), View.OnClickListener {

    private val appSettingViewModel: AppSettingViewModel by viewModels()
    private val passwordViewModel: PasswordViewModel by viewModels()

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var includeBinding: SettingsFragmentLayoutBinding

    private lateinit var appSetting: AppSetting
    private var savedPasswordList: List<Password> = emptyList()

    private var appPassword = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSettingsBinding.bind(view)

        includeBinding = binding.include

        initListeners()
        getAppSetting()
        getSavedPasswordList()
    }

    private fun getAppSetting() {

        try {

            appSettingViewModel.getAppSetting().observe(viewLifecycleOwner) { setting ->

                if (setting != null) {

                    appSetting = setting
                    updateUI()
                } else {

                    showToast(requireContext(), "Something went wrong!!")
                    requireActivity().onBackPressed()
                }

            }

        } catch (e: Exception) {

            e.printStackTrace()
        }
     }

    private fun getSavedPasswordList() {

        try {

            passwordViewModel.getAllPasswordsList().observe(viewLifecycleOwner) { passwords ->

                savedPasswordList = passwords
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
     }

     private fun updateUI() {

         if (::appSetting.isInitialized) {

             includeBinding.deleteCB.isChecked = appSetting.isPasswordRequiredForDeleting
             includeBinding.fingerprintCB.isChecked = appSetting.isFingerprintEnabled
             appPassword = appSetting.appPassword
         }
     }

     private fun initListeners() {

         binding.backBtn.setOnClickListener(this)
         binding.saveBtn.setOnClickListener(this)
         includeBinding.changePasswordIB.setOnClickListener(this)
         includeBinding.changePasswordTV.setOnClickListener(this)
     }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.saveBtn.id -> {

                saveAppSettingChanges()
            }
            binding.backBtn.id -> {
                requireActivity().onBackPressed()
            }
        }

        if (v?.id == includeBinding.changePasswordIB.id || v?.id == includeBinding.changePasswordTV.id) {

            openBottomSheetForChangingPassword()
        }
    }

    private fun saveAppSettingChanges() {

        appSetting.appPassword = appPassword
        appSetting.isFingerprintEnabled = includeBinding.fingerprintCB.isChecked
        appSetting.isPasswordRequiredForDeleting = includeBinding.deleteCB.isChecked

        appSettingViewModel.insert(appSetting)

        showToast(requireContext(), "Settings saved")
        requireActivity().onBackPressed()

    }

    private fun openBottomSheetForChangingPassword() {

        MaterialDialog(requireContext(), BottomSheet()).show {

            title(text = "Password change")

            customView(
                R.layout.dialog_change_password,
                scrollable = true
            )

            textWatcher(getCustomView())

            getCustomView().findViewById<Button>(R.id.changePasswordBtn).setOnClickListener {

                if (validateForm(getCustomView())) {

                    appPassword = EncryptData().encryptWithSHA(
                        getCustomView().findViewById<TextInputLayout>(R.id.newPasswordET).editText?.text.toString()
                            .trim()
                    )

                    showToast(
                        requireContext(),
                        "Please press save button for saving changes.",
                        Toast.LENGTH_LONG
                    )

                    dismiss()
                }
            }

        }
    }

    private fun validateForm(customView: View): Boolean {

        val oldPasswordET = customView.findViewById<TextInputLayout>(R.id.oldPasswordET)
        val newPasswordET = customView.findViewById<TextInputLayout>(R.id.newPasswordET)
        val confirmNewPasswordET =
            customView.findViewById<TextInputLayout>(R.id.confirmNewPasswordET)

        if (oldPasswordET.editText?.text.toString().trim().isEmpty()) {

            oldPasswordET.error = EDITTEXT_EMPTY_MESSAGE
            return false
        }

        if (newPasswordET.editText?.text.toString().trim().isEmpty()) {

            newPasswordET.error = EDITTEXT_EMPTY_MESSAGE
            return false
        }

        if (confirmNewPasswordET.editText?.text.toString().trim().isEmpty()) {

            confirmNewPasswordET.error = EDITTEXT_EMPTY_MESSAGE
            return false
        }

        if (confirmNewPasswordET.editText?.text.toString().trim()
            != newPasswordET.editText?.text.toString().trim()
        ) {

            confirmNewPasswordET.error = "It should match with the password written above"
            return false
        }

        val encryptedPassword =
            EncryptData().encryptWithSHA(oldPasswordET.editText?.text.toString().trim())

        if (encryptedPassword != appSetting.appPassword) {

            oldPasswordET.error = "Password incorrect!!!"
            return false
        }

        return oldPasswordET.error == null
                && newPasswordET.error == null
                && confirmNewPasswordET.error == null
    }

    private fun textWatcher(customView: View) {

        val oldPasswordET = customView.findViewById<TextInputLayout>(R.id.oldPasswordET)
        val newPasswordET = customView.findViewById<TextInputLayout>(R.id.newPasswordET)
        val confirmNewPasswordET =
            customView.findViewById<TextInputLayout>(R.id.confirmNewPasswordET)

        oldPasswordET.editText?.onTextChangedListener { s ->

            if (s?.trim()?.isEmpty()!!) {

                oldPasswordET.error = EDITTEXT_EMPTY_MESSAGE
            } else {

                oldPasswordET.error = null
            }

        }

        newPasswordET.editText?.addTextChangedListener { s ->

            if (s?.trim()?.isEmpty()!!) {

                newPasswordET.error = EDITTEXT_EMPTY_MESSAGE
            } else {

                newPasswordET.error = null
            }

        }

        confirmNewPasswordET.editText?.addTextChangedListener { s ->

            if (s?.trim()?.isEmpty()!!) {

                confirmNewPasswordET.error = EDITTEXT_EMPTY_MESSAGE
            } else {

                confirmNewPasswordET.error = null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}
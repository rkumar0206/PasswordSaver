package com.rohitthebest.passwordsaver.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.database.entity.Password
import com.rohitthebest.passwordsaver.databinding.FragmentAddPasswordBinding
import com.rohitthebest.passwordsaver.other.Constants.EDITTEXT_EMPTY_MESSAGE
import com.rohitthebest.passwordsaver.other.Constants.NOT_SYNCED
import com.rohitthebest.passwordsaver.other.Constants.OFFLINE
import com.rohitthebest.passwordsaver.other.Constants.SYNCED
import com.rohitthebest.passwordsaver.other.encryption.EncryptData
import com.rohitthebest.passwordsaver.ui.viewModels.AppSettingViewModel
import com.rohitthebest.passwordsaver.ui.viewModels.PasswordViewModel
import com.rohitthebest.passwordsaver.util.CheckPasswordPattern
import com.rohitthebest.passwordsaver.util.ConversionWithGson.Companion.convertFromJsonToPassword
import com.rohitthebest.passwordsaver.util.ConversionWithGson.Companion.convertPasswordToJson
import com.rohitthebest.passwordsaver.util.FirebaseServiceHelper.Companion.uploadDocumentToFireStore
import com.rohitthebest.passwordsaver.util.Functions.Companion.hideKeyBoard
import com.rohitthebest.passwordsaver.util.Functions.Companion.isInternetAvailable
import com.rohitthebest.passwordsaver.util.Functions.Companion.showToast
import com.rohitthebest.passwordsaver.util.Functions.Companion.toStringM
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random

@AndroidEntryPoint
class AddPasswordFragment : Fragment(R.layout.fragment_add_password), View.OnClickListener {

    private val appSettingViewModel: AppSettingViewModel by viewModels()
    private val passwordViewModel: PasswordViewModel by viewModels()

    private var _binding: FragmentAddPasswordBinding? = null
    private val binding get() = _binding!!

    private var appSetting: AppSetting? = null

    private var isForEditing = false
    private var receivedPassword: Password? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAddPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getAppSetting()

        getMessage()

        initListeners()
        textWatcher()
    }

    private fun getMessage() {

        try {
            if (!arguments?.isEmpty!!) {

                val args = arguments?.let {

                    AddPasswordFragmentArgs.fromBundle(it)
                }

                val message = args?.editPasswordMessage

                receivedPassword = convertFromJsonToPassword(message)

                Log.i("AddPasswordFrag", "${receivedPassword?.password}")
                Log.i("AddPasswordFrag", "${receivedPassword?.userName}")

                isForEditing = true

                updateUI()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun getAppSetting() {

        try {

            appSettingViewModel.getAppSetting().observe(viewLifecycleOwner, Observer {

                appSetting = it
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateUI() {

        receivedPassword?.let {

            binding.titleTV.text = getString(R.string.editPassword)

            binding.userNameET.editText?.setText(it.userName)
            binding.siteNameET.editText?.setText(it.siteName)
            try {
                binding.passwordET.editText?.setText(
                    EncryptData().decryptAES(
                        it.password,
                        appSetting?.appPassword
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

                    if (isForEditing) {

                        updatePassword(receivedPassword)
                    } else {

                        insertToDatabase()
                    }
                }
            }

            binding.backBtn.id -> {

                requireActivity().onBackPressed()
            }

        }

        hideKeyBoard(requireActivity())
    }

    private fun insertToDatabase() {

        val encryptedPassword: String? =
            encryptPassword(binding.passwordET.editText?.text.toString().trim())

        val password = Password()

        password.apply {

            siteName = if (binding.siteNameET.editText?.text.toString().trim().isNotEmpty()) {
                binding.siteNameET.editText?.text.toString().trim()
            } else {
                ""
            }
            userName = binding.userNameET.editText?.text.toString().trim()
            this.password = encryptedPassword
            uid = ""
            key = ""
            isSynced = NOT_SYNCED
            timeStamp = System.currentTimeMillis()
        }


        if (appSetting?.mode == OFFLINE) {

            passwordViewModel.insert(password)
            requireActivity().onBackPressed()
        } else {

            password.uid = appSetting?.uid

            password.key = "${System.currentTimeMillis().toStringM(69)}_${
                Random.nextLong(
                    100,
                    9223372036854775
                ).toStringM(69)
            }_${password.uid}"

            if (isInternetAvailable(requireContext())) {

                password.isSynced = SYNCED

                passwordViewModel.insert(password)

                //Uploading to firestore

                convertPasswordToJson(password)?.let { pass ->

                    uploadDocumentToFireStore(
                        requireContext(),
                        pass,
                        getString(R.string.savedPasswords),
                        password.key!!
                    )

                }

                requireActivity().onBackPressed()

            } else {

                password.isSynced = NOT_SYNCED
                passwordViewModel.insert(password)

                requireActivity().onBackPressed()
            }

        }

        showToast(requireContext(), "Password Saved")
    }

    private fun updatePassword(receivedPassword: Password?) {

        receivedPassword?.siteName = binding.siteNameET.editText?.text.toString().trim()
        receivedPassword?.userName = binding.userNameET.editText?.text.toString().trim()
        receivedPassword?.password =
            encryptPassword(binding.passwordET.editText?.text.toString().trim())

        if (receivedPassword?.uid == "" || receivedPassword?.isSynced == NOT_SYNCED) {

            passwordViewModel.insert(receivedPassword)

            showToast(requireContext(), "Password updated")

            requireActivity().onBackPressed()
        } else {

            if (isInternetAvailable(requireContext())) {

                receivedPassword?.let { passwordViewModel.insert(it) }

                uploadDocumentToFireStore(
                    requireContext(),
                    convertPasswordToJson(receivedPassword),
                    getString(R.string.savedPasswords),
                    receivedPassword?.key!!
                )

                showToast(requireContext(), "Password updated")
                requireActivity().onBackPressed()
            } else {

                showToast(
                    requireContext(),
                    "In Online mode you need to have an active internet connection for editing.",
                    Toast.LENGTH_LONG
                )
            }
        }
    }

    private fun encryptPassword(password: String): String? {

        return try {

            EncryptData().encryptWithAES(password, appSetting?.appPassword)
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

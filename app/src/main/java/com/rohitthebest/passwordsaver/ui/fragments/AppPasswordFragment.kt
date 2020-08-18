package com.rohitthebest.passwordsaver.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.databinding.FragmentAppPasswordBinding
import com.rohitthebest.passwordsaver.other.Constants.APP_SETTING_SERVICE_MESSAGE
import com.rohitthebest.passwordsaver.other.Constants.EDITTEXT_EMPTY_MESSAGE
import com.rohitthebest.passwordsaver.other.Constants.NO_INTERNET_MESSAGE
import com.rohitthebest.passwordsaver.other.Constants.OFFLINE
import com.rohitthebest.passwordsaver.other.Functions.Companion.closeKeyboard
import com.rohitthebest.passwordsaver.other.Functions.Companion.isInternetAvailable
import com.rohitthebest.passwordsaver.other.Functions.Companion.showToast
import com.rohitthebest.passwordsaver.other.encryption.EncryptData
import com.rohitthebest.passwordsaver.services.UploadAppSettingsService
import com.rohitthebest.passwordsaver.ui.viewModels.AppSettingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AppPasswordFragment : Fragment(), View.OnClickListener {

    private val viewModel: AppSettingViewModel by viewModels()

    private var _binding: FragmentAppPasswordBinding? = null
    private val binding get() = _binding!!

    private var appSetting: AppSetting? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAppPasswordBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getAppSettingData()
        initListeners()
        textWatcher()
    }

    private fun getAppSettingData() {

        viewModel.getAppSettingByID().observe(viewLifecycleOwner, Observer {

            try {

                if (it.isNotEmpty()) {

                    appSetting = if (it[0].appPassword != "") {

                        showEnterPasswordCL()
                        it[0]
                    } else {

                        showSetupPasswordCL()
                        it[0]
                    }
                } else {


                    findNavController().navigate(R.id.action_appPasswordFragment_to_introductionFragment)


                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

    }


    private fun initListeners() {

        binding.saveBtn.setOnClickListener(this)
        binding.setupPasswordCL.setOnClickListener(this)
        binding.enterPasswordCL.setOnClickListener(this)
        binding.confirmBtn.setOnClickListener(this)
    }


    override fun onClick(v: View?) {

        when (v?.id) {

            binding.saveBtn.id -> {

                if (validateForm()) {

                    saveInputToDataBase(binding.passwordET.editText?.text.toString().trim())
                }
            }

            binding.confirmBtn.id -> {

                if (binding.enterPasswordET.editText?.text.toString().trim().isNotBlank()) {

                    checkForPasswordMatch(binding.enterPasswordET.editText?.text.toString().trim())
                } else {

                    binding.enterPasswordET.error = EDITTEXT_EMPTY_MESSAGE
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {

            closeKeyboard(requireActivity())
        }
    }

    private fun saveInputToDataBase(password: String) {

        val encryptedPassword: String? = encryptPassword(password)

        if (encryptedPassword == "") {

            showToast(requireContext(), "Something went wrong!!!")
            requireActivity().onBackPressed()
        } else {

            appSetting?.let {

                if (it.mode == OFFLINE) {

                    it.appPassword = encryptedPassword
                    viewModel.insert(it)

                    showToast(requireContext(), "Saved")
                } else {

                    if (isInternetAvailable(requireContext())) {
                        it.appPassword = encryptedPassword
                        val gson = Gson()
                        val appsSettingMessage = gson.toJson(it)

                        val foreGroundServiceIntent =
                            Intent(requireContext(), UploadAppSettingsService::class.java)
                        foreGroundServiceIntent.putExtra(
                            APP_SETTING_SERVICE_MESSAGE,
                            appsSettingMessage
                        )

                        ContextCompat.startForegroundService(
                            requireContext(),
                            foreGroundServiceIntent
                        )

                        viewModel.insert(it)
                    } else {

                        showToast(requireContext(), NO_INTERNET_MESSAGE)
                    }

                    findNavController().navigate(R.id.action_appPasswordFragment_to_homeFragment)
                }
            }
        }
    }

    private fun encryptPassword(data: String): String? {

        return try {

            EncryptData().encryptWithSHA(data)

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun validateForm(): Boolean {

        if (binding.passwordET.editText?.text.toString().trim().isEmpty()) {

            binding.passwordET.error = EDITTEXT_EMPTY_MESSAGE
            return false
        }

        if (binding.confirmPasswordET.editText?.text.toString().trim().isEmpty()) {

            binding.confirmPasswordET.error = EDITTEXT_EMPTY_MESSAGE
            return false
        }

        return binding.passwordET.error == null
                && binding.confirmPasswordET.error == null
    }

    private fun checkForPasswordMatch(enteredPassword: String) {

        if (EncryptData().encryptWithSHA(enteredPassword) == appSetting?.appPassword ) {

            findNavController().navigate(R.id.action_appPasswordFragment_to_homeFragment)
        } else {

            binding.enterPasswordET.error = "Please check your password..."
        }
    }

    private fun textWatcher() {

        binding.passwordET.editText?.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(str: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (str?.isEmpty()!!) {

                    binding.passwordET.error = EDITTEXT_EMPTY_MESSAGE
                } else {
                    binding.passwordET.error = null
                }

            }
        })

        binding.confirmPasswordET.editText?.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(str: CharSequence?, p1: Int, p2: Int, p3: Int) {

                when {
                    str?.isEmpty()!! -> {

                        binding.confirmPasswordET.error = EDITTEXT_EMPTY_MESSAGE
                    }
                    str.toString().trim() != binding.passwordET.editText?.text.toString()
                        .trim() -> {

                        binding.confirmPasswordET.error = "Password doesn't match."
                    }
                    else -> {

                        binding.confirmPasswordET.error = null
                    }
                }
            }
        })

        binding.enterPasswordET.editText?.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(str: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (str?.isEmpty()!!) {

                    binding.enterPasswordET.error = EDITTEXT_EMPTY_MESSAGE
                } else {
                    binding.enterPasswordET.error = null
                }
            }
        })

    }

    private fun showSetupPasswordCL() {

        binding.setupPasswordCL.visibility = View.VISIBLE
        binding.enterPasswordCL.visibility = View.GONE

    }

    private fun showEnterPasswordCL() {

        binding.setupPasswordCL.visibility = View.GONE
        binding.enterPasswordCL.visibility = View.VISIBLE
    }


    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }


}
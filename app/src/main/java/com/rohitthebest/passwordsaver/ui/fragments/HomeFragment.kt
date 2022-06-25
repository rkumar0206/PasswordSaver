package com.rohitthebest.passwordsaver.ui.fragments

import android.annotation.SuppressLint
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.input.input
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.database.entity.Password
import com.rohitthebest.passwordsaver.databinding.FragmentHomeBinding
import com.rohitthebest.passwordsaver.other.encryption.EncryptData
import com.rohitthebest.passwordsaver.ui.adapters.SavedPasswordRVAdapter
import com.rohitthebest.passwordsaver.ui.viewModels.AppSettingViewModel
import com.rohitthebest.passwordsaver.ui.viewModels.PasswordViewModel
import com.rohitthebest.passwordsaver.util.*
import com.rohitthebest.passwordsaver.util.Functions.Companion.copyToClipBoard
import com.rohitthebest.passwordsaver.util.Functions.Companion.generatePasswordPdfDocumentAndExportToStorage
import com.rohitthebest.passwordsaver.util.Functions.Companion.hideKeyBoard
import com.rohitthebest.passwordsaver.util.Functions.Companion.openLinkInBrowser
import com.rohitthebest.passwordsaver.util.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "HomeFragment"

@SuppressLint("CheckResult")
@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), SavedPasswordRVAdapter.OnClickListener,
    View.OnClickListener {

    private val viewModel: AppSettingViewModel by viewModels()
    private val passwordViewModel: PasswordViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var appSetting: AppSetting? = null

    private lateinit var passwordRVAdapter: SavedPasswordRVAdapter

    private var passwrd: Password? = Password()
    private var passwordDialog: MaterialDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentHomeBinding.bind(view)

        passwordRVAdapter = SavedPasswordRVAdapter()

        getAppSetting()

        showProgressBar()

        setUpRecyclerView()

        lifecycleScope.launch {

            delay(350)
            getAllSavedPassword()
        }

        initListeners()

    }

    private fun getAppSetting() {

        viewModel.getAppSetting().observe(viewLifecycleOwner) {

            if (it != null) {

                appSetting = it
            }
        }
    }

    private fun getAllSavedPassword() {

        try {

            passwordViewModel.getAllPasswordsList().observe(viewLifecycleOwner) { passwords ->

                if (passwords.isNotEmpty()) {

                    hideNoPassTV()

                    setUpSearchView(passwords)
                } else {

                    showNoPassTV()
                }

                passwordRVAdapter.submitList(passwords)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setUpSearchView(passwords: List<Password>) {

        val searchView =
            binding.toolbar.menu.findItem(R.id.home_menu_search).actionView as SearchView

        searchView.searchText { newText ->

            newText?.let { newTxt ->

                if (newTxt.trim().isEmpty()) {

                    passwordRVAdapter.submitList(passwords)
                } else {

                    val filteredList = passwords.filter { passwrd ->

                        passwrd.userName.lowercase(Locale.ROOT)
                            .contains(newTxt.lowercase(Locale.ROOT)) ||
                                passwrd.siteName.lowercase(Locale.ROOT)
                                    .contains(newTxt.lowercase(Locale.ROOT))
                    }

                    passwordRVAdapter.submitList(filteredList)
                }
            }

        }
    }

    private fun setUpRecyclerView() {

        try {

            binding.savedPasswordRV.apply {

                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                adapter = passwordRVAdapter
                changeVisibilityOfFABOnScrolled(binding.addPasswordFAB)
            }
            passwordRVAdapter.setOnClickListener(this)

            hideProgressBar()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onItemClick(password: Password?) {

        passwrd = password

        isPasswordRequiredForDeleting = false

        if (appSetting?.isFingerprintEnabled == true) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                checkFingerPrintValidation()
            }
        } else {

            checkPasswordValidation()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun checkFingerPrintValidation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            Functions.checkForFingerPrintValidation(
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

    private var isPasswordRequiredForDeleting = false

    private fun checkPasswordValidation() {

        Functions.checkForPasswordValidation(
            requireContext(),
            appSetting!!,
            "Cancel",
            {
                // on Success

                if (isPasswordRequiredForDeleting) {

                    showDialogForDeletingPassword()
                    isPasswordRequiredForDeleting = false
                } else {

                    showPasswordInBottomSheet()
                }

            }, {
                // onFailure
                showToast(requireContext(), getString(R.string.passord_not_match))
                checkPasswordValidation()
            },
            {
                // negative button - cancel
                it.dismiss()
            }
        )
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

                showPasswordInBottomSheet()
            }
        }

    private fun showPasswordInBottomSheet() {

        val decryptedPassword: String? = try {
            EncryptData().decryptAES(
                passwrd?.password,
                appSetting?.secretKey
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }

        Log.i(TAG, "onAuthenticationSucceeded: password : ${passwrd?.password}")
        Log.i(TAG, "onAuthenticationSucceeded: decrypted password :  $decryptedPassword")

        /**
         * showing password information in bottomSheet
         */

        passwordDialog = MaterialDialog(requireContext(), BottomSheet()).show {

            title(text = "Your Password")
            customView(
                R.layout.show_password_bottomsheet_layout,
                scrollable = true
            )

            initializeTheFieldsOfBottomSheet(getCustomView(), decryptedPassword)

            if (passwrd?.siteLink == "") {

                getCustomView().findViewById<TextView>(R.id.siteLinkTV).hide()
            }

            getCustomView().findViewById<ImageButton>(R.id.editBtn).setOnClickListener {

                val action = HomeFragmentDirections.actionHomeFragmentToAddPasswordFragment(
                    passwrd?.key
                )

                findNavController().navigate(action)

                dismiss()
            }

            getCustomView().findViewById<ImageButton>(R.id.deleteBtn).setOnClickListener {

                if (appSetting?.isPasswordRequiredForDeleting == true) {

                    isPasswordRequiredForDeleting = true
                    checkPasswordValidation()

                } else {

                    showDialogForDeletingPassword()
                }
                dismiss()
            }

            getCustomView().findViewById<ImageButton>(R.id.siteNameCopyBtn)
                .setOnClickListener {

                    copyToClipBoard(requireActivity(), passwrd?.siteName.toString())
                }

            getCustomView().findViewById<ImageButton>(R.id.userNameCopyBtn)
                .setOnClickListener {

                    copyToClipBoard(requireActivity(), passwrd?.userName.toString())
                }
            getCustomView().findViewById<ImageButton>(R.id.passwordCopyBtn)
                .setOnClickListener {

                    copyToClipBoard(requireActivity(), decryptedPassword.toString())
                }

            getCustomView().findViewById<TextView>(R.id.siteLinkTV).setOnClickListener {

                if (passwrd?.siteLink != "") {

                    openLinkInBrowser(passwrd?.siteLink, requireContext())
                }
            }
        }
    }

    private fun showDialogForDeletingPassword() {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.are_you_sure))
            .setMessage(getString(R.string.after_deleting_will_lose_this_password))
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->

                deletePassword(passwrd!!)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->

                dialog.dismiss()
            }
            .create()
            .show()

    }

    private fun deletePassword(password: Password) {

        try {

            passwordViewModel.delete(password)

            binding.root.showSnackBarWithActionAndDismissListener(
                getString(R.string.password_deleted),
                getString(R.string.undo),
                {
                    passwordViewModel.insert(password)
                    showToast(requireContext(), getString(R.string.password_restored))
                },
                {}
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initializeTheFieldsOfBottomSheet(customView: View, decryptedPassword: String?) {

        customView.findViewById<TextView>(R.id.sitenameTV).text = if (passwrd?.siteName == "") {

            getString(R.string.no_site_name)
        } else {

            passwrd?.siteName
        }

        customView.findViewById<TextView>(R.id.usernameTV).text = passwrd?.userName

        customView.findViewById<TextView>(R.id.passwordTV).text = decryptedPassword

        customView.findViewById<TextView>(R.id.siteLinkTV).text = passwrd?.siteLink
    }

    private fun initListeners() {

        binding.addPasswordFAB.setOnClickListener(this)
        binding.homeFragCoordinatorLayout.setOnClickListener(this)

        binding.toolbar.menu.findItem(R.id.home_menu_settings).setOnMenuItemClickListener {

            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
            true
        }

        binding.toolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        binding.toolbar.menu.findItem(R.id.home_menu_password_generator)
            .setOnMenuItemClickListener {

                findNavController().navigate(R.id.action_homeFragment_to_passwordGeneratorFragment)
                true
            }

        binding.toolbar.menu.findItem(R.id.home_menu_export_password).setOnMenuItemClickListener {

            MaterialDialog(requireContext()).show {

                title(text = getString(R.string.password))
                positiveButton(text = getString(R.string.confirm))
                cancelOnTouchOutside(false)

                input(
                    hint = getString(R.string.enter_the_app_password),
                    inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD,
                    allowEmpty = false
                ) { _, inputString ->

                    val encryptPassword = EncryptData().encryptWithSHA(inputString.toString())

                    if (encryptPassword == appSetting?.appPassword) {

                        if (passwordRVAdapter.currentList.isNotEmpty()) {

                            lifecycleScope.launch {

                                generatePasswordPdfDocumentAndExportToStorage(
                                    requireActivity(),
                                    passwordRVAdapter.currentList,
                                    inputString.toString(),
                                    appSetting?.secretKey!!
                                )
                            }

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(getString(R.string.password_exported))
                                .setMessage(getString(R.string.password_exported_note))
                                .setPositiveButton("Ok") { dialog, _ ->

                                    dialog.dismiss()
                                }.create()
                                .show()


                        } else {

                            showToast(requireContext(), getString(R.string.no_passwords_added))
                        }

                    } else {
                        showToast(requireContext(), getString(R.string.passord_not_match))
                    }
                }
            }.negativeButton(text = getString(R.string.cancel)) {
                it.dismiss()
            }

            true
        }

    }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.addPasswordFAB.id -> {

                findNavController().navigate(R.id.action_homeFragment_to_addPasswordFragment)
            }
        }

        hideKeyBoard(requireActivity())
    }

    private fun showProgressBar() {

        try {
            binding.homeProgressBar.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun hideProgressBar() {

        try {

            binding.homeProgressBar.hide()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun showNoPassTV() {

        binding.noPassAddedTV.show()
        binding.savedPasswordRV.hide()
    }

    private fun hideNoPassTV() {

        binding.noPassAddedTV.hide()
        binding.savedPasswordRV.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        try {

            hideKeyBoard(requireActivity())

        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

        _binding = null

    }
}
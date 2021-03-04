package com.rohitthebest.passwordsaver.ui.fragments

import android.content.Context
import android.content.DialogInterface
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.input.input
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.database.entity.Password
import com.rohitthebest.passwordsaver.databinding.FragmentHomeBinding
import com.rohitthebest.passwordsaver.other.Constants.NOT_SYNCED
import com.rohitthebest.passwordsaver.other.Constants.OFFLINE
import com.rohitthebest.passwordsaver.other.Constants.ONLINE
import com.rohitthebest.passwordsaver.other.Constants.SHARED_PREFERENCE_KEY
import com.rohitthebest.passwordsaver.other.Constants.SHARED_PREFERENCE_NAME
import com.rohitthebest.passwordsaver.other.Constants.SYNCED
import com.rohitthebest.passwordsaver.other.encryption.EncryptData
import com.rohitthebest.passwordsaver.ui.adapters.SavedPasswordRVAdapter
import com.rohitthebest.passwordsaver.ui.viewModels.AppSettingViewModel
import com.rohitthebest.passwordsaver.ui.viewModels.PasswordViewModel
import com.rohitthebest.passwordsaver.util.ConversionWithGson.Companion.convertPasswordToJson
import com.rohitthebest.passwordsaver.util.FirebaseServiceHelper
import com.rohitthebest.passwordsaver.util.FirebaseServiceHelper.Companion.deleteDocumentFromFireStore
import com.rohitthebest.passwordsaver.util.Functions.Companion.closeKeyboard
import com.rohitthebest.passwordsaver.util.Functions.Companion.copyToClipBoard
import com.rohitthebest.passwordsaver.util.Functions.Companion.getUid
import com.rohitthebest.passwordsaver.util.Functions.Companion.hide
import com.rohitthebest.passwordsaver.util.Functions.Companion.hideKeyBoard
import com.rohitthebest.passwordsaver.util.Functions.Companion.isInternetAvailable
import com.rohitthebest.passwordsaver.util.Functions.Companion.openLinkInBrowser
import com.rohitthebest.passwordsaver.util.Functions.Companion.show
import com.rohitthebest.passwordsaver.util.Functions.Companion.showKeyboard
import com.rohitthebest.passwordsaver.util.Functions.Companion.showNoInternetMessage
import com.rohitthebest.passwordsaver.util.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), SavedPasswordRVAdapter.OnClickListener,
    View.OnClickListener {

    private val TAG = "HomeFragment"

    private val viewModel: AppSettingViewModel by viewModels()
    private val passwordViewModel: PasswordViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var appSetting: AppSetting? = null

    private lateinit var mAdapter: SavedPasswordRVAdapter

    private var cancellationSignal: CancellationSignal? = null

    private var isSearchViewVisible = false

    private var passwrd: Password? = Password()

    private var isSyncedFromCloud = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = SavedPasswordRVAdapter()

        loadData()

        if (!isSyncedFromCloud) {

            syncPasswordFromCloud()
        }

        getAppSetting()

        showProgressBar()
        GlobalScope.launch {

            delay(350)
            withContext(Dispatchers.Main) {

                getAllSavedPassword()
            }
        }

        initListeners()
    }

    private fun syncPasswordFromCloud() {

        showProgressBar()

        if (appSetting?.mode != OFFLINE) {

            if (isInternetAvailable(requireContext())) {

                FirebaseFirestore.getInstance()
                    .collection(getString(R.string.savedPasswords))
                    .whereEqualTo("uid", getUid())
                    .get()
                    .addOnSuccessListener {

                        if (!it.isEmpty) {

                            for (p in it) {

                                val password = p.toObject(Password::class.java)

                                passwordViewModel.insert(password)
                            }
                        }

                        isSyncedFromCloud = true

                        saveData()

                        hideProgressBar()
                    }
                    .addOnFailureListener {

                        showToast(requireContext(), it.toString())
                        hideProgressBar()
                    }

            } else {

                showNoInternetMessage(requireContext())
            }
        } else {

            isSyncedFromCloud = true
            saveData()
        }
    }

    private fun saveData() {

        val sharedPreference =
            requireActivity().getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)

        val editor = sharedPreference.edit()

        editor.putBoolean(SHARED_PREFERENCE_KEY, isSyncedFromCloud)

        editor.apply()
    }

    private fun loadData() {

        val sharedPreference =
            requireActivity().getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)

        try {

            isSyncedFromCloud = sharedPreference.getBoolean(SHARED_PREFERENCE_KEY, false)

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun getAppSetting() {

        viewModel.getAppSetting().observe(viewLifecycleOwner, Observer {

            if (it != null) {

                appSetting = it
            }
        })
    }

    private fun getAllSavedPassword() {

        try {

            passwordViewModel.getAllPasswordsList().observe(viewLifecycleOwner, Observer {

                if (it.isNotEmpty()) {

                    hideNoPassTV()

                    setUpSearchView(it)
                } else {

                    showNoPassTV()
                }

                setUpRecyclerView(it)
            })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setUpSearchView(it: List<Password>?) {

        binding.searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                try {
                    newText?.let { newTxt ->

                        if (newTxt.trim().isEmpty()) {

                            setUpRecyclerView(it)
                        } else {

                            val filteredList = it?.filter { passwrd ->

                                passwrd.userName?.toLowerCase(Locale.ROOT)!!
                                    .contains(newTxt.toLowerCase(Locale.ROOT)) ||
                                        passwrd.siteName?.toLowerCase(Locale.ROOT)!!
                                            .contains(newTxt.toLowerCase(Locale.ROOT))
                            }

                            setUpRecyclerView(filteredList)
                        }
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                return false
            }
        })
    }

    private fun setUpRecyclerView(passwordList: List<Password>?) {

        try {

            passwordList?.let {

                mAdapter.submitList(it)

                binding.savedPasswordRV.apply {

                    layoutManager = LinearLayoutManager(requireContext())
                    setHasFixedSize(true)
                    adapter = mAdapter

                }
                mAdapter.setOnClickListener(this)
            }

            hideProgressBar()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onItemClickListener(password: Password?) {

        passwrd = password

        isPasswordRequiredForDeleting = false

        if (appSetting?.isFingerprintEnabled == getString(R.string.t)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                checkForFingerPrintValidation()
            }
        } else {

            checkForPasswordValidation()
        }
    }

    override fun onSyncBtnClickListener(password: Password?) {

        if (password?.isSynced == NOT_SYNCED) {

            password.isSynced = SYNCED

            FirebaseServiceHelper.uploadDocumentToFireStore(
                requireContext(),
                convertPasswordToJson(password),
                getString(R.string.savedPasswords),
                password.key!!
            )

            showToast(requireContext(), "Password synced")

            passwordViewModel.insert(password)
        } else {

            showToast(requireContext(), "Already synced")
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun checkForFingerPrintValidation() {

        /**check for fingerprint validation**/

        val biometricPrompt = BiometricPrompt.Builder(requireContext())
            .setTitle("Please use your fingerprint")
            .setSubtitle("Authentication required")
            .setDescription("This app has fingerprint protection to keep your password secret.")
            .setNegativeButton(
                "Use your password",
                requireActivity().mainExecutor,
                DialogInterface.OnClickListener { _, _ ->

                    checkForPasswordValidation()
                }).build()


        biometricPrompt.authenticate(
            getCancellationSignal(),
            requireActivity().mainExecutor,
            authenticationCallback
        )
    }

    private var isPasswordRequiredForDeleting = false

    private fun checkForPasswordValidation() {

        MaterialDialog(requireContext()).show {

            title(text = "Password")
            positiveButton(text = "Confirm")
            negativeButton(text = "Cancel")

            input(
                hint = "Enter your password",
                inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD,
                allowEmpty = false
            ) { _, inputString ->

                val encryptPassword = EncryptData().encryptWithSHA(inputString.toString())

                if (encryptPassword == appSetting?.appPassword) {

                    if (isPasswordRequiredForDeleting) {

                        showDialogForDeletingPassword()
                    } else {

                        showPasswordInBottomSheet()
                    }
                } else {

                    showToast(requireContext(), "Password doesn't match!!!")
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

        MaterialDialog(requireContext(), BottomSheet()).show {

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
                    convertPasswordToJson(passwrd)
                )

                findNavController().navigate(action)

                dismiss()
            }

            getCustomView().findViewById<ImageButton>(R.id.deleteBtn).setOnClickListener {

                if (appSetting?.isPasswordRequiredForDeleting == getString(R.string.t)) {

                    isPasswordRequiredForDeleting = true
                    checkForPasswordValidation()

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
            .setTitle("Are you sure?")
            .setMessage("After deleting you will lose this password.")
            .setPositiveButton("DELETE") { dialog, _ ->

                if (passwrd?.mode == OFFLINE || (passwrd?.mode == ONLINE && passwrd?.isSynced == NOT_SYNCED)) {

                    deletePassword(passwrd!!)
                } else {

                    if (isInternetAvailable(requireContext())) {

                        deletePassword(passwrd!!)
                    } else {

                        showNoInternetMessage(requireContext())
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->

                dialog.dismiss()
            }
            .create()
            .show()

    }

    private fun deletePassword(password: Password) {

        try {

            var isUndoClicked = false

            passwordViewModel.delete(password)

            Snackbar.make(
                binding.homeFragCoordinatorLayout,
                "Password deleted",
                Snackbar.LENGTH_LONG
            )
                .setAction("Undo") {

                    passwordViewModel.insert(password)
                    showToast(requireContext(), "Password restored")
                    isUndoClicked = true
                }
                .addCallback(object : Snackbar.Callback() {

                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {

                        if (!isUndoClicked && (password.mode != OFFLINE || (passwrd?.mode == ONLINE && password.isSynced != NOT_SYNCED))) {

                            deleteDocumentFromFireStore(
                                requireContext(),
                                getString(R.string.savedPasswords),
                                password.key!!
                            )
                        }
                    }
                })
                .show()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initializeTheFieldsOfBottomSheet(customView: View, decryptedPassword: String?) {

        customView.findViewById<TextView>(R.id.sitenameTV).text = if (passwrd?.siteName == "") {

            "No site name"
        } else {

            passwrd?.siteName
        }

        customView.findViewById<TextView>(R.id.usernameTV).text = passwrd?.userName

        customView.findViewById<TextView>(R.id.passwordTV).text = decryptedPassword

        customView.findViewById<TextView>(R.id.siteLinkTV).text = passwrd?.siteLink
    }

    private fun getCancellationSignal(): CancellationSignal {

        cancellationSignal = CancellationSignal()

        cancellationSignal?.setOnCancelListener {

            showToast(requireContext(), "Authentication was cancelled")
        }

        return cancellationSignal as CancellationSignal
    }

    private fun initListeners() {

        binding.addPasswordFAB.setOnClickListener(this)
        binding.homeFragCoordinatorLayout.setOnClickListener(this)
        binding.settingsBtn.setOnClickListener(this)
        binding.helpBtn.setOnClickListener(this)
        binding.searchBtn.setOnClickListener(this)

        binding.savedPasswordRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                try {
                    if (dy > 0 && binding.addPasswordFAB.visibility == View.VISIBLE) {

                        binding.addPasswordFAB.hide()
                    } else if (dy < 0 && binding.addPasswordFAB.visibility != View.VISIBLE) {

                        binding.addPasswordFAB.show()

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.addPasswordFAB.id -> {

                findNavController().navigate(R.id.action_homeFragment_to_addPasswordFragment)
            }

            binding.searchBtn.id -> {

                if (!isSearchViewVisible) {

                    showSearchView()
                } else {

                    hideSearchView()
                }
            }

            binding.settingsBtn.id -> {

                findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
            }

            binding.helpBtn.id -> {

                findNavController().navigate(R.id.action_homeFragment_to_helpFragment)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {

            closeKeyboard(requireActivity())
        }
    }

    private fun showSearchView() {

        isSearchViewVisible = !isSearchViewVisible

        binding.searchView.show()
        binding.searchView.animate().translationY(0f).alpha(1f).setDuration(350).start()

        binding.searchView.requestFocus()

        showKeyboard(requireActivity(), binding.searchView)
    }

    private fun hideSearchView() {

        isSearchViewVisible = !isSearchViewVisible

        binding.searchView.animate().translationY(-50f).alpha(0f).setDuration(350).start()

        GlobalScope.launch {

            delay(360)

            hideKeyBoard(requireActivity())
            withContext(Dispatchers.Main) {

                binding.searchView.hide()
                binding.searchView.setQuery("", true)
            }
        }
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

        binding.noPassAddedTV.visibility = View.VISIBLE
        binding.savedPasswordRV.visibility = View.GONE
    }

    private fun hideNoPassTV() {

        binding.noPassAddedTV.visibility = View.GONE
        binding.savedPasswordRV.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null

        try {

            CoroutineScope(Dispatchers.IO).launch {

                closeKeyboard(requireActivity())
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
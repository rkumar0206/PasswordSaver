package com.rohitthebest.passwordsaver.ui.fragments

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.databinding.FragmentAppSetupBinding
import com.rohitthebest.passwordsaver.other.Constants.EDITTEXT_EMPTY_MESSAGE
import com.rohitthebest.passwordsaver.other.Constants.OFFLINE
import com.rohitthebest.passwordsaver.other.Constants.ONLINE
import com.rohitthebest.passwordsaver.other.Constants.RC_SIGN_IN
import com.rohitthebest.passwordsaver.other.Constants.TRY_SIGNIN
import com.rohitthebest.passwordsaver.other.encryption.EncryptData
import com.rohitthebest.passwordsaver.ui.viewModels.AppSettingViewModel
import com.rohitthebest.passwordsaver.util.ConversionWithGson.Companion.convertAppSettingToJson
import com.rohitthebest.passwordsaver.util.FirebaseServiceHelper.Companion.updateDocumentOnFireStore
import com.rohitthebest.passwordsaver.util.FirebaseServiceHelper.Companion.uploadDocumentToFireStore
import com.rohitthebest.passwordsaver.util.Functions.Companion.checkBiometricSupport
import com.rohitthebest.passwordsaver.util.Functions.Companion.getUid
import com.rohitthebest.passwordsaver.util.Functions.Companion.hide
import com.rohitthebest.passwordsaver.util.Functions.Companion.hideKeyBoard
import com.rohitthebest.passwordsaver.util.Functions.Companion.isInternetAvailable
import com.rohitthebest.passwordsaver.util.Functions.Companion.show
import com.rohitthebest.passwordsaver.util.Functions.Companion.showNoInternetMessage
import com.rohitthebest.passwordsaver.util.Functions.Companion.showToast
import com.rohitthebest.passwordsaver.util.Functions.Companion.toStringM
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.random.Random

@AndroidEntryPoint
class AppSetupFragment : Fragment(), View.OnClickListener,
    RadioGroup.OnCheckedChangeListener {

    private val TAG = "AppSetupFragment"

    private val appSettingViewModel: AppSettingViewModel by viewModels()

    private var _binding: FragmentAppSetupBinding? = null
    private val binding get() = _binding!!

    private var flag = true
    private var appSetting: AppSetting? = null

    private lateinit var securityQuestionsList: List<String>
    private var securityQuestion: String = ""

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAppSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        appSettingViewModel.getAppSetting().observe(viewLifecycleOwner, Observer {

            try {

                if (it != null) {

                    appSetting = it

                    binding.include.fingerprintCL.show()
                    binding.include.setupCL.hide()

                    if (flag) {

                        binding.appbar.hide()
                        if (checkBiometricSupport(requireActivity())) {

                            binding.include.fingerPrintAuthBtn.show()
                            binding.include.passwordAuthBtn.hide()

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                                checkForFingerPrintValidation()
                            } else {

                                checkForPasswordValidation()
                            }
                        } else {

                            binding.include.fingerPrintAuthBtn.hide()
                            binding.include.passwordAuthBtn.show()
                            checkForPasswordValidation()
                        }
                    }

                } else {

                    binding.include.setupCL.show()
                    binding.include.fingerprintCL.hide()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

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

    private fun checkForPasswordValidation() {

        MaterialDialog(requireContext()).show {

            title(text = "Password")
            positiveButton(text = "Confirm")
            cancelOnTouchOutside(false)

            input(
                hint = "Enter your password",
                inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD,
                allowEmpty = false
            ) { _, inputString ->

                val encryptPassword = EncryptData().encryptWithSHA(inputString.toString())

                if (encryptPassword == appSetting?.appPassword) {

                    findNavController().navigate(R.id.action_appSetupFragment_to_homeFragment)
                } else {

                    showToast(requireContext(), "Password doesn't match!!!")
                    checkForPasswordValidation()
                }
            }
        }.negativeButton(text = "Forgot password") {

            askForSecurityAnswer()
        }
    }

    private fun askForSecurityAnswer() {

        MaterialDialog(requireContext()).show {

            title(text = "Security Question")
            positiveButton(text = "Confirm")
            negativeButton(text = "Cancel") {

                checkForPasswordValidation()
            }
            message(
                text = EncryptData().decryptAES(
                    appSetting?.securityQuestion,
                    appSetting?.secretKey
                )
            )
            input(hint = "Your answer here", allowEmpty = false) { _, inputString ->

                if (inputString.toString().trim()
                        .toLowerCase(Locale.ROOT) == EncryptData().decryptAES(
                        appSetting?.securityAnswer,
                        appSetting?.secretKey
                    )
                ) {

                    resetPassword()
                } else {

                    showToast(requireContext(), "Oops!! wrong answer")
                    checkForPasswordValidation()
                }
            }
        }

    }

    private fun resetPassword() {

        MaterialDialog(requireContext()).show {

            title(text = "Enter new password")
            positiveButton(text = "Reset password")
            negativeButton(text = "Cancel") {

                checkForPasswordValidation()
            }

            input(
                hint = "Enter new password"
            ) { _, charSequence ->

                if (charSequence.toString().trim().isNotEmpty()) {

                    val encryptPassword =
                        EncryptData().encryptWithSHA(charSequence.toString().trim())
                    //flag = false

                    appSetting?.appPassword = encryptPassword

                    if (appSetting?.mode != OFFLINE) {

                        val map = HashMap<String, Any?>()
                        map["appPassword"] = encryptPassword

                        updateDocumentOnFireStore(
                            requireContext(),
                            map,
                            getString(R.string.appSetting),
                            getUid()!!
                        )

                        appSettingViewModel.insert(appSetting!!)

                    } else {

                        appSettingViewModel.insert(appSetting!!)
                    }

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

    private var cancellationSignal: CancellationSignal? = null

    private fun getCancellationSignal(): CancellationSignal {

        cancellationSignal = CancellationSignal()

        cancellationSignal?.setOnCancelListener {

            showToast(requireContext(), "Authentication was cancelled")
        }

        return cancellationSignal as CancellationSignal
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
                } else if (binding.include.confirmPasswordET.editText?.text.toString()
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
        binding.include.modeRG.setOnClickListener(this)
        binding.include.signInBtn.setOnClickListener(this)
        binding.include.modeRG.setOnCheckedChangeListener(this)
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

                    saveAppSettingToDatabase()
                }
            }

            binding.include.signInBtn.id -> {

                if (isInternetAvailable(requireContext())) {
                    signIn()
                } else {
                    showNoInternetMessage(requireContext())
                }
            }

            binding.include.fingerPrintAuthBtn.id -> {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                    checkForFingerPrintValidation()
                }
            }

            binding.include.passwordAuthBtn.id -> {

                checkForPasswordValidation()
            }

        }
    }

    private var encryptedSecretKey = ""

    private fun saveAppSettingToDatabase() {

        val mode = when (binding.include.modeRG.checkedRadioButtonId) {

            binding.include.modeOfflineRB.id -> OFFLINE

            binding.include.modeOnlineRB.id -> ONLINE

            else -> TRY_SIGNIN
        }

        val encryptedAppPassword = EncryptData().encryptWithSHA(
            binding.include.passwordET.editText?.text.toString().trim()
        )

        if (encryptedSecretKey == "") {

            EncryptData().encryptWithSHA(
                "${System.currentTimeMillis().toStringM(69)}_${
                    Random.nextLong(100, 9223372036854775).toStringM(69)
                }"
            )
        }

        Log.i(TAG, "saveAppSettingToDatabase: Secret key : $encryptedSecretKey")

        val encryptedSecurityQuestion =
            EncryptData().encryptWithAES(
                securityQuestion.toLowerCase(Locale.ROOT),
                encryptedSecretKey
            )!!

        val encryptedSecurityAnswer = EncryptData().encryptWithAES(
            binding.include.securityAnswerET.editText?.text.toString().trim()
                .toLowerCase(Locale.ROOT), encryptedSecretKey
        )!!

        val isFingerPrintEnabled =
            if (!requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {

                getString(R.string.f)
            } else {

                if (binding.include.fingerPrintCB.isChecked) {

                    getString(R.string.t)
                } else {

                    getString(R.string.f)
                }

            }

        val appSetting = AppSetting(
            mode = mode,
            appPassword = encryptedAppPassword,
            securityQuestion = encryptedSecurityQuestion,
            securityAnswer = encryptedSecurityAnswer,
            uid = getUid(),
            secretKey = encryptedSecretKey,
            isPasswordRequiredForCopy = getString(R.string.t),
            isPasswordRequiredForVisibility = getString(R.string.t),
            isFingerprintEnabled = isFingerPrintEnabled,
            key = getUid()!!
        )

        if (mode != OFFLINE) {

            //upload to firestore as well as local database

            if (isInternetAvailable(requireContext())) {

                uploadDocumentToFireStore(
                    requireContext(),
                    convertAppSettingToJson(appSetting)!!,
                    getString(R.string.appSetting),
                    getUid().toString()
                )

                insertToLocalDatabase(appSetting)

            } else {
                showNoInternetMessage(requireContext())
            }
        } else {

            //saving only to local database
            insertToLocalDatabase(appSetting)
        }
    }

    private fun insertToLocalDatabase(appSetting: AppSetting) {

        isRecordsFound = true
        flag = false
        appSettingViewModel.insert(appSetting)
        if (appSetting.mode == OFFLINE && mAuth.currentUser != null) {

            signOut()
        } else {

            findNavController().navigate(R.id.action_appSetupFragment_to_homeFragment)
        }
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

        if (binding.include.modeRG.checkedRadioButtonId == binding.include.modeOnlineRB.id
            || binding.include.modeRG.checkedRadioButtonId == binding.include.modeTrySignInRB.id
        ) {

            if (mAuth.currentUser == null) {

                Snackbar.make(
                    binding.appSetupCOORL,
                    getString(R.string.log_in),
                    Snackbar.LENGTH_LONG
                )
                    .setAction("Log in") {

                        if (isInternetAvailable(requireContext())) {
                            signIn()
                        } else {
                            showNoInternetMessage(requireContext())
                        }
                    }
                    .show()

                return false
            }
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

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {

        when (checkedId) {

            binding.include.modeOfflineRB.id -> {

                binding.include.selectedModeTV.text = getString(R.string.offline_text)
                binding.include.signInBtn.hide()
                binding.include.emailTV.hide()
                hideProgressBar()
            }

            binding.include.modeOnlineRB.id -> {

                binding.include.selectedModeTV.text = getString(R.string.online_text)

                if (mAuth.currentUser == null) {

                    binding.include.signInBtn.show()
                } else {

                    showEmailTV()
                }
            }

            binding.include.modeTrySignInRB.id -> {

                binding.include.selectedModeTV.text = getString(R.string.trySignIn_text)

                if (mAuth.currentUser == null) {

                    binding.include.signInBtn.show()
                } else {
                    showEmailTV()
                    getAppSettingsFromCloudDatabase()
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mAuth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    // [START signin]
    private fun signIn() {

        showProgressBar()

        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(
            signInIntent,
            RC_SIGN_IN
        )
    }
    // [END signin]

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)

                firebaseAuthWithGoogle(account.idToken!!)

            } catch (e: ApiException) {
                try {
                    // Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in failed", e)
                    // [START_EXCLUDE]
                    showToast(requireContext(), "SignIn Un-successful")
                    hideProgressBar()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        // [START_EXCLUDE silent]
        showProgressBar()
        // [END_EXCLUDE]
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        try {
            mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")

                        showToast(requireContext(), "SignIn successful")
                        showEmailTV()

                        if (binding.include.modeRG.checkedRadioButtonId != binding.include.modeOfflineRB.id) {

                            showProgressBar()
                            getAppSettingsFromCloudDatabase()
                        }

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        showToast(requireContext(), "Authentication Failed.")
                        hideProgressBar()

                    }
                    // [START_EXCLUDE]
                    hideProgressBar()
                    // [END_EXCLUDE]
                }
        } catch (e: Exception) {
        }
    }

    private var isRecordsFound = true

    private fun getAppSettingsFromCloudDatabase() {

        try {
            showProgressBar()

            FirebaseFirestore.getInstance()
                .collection(getString(R.string.appSetting))
                .document(getUid()!!)
                .get()
                .addOnSuccessListener {

                    if (it.exists()) {

                        val appSetting = it.toObject(AppSetting::class.java)

                        if (binding.include.modeRG.checkedRadioButtonId == binding.include.modeOnlineRB.id) {

                            // setting the secret key to the secret key that is stored in the cloud database
                            // otherwise the passwords will not be decrypted again after the he changes the password
                            encryptedSecretKey = appSetting?.secretKey!!

                        } else if (
                            binding.include.modeRG.checkedRadioButtonId == binding.include.modeTrySignInRB.id) {

                            showToast(
                                requireContext(),
                                getString(R.string.check_previous_app_setting_message)
                            )

                            try {

                                if (appSetting != null) {

                                    Log.i(
                                        TAG,
                                        "getAppSettingsFromCloudDatabase: appSetting not null"
                                    )
                                    appSettingViewModel.insert(appSetting)

                                    hideProgressBar()
                                    isRecordsFound = true
                                } else {

                                    Log.i(TAG, "handleIfNoRecordsFound: appSetting null")
                                    handleIfNoRecordsFound()
                                }
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {

                        handleIfNoRecordsFound()
                    }

                }.addOnFailureListener {

                    handleIfNoRecordsFound()
                }
        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    private fun handleIfNoRecordsFound() {

        isRecordsFound = false
        showToast(
            requireContext(),
            "Sorry!! No records found. Go with OFFLINE or ONLINE mode.",
            Toast.LENGTH_LONG
        )
        hideProgressBar()

        signOut()

        binding.include.modeRG.check(binding.include.modeOfflineRB.id)
    }

    private fun signOut() {

        try {

            mAuth.signOut()

            //[Google Sign Out]
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

            googleSignInClient?.signOut()?.addOnCompleteListener {
                Log.i(TAG, "Google signOut Successful")

                try {

                    if (isRecordsFound) {

                        findNavController().navigate(R.id.action_appSetupFragment_to_homeFragment)
                    }
                    binding.include.emailTV.hide()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            //showToast(requireContext(), "You are signed out")
            Log.i(TAG, "You are signed out")

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun showEmailTV() {

        try {

            binding.include.emailTV.text = mAuth.currentUser?.email
            binding.include.emailTV.show()
            binding.include.signInBtn.hide()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
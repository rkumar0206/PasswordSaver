package com.rohitthebest.passwordsaver.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
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
import com.rohitthebest.passwordsaver.util.FirebaseServiceHelper.Companion.uploadDocumentToFireStore
import com.rohitthebest.passwordsaver.util.Functions.Companion.getUid
import com.rohitthebest.passwordsaver.util.Functions.Companion.hide
import com.rohitthebest.passwordsaver.util.Functions.Companion.hideKeyBoard
import com.rohitthebest.passwordsaver.util.Functions.Companion.isInternetAvailable
import com.rohitthebest.passwordsaver.util.Functions.Companion.show
import com.rohitthebest.passwordsaver.util.Functions.Companion.showNoInternetMessage
import com.rohitthebest.passwordsaver.util.Functions.Companion.showToast
import com.rohitthebest.passwordsaver.util.Functions.Companion.toStringM
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random

@AndroidEntryPoint
class AppSetupFragment : Fragment(), View.OnClickListener,
    RadioGroup.OnCheckedChangeListener {

    private val TAG = "AppSetupFragment"

    private val appSettingViewModel: AppSettingViewModel by viewModels()

    private var _binding: FragmentAppSetupBinding? = null
    private val binding get() = _binding!!

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

        getAppSettingData()
        initListeners()
        textWatcher()
    }

    private fun getAppSettingData() {

        appSettingViewModel.getAppSetting().observe(viewLifecycleOwner, Observer {

            try {

                if (it != null) {

                    findNavController().navigate(R.id.action_appSetupFragment_to_homeFragment)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

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

                    binding.include.passwordET.error = EDITTEXT_EMPTY_MESSAGE
                } else if (binding.include.passwordET.editText?.text.toString().isNotEmpty()) {

                    if (s.toString()
                            .trim() != binding.include.passwordET.editText?.text.toString()
                    ) {

                        binding.include.confirmPasswordET.error =
                            "It should be same as the password."
                    } else {

                        binding.include.confirmPasswordET.error = null
                    }
                } else {

                    binding.include.passwordET.error = null
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
        binding.nextBtn.setOnClickListener(this)
        //binding.include.fingerPrintCB.setOnCheckedChangeListener(this)
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.nextBtn.id -> {

                if (validateForm()) {

                    saveAppSettingToDatabase()
                } else {

                    showToast(requireContext(), "Something wrong")
                }
            }

            binding.include.signInBtn.id -> {

                if (isInternetAvailable(requireContext())) {
                    signIn()
                } else {
                    showNoInternetMessage(requireContext())
                }
            }

        }
    }

    private fun saveAppSettingToDatabase() {

        val mode = when (binding.include.modeRG.checkedRadioButtonId) {

            binding.include.modeOfflineRB.id -> OFFLINE

            binding.include.modeOnlineRB.id -> ONLINE

            else -> TRY_SIGNIN
        }

        val encryptedPassword = EncryptData().encryptWithSHA(
            binding.include.passwordET.editText?.text.toString().trim()
        )

        val isFingerPrintEnabled = if (binding.include.fingerPrintCB.isChecked) {

            getString(R.string.t)
        } else {

            getString(R.string.f)
        }

        val appSetting = AppSetting(
            mode = mode,
            appPassword = encryptedPassword,
            uid = getUid(),
            isPasswordRequiredForCopy = getString(R.string.t),
            isPasswordRequiredForVisibility = getString(R.string.t),
            isFingerprintEnabled = isFingerPrintEnabled,
            key = "${System.currentTimeMillis().toStringM(69)}_${
                Random.nextLong(
                    100,
                    9223372036854775
                ).toStringM(69)
            }_${getUid()}"
        )

        if (mode != OFFLINE) {

            //upload to firestore as well as local database

            if (isInternetAvailable(requireContext())) {
                uploadDocumentToFireStore(
                    requireContext(),
                    convertAppSettingToJson(appSetting)!!,
                    getString(R.string.appSetting),
                    appSetting.key
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

        appSettingViewModel.insert(appSetting)
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

        if (binding.include.modeRG.checkedRadioButtonId == binding.include.modeOnlineRB.id
            || binding.include.modeRG.checkedRadioButtonId == binding.include.modeTrySignInRB.id
        ) {

            if (mAuth.currentUser == null) {

                Snackbar.make(
                    binding.appSetupCOORL,
                    "You are not Logged In",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(getString(R.string.log_in)) {

                        if (isInternetAvailable(requireContext())) {
                            signIn()
                        } else {
                            showNoInternetMessage(requireContext())
                        }
                    }
                    .show()
            }

            return false
        }

        if ((binding.include.confirmPasswordET.editText?.text.toString().trim() !=
                    binding.include.passwordET.editText?.text.toString().trim())
        ) {

            binding.include.confirmPasswordET.error = "It doesn't match with the password!!!"
            return false
        }

        return binding.include.passwordET.error == null
                && binding.include.confirmPasswordET.error == null
                && (binding.include.confirmPasswordET.editText?.text.toString().trim() ==
                binding.include.passwordET.editText?.text.toString().trim())

    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {

        when (checkedId) {

            binding.include.modeOfflineRB.id -> {

                binding.include.selectedModeTV.text = getString(R.string.offline_text)
                binding.include.signInBtn.hide()
            }

            binding.include.modeOnlineRB.id -> {

                binding.include.selectedModeTV.text = getString(R.string.online_text)
                binding.include.signInBtn.show()
            }

            binding.include.modeTrySignInRB.id -> {

                binding.include.selectedModeTV.text = getString(R.string.trySignIn_text)
                binding.include.signInBtn.show()
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


    private fun showProgressBar() {

        try {

            binding.progressBar.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideProgressBar() {

        try {

            binding.progressBar.hide()
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
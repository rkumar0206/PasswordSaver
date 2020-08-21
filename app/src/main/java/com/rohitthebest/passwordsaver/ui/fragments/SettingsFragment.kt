package com.rohitthebest.passwordsaver.ui.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.database.entity.Password
import com.rohitthebest.passwordsaver.databinding.FragmentSettingsBinding
import com.rohitthebest.passwordsaver.other.Constants
import com.rohitthebest.passwordsaver.other.Constants.NOT_SYNCED
import com.rohitthebest.passwordsaver.other.Constants.OFFLINE
import com.rohitthebest.passwordsaver.other.Constants.ONLINE
import com.rohitthebest.passwordsaver.other.Constants.TARGET_FRAGMENT_MESSAGE
import com.rohitthebest.passwordsaver.other.Constants.TARGET_FRAGMENT_REQUEST_CODE
import com.rohitthebest.passwordsaver.other.Functions
import com.rohitthebest.passwordsaver.other.Functions.Companion.showToast
import com.rohitthebest.passwordsaver.ui.viewModels.AppSettingViewModel
import com.rohitthebest.passwordsaver.ui.viewModels.PasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*


@AndroidEntryPoint
class SettingsFragment : Fragment(), View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private val TAG = "SettingsFragment"
    private val appSettingViewModel: AppSettingViewModel by viewModels()
    private val passwordViewModel: PasswordViewModel by viewModels()

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var appSetting: AppSetting? = null
    private var savedPasswordList: ArrayList<Password>? = null

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private var radioButtonChangeListener = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        disableSaveBtn()

        savedPasswordList = ArrayList()

        mAuth = Firebase.auth

        initListeners()
        getAppSetting()
        getPasswordList()
    }

    private fun getAppSetting() {

        try {

            appSettingViewModel.getAppSettingByID().observe(viewLifecycleOwner, Observer {


                if (it.isNotEmpty()) {

                    appSetting = it[0]

                    updateUI()
                }

            })

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    private fun getPasswordList() {

        try {

            passwordViewModel.getAllPasswordsList().observe(viewLifecycleOwner, Observer { it ->

                if (it.isNotEmpty()) {

                    it.forEach { password ->

                        savedPasswordList?.add(password)
                    }

                    enableSaveBtn()
                }

            })

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    private fun updateUI() {

        appSetting?.let {

            if (it.mode == ONLINE) {

                binding.modeChangeRG.check(binding.onlineModeRB.id)
            } else {

                binding.modeChangeRG.check(binding.offlineModeRB.id)
            }

            binding.copyCB.isChecked = it.enterPasswordForCopy == getString(R.string.t)

            binding.visibilityCB.isChecked = it.enterPasswordForVisibility == getString(R.string.t)
        }

        radioButtonChangeListener = true
    }

    private fun initListeners() {

        binding.backBtn.setOnClickListener(this)
        binding.saveBtn.setOnClickListener(this)
        binding.changePasswordIB.setOnClickListener(this)
        binding.changePasswordTV.setOnClickListener(this)
        binding.modeChangeRG.setOnCheckedChangeListener(this)
    }


    override fun onCheckedChanged(radioGroup: RadioGroup?, checkedId: Int) {

        if (checkedId == binding.offlineModeRB.id) {

            if (radioButtonChangeListener) {
                val message =
                    "${getString(R.string.offline_text)}\n2 .If you choose offline mode all " +
                            "the passwords saved on cloud will" +
                            " be permanently deleted and cannot be retrieved again."

                AlertDialog.Builder(requireContext())
                    .setMessage(message)
                    .setPositiveButton("Ok") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .create()
                    .show()

            }
        } else {

            if (radioButtonChangeListener && appSetting?.uid == "") {
                val message =
                    "${getString(R.string.online_text)}\n\nIn Order to save your data to" +
                            " cloud you need to signIn with your Google Account."

                AlertDialog.Builder(requireContext())
                    .setMessage(message)
                    .setPositiveButton("SignIn") { dialogInterface, _ ->

                        if (Functions.isInternetAvailable(requireContext())) {
                            disableSaveBtn()
                            signIn()
                        } else {
                            showToast(requireContext(), Constants.NO_INTERNET_MESSAGE)
                        }
                        dialogInterface.dismiss()
                    }.setOnDismissListener {

                        if (mAuth.currentUser == null) {
                            radioButtonChangeListener = false
                            binding.modeChangeRG.check(binding.offlineModeRB.id)
                        }

                        GlobalScope.launch {

                            delay(200)

                            withContext(Dispatchers.Main) {

                                radioButtonChangeListener = true
                            }
                        }
                    }
                    .create()
                    .show()
            }

        }

    }


    override fun onClick(v: View?) {

        when (v?.id) {

            binding.saveBtn.id -> {

                updateChanges()
            }

            binding.backBtn.id -> {
                requireActivity().onBackPressed()
            }
        }

        if (v?.id == binding.changePasswordIB.id || v?.id == binding.changePasswordTV.id) {

            openDialog()
        }

    }

    private fun openDialog() {

        val dialogFragment = ChangeAppPasswordDialog().getInstance()
        dialogFragment.setTargetFragment(this, TARGET_FRAGMENT_REQUEST_CODE)

        parentFragmentManager.let { dialogFragment.show(it, "ChangeAppPasswordDialog") }
    }


    private fun updateChanges() {

        appSetting?.let {

            it.enterPasswordForCopy = if (binding.copyCB.isChecked) {

                getString(R.string.t)
            } else {

                getString(R.string.f)
            }

            it.enterPasswordForVisibility = if (binding.visibilityCB.isChecked) {

                getString(R.string.t)
            } else {

                getString(R.string.f)
            }

            it.mode =
                if (binding.modeChangeRG.checkedRadioButtonId == binding.offlineModeRB.id) {

                    //todo : delete all passwords from firestore database
                    OFFLINE
                } else {
                    //todo : upload appSetting to database
                    //todo : update the field isSynced in sqlite database

                    savedPasswordList?.forEach { password ->

                        password.isSynced = NOT_SYNCED
                        password.uid = mAuth.currentUser?.uid
                        passwordViewModel.insert(password)
                    }

                    ONLINE
                }

            appSettingViewModel.insert(it)
        }

    }

    // [START signin]
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(
            signInIntent,
            Constants.RC_SIGN_IN
        )
    }
    // [END signin]


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mAuth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == Constants.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)

                firebaseAuthWithGoogle(account.idToken!!)

            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                // [START_EXCLUDE]
                showToast(requireContext(), "SignIn Un-successful")
            }
        }

        if (resultCode != Activity.RESULT_OK) {

            return
        }

        if (requestCode == TARGET_FRAGMENT_REQUEST_CODE) {

            data?.getStringExtra(TARGET_FRAGMENT_MESSAGE)?.let {

                if (it.isNotEmpty()) {

                    //todo : change the password
                    showToast(requireContext(), it)
                }
            }
        }

    }

    fun newIntent(message: String): Intent {

        val intent = Intent()
        intent.putExtra(TARGET_FRAGMENT_MESSAGE, message)

        return intent
    }

    private fun firebaseAuthWithGoogle(idToken: String) {

        showProgressBar()

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    showToast(requireContext(), "SignIn successful")
                    showToast(requireContext(), "signInWithCredential:success")

                    enableSaveBtn()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    showToast(requireContext(), "Authentication Failed.")

                    radioButtonChangeListener = false
                    binding.modeChangeRG.check(binding.offlineModeRB.id)
                    enableSaveBtn()
                    hideProgressBar()
                }
                hideProgressBar()
            }
    }

    private fun showProgressBar() {

        try {
            binding.progressBar.visibility = View.VISIBLE
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun hideProgressBar() {
        try {
            binding.progressBar.visibility = View.GONE
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun enableSaveBtn() {

        try {
            binding.saveBtn.isEnabled = true
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun disableSaveBtn() {
        try {
            binding.saveBtn.isEnabled = false
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}
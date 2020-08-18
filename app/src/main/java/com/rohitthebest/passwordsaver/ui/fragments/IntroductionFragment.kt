package com.rohitthebest.passwordsaver.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.databinding.FragmentIntroductionBinding
import com.rohitthebest.passwordsaver.other.Constants
import com.rohitthebest.passwordsaver.other.Constants.APP_SETTING_ID
import com.rohitthebest.passwordsaver.other.Constants.NO_INTERNET_MESSAGE
import com.rohitthebest.passwordsaver.other.Constants.OFFLINE
import com.rohitthebest.passwordsaver.other.Constants.ONLINE
import com.rohitthebest.passwordsaver.other.Functions.Companion.isInternetAvailable
import com.rohitthebest.passwordsaver.other.Functions.Companion.showToast
import com.rohitthebest.passwordsaver.ui.viewModels.AppSettingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntroductionFragment : Fragment(), RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private val TAG = "IntroductionFragment"
    private val viewModel: AppSettingViewModel by viewModels()

    private var _binding: FragmentIntroductionBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentIntroductionBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = Firebase.auth

        initListeners()


    }

    private fun initListeners() {

        binding.nextBtn.setOnClickListener(this)
        binding.modeRG.setOnCheckedChangeListener(this)
    }


    override fun onCheckedChanged(p0: RadioGroup?, checkedId: Int) {

        when (checkedId) {
            binding.offlineModeRB.id -> {

                binding.modeInfoTV.text = getString(R.string.offline_text)
            }
            binding.onlineModeRB.id -> {

                binding.modeInfoTV.text = getString(R.string.online_text)
            }
            else -> {

                binding.modeInfoTV.text = getString(R.string.trySignIn_text)
            }
        }
    }


    override fun onClick(v: View?) {

        when (v?.id) {

            binding.nextBtn.id -> {
                saveResultToDatabase()
            }
        }
    }

    private fun saveResultToDatabase() {

        if (binding.modeRG.checkedRadioButtonId == binding.offlineModeRB.id) {

            insertToDatabase(OFFLINE, "")

        } else if (binding.modeRG.checkedRadioButtonId == binding.onlineModeRB.id) {

            if (mAuth.currentUser == null) {
                showAlertMessage()
            } else {
                mAuth.currentUser?.uid?.let { insertToDatabase(ONLINE, it) }
            }
        } else {

            if (isInternetAvailable(requireContext())) {
                signIn()
            } else {
                showToast(requireContext(), NO_INTERNET_MESSAGE)
            }
        }
    }

    private fun insertToDatabase(mode: String, uid: String) {

        val appSetting = AppSetting(mode, "", uid)
        appSetting.id = APP_SETTING_ID
        viewModel.insert(appSetting)

        findNavController().navigate(R.id.action_introductionFragment_to_appPasswordFragment)
    }

    private fun showAlertMessage() {

        AlertDialog.Builder(requireContext())
            .setTitle("SignIn")
            .setMessage("In Order to save your data to cloud you need to signIn with your Google Account.")
            .setPositiveButton("Sign In") { dialogInterface, _ ->

                if (isInternetAvailable(requireContext())) {
                    signIn()
                } else {
                    showToast(requireContext(), NO_INTERNET_MESSAGE)
                }
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->

                dialogInterface.dismiss()
            }.create()
            .show()

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

                    if (binding.modeRG.checkedRadioButtonId == binding.onlineModeRB.id) {
                        mAuth.currentUser?.uid?.let { insertToDatabase(ONLINE, it) }
                    } else {
                        try {

                            showProgressBar()
                            showToast(requireContext(), "Getting Data")
                            mAuth.currentUser?.uid?.let {
                                FirebaseFirestore.getInstance()
                                    .collection(getString(R.string.appSetting))
                                    .document(it)
                                    .get()
                                    .addOnSuccessListener { documentSnapshot ->

                                        if (documentSnapshot.exists()) {

                                            val appSetting: AppSetting? =
                                                documentSnapshot.toObject(AppSetting::class.java)

                                            appSetting?.let { it1 ->
                                                viewModel.insert(it1)
                                                hideProgressBar()
                                                findNavController().navigate(R.id.action_introductionFragment_to_appPasswordFragment)
                                            }
                                        } else {

                                            hideProgressBar()
                                            showToast(
                                                requireContext(),
                                                "No Data Found!!!\n Try another method."
                                            )
                                        }
                                    }
                                    .addOnFailureListener {exception ->

                                        showToast(requireContext(), "${exception.message}")
                                    }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    showToast(requireContext(), "Authentication Failed.")
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


    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback = object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {

                requireActivity().finish()

            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(callback)
    }


}
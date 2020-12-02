package com.rohitthebest.passwordsaver.ui.fragments

import android.app.Activity
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
import com.rohitthebest.passwordsaver.databinding.SettingsFragmentLayoutBinding
import com.rohitthebest.passwordsaver.other.Constants
import com.rohitthebest.passwordsaver.other.Constants.ONLINE
import com.rohitthebest.passwordsaver.ui.viewModels.AppSettingViewModel
import com.rohitthebest.passwordsaver.ui.viewModels.PasswordViewModel
import com.rohitthebest.passwordsaver.util.Functions.Companion.hide
import com.rohitthebest.passwordsaver.util.Functions.Companion.show
import com.rohitthebest.passwordsaver.util.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class SettingsFragment : Fragment(), View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private val TAG = "SettingsFragment"
    private val appSettingViewModel: AppSettingViewModel by viewModels()
    private val passwordViewModel: PasswordViewModel by viewModels()

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var includeBinding: SettingsFragmentLayoutBinding

    private var appSetting: AppSetting? = null
    private var savedPasswordList: ArrayList<Password>? = null

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private var isradioButtonChangeListenerEnabled = false

    private var isOnlineModeInitially = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        includeBinding = binding.include

        disableSaveBtn()
        initListeners()
        getAppSetting()
        getSavedPasswordList()
    }

    private fun getAppSetting() {

        try {

            appSettingViewModel.getAppSetting().observe(viewLifecycleOwner, Observer {

                if (it != null) {

                    appSetting = it

                    updateUI()
                }

            })

         } catch (e: Exception) {

             e.printStackTrace()
         }
     }

    private fun getSavedPasswordList() {

        try {

            passwordViewModel.getAllPasswordsList().observe(viewLifecycleOwner, Observer {

                if (it.isNotEmpty()) {

                    it.forEach { password ->

                        savedPasswordList?.add(password)
                    }
                }

                enableSaveBtn()
            })

         } catch (e: Exception) {
             e.printStackTrace()
         }
     }

     private fun updateUI() {

         appSetting?.let {

             if (it.mode == ONLINE) {

                 isOnlineModeInitially = true
                 includeBinding.modeChangeRG.check(includeBinding.onlineModeRB.id)
             } else {
                 isOnlineModeInitially = false
                 includeBinding.modeChangeRG.check(includeBinding.offlineModeRB.id)
             }

             includeBinding.deleteCB.isChecked =
                 it.isPasswordRequiredForDeleting == getString(R.string.t)

             includeBinding.fingerprintCB.isChecked =
                 it.isFingerprintEnabled == getString(R.string.t)
         }
         isradioButtonChangeListenerEnabled = true
     }

     private fun initListeners() {

         binding.backBtn.setOnClickListener(this)
         binding.saveBtn.setOnClickListener(this)
         includeBinding.changePasswordIB.setOnClickListener(this)
         includeBinding.changePasswordTV.setOnClickListener(this)
         includeBinding.modeChangeRG.setOnCheckedChangeListener(this)
     }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.saveBtn.id -> {

                //todo : save changes
            }
            binding.backBtn.id -> {
                requireActivity().onBackPressed()
            }
        }

        if (v?.id == includeBinding.changePasswordIB.id || v?.id == includeBinding.changePasswordTV.id) {

            //todo : open the dialog for changing the password
        }
    }


    override fun onCheckedChanged(radioGroup: RadioGroup?, checkedId: Int) {

        if (isradioButtonChangeListenerEnabled) {

            if (isOnlineModeInitially && checkedId == includeBinding.offlineModeRB.id) {

                //todo : ask for deletion of password from fireStore

            }

            if (!isOnlineModeInitially && checkedId == includeBinding.onlineModeRB.id) {

                //also change the secretkey of the saved passwords
                //todo : sign in the user and upload all the password to firestore along with app settings
            }
        }
    }


    /*private fun deleteDataFromFireStore(it: AppSetting) {

        if (savedPasswordList?.isNotEmpty()!!) {
            savedPasswordList?.forEach { password ->

                password.uid = ""
                passwordViewModel.insert(password)
            }
        }

        val appSettingMessage = it.uid

        val passwordListMessage = if (savedPasswordList?.isNotEmpty()!!) {

            convertPasswordListToJson(savedPasswordList)
        } else {
            ""
        }

        Log.d(TAG, "$appSettingMessage")
        Log.d(TAG, "$passwordListMessage")

        val foreGroundServiceIntent =
            Intent(requireContext(), DeleteAppSettingAndPasswordService::class.java)
        foreGroundServiceIntent.putExtra(
            DELETE_APPSETTING_SERVICE_MESSAGE,
            appSettingMessage
        )
        foreGroundServiceIntent.putExtra(
            DELETE_PASSWORD_SERVICE_MESSAGE,
            passwordListMessage
        )

        ContextCompat.startForegroundService(
            requireContext(),
            foreGroundServiceIntent
        )


        it.uid = ""
        it.mode = OFFLINE
        appSettingViewModel.insert(it)

        requireActivity().onBackPressed()
    }
*/
/*
     private fun uploadAppSettingToFireStore(it: AppSetting) {

         it.uid = mAuth.currentUser?.uid
         it.mode = ONLINE

         appSettingViewModel.insert(it)

         val foreGroundServiceIntent =
             Intent(requireContext(), UploadAppSettingsService::class.java)
         foreGroundServiceIntent.putExtra(
             APP_SETTING_SERVICE_MESSAGE,
             convertAppSettingToJson(it)
         )

         ContextCompat.startForegroundService(
             requireContext(),
             foreGroundServiceIntent
         )

         requireActivity().onBackPressed()
     }
*/

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
     }

/*
     private fun changePassword(newPassword: String) {

         appSetting?.let {

             if (it.mode == ONLINE) {

                 it.appPassword = newPassword

                 if (isInternetAvailable(requireContext())) {

                     val foreGroundServiceIntent =
                         Intent(requireContext(), UploadAppSettingsService::class.java)
                     foreGroundServiceIntent.putExtra(
                         APP_SETTING_SERVICE_MESSAGE,
                         convertAppSettingToJson(it)
                     )

                     ContextCompat.startForegroundService(
                         requireContext(),
                         foreGroundServiceIntent
                     )

                     appSettingViewModel.insert(it)
                     showToast(requireContext(), "Password Changed")
                 } else {

                     showToast(requireContext(), NO_INTERNET_MESSAGE)
                     showToast(requireContext(), "Cannot change the password!!!", Toast.LENGTH_LONG)
                 }

             } else {

                 it.appPassword = newPassword
                 appSettingViewModel.insert(it)
                 showToast(requireContext(), "Password Changed")
             }

         }
     }
*/

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
                    isradioButtonChangeListenerEnabled = false
                    includeBinding.modeChangeRG.check(includeBinding.onlineModeRB.id)

                    GlobalScope.launch {
                        delay(200)
                        withContext(Dispatchers.Main) {

                            isradioButtonChangeListenerEnabled = true
                        }
                    }

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    showToast(requireContext(), "Authentication Failed.")

                    isradioButtonChangeListenerEnabled = false
                    includeBinding.modeChangeRG.check(includeBinding.offlineModeRB.id)
                    enableSaveBtn()
                    hideProgressBar()

                    GlobalScope.launch {
                        delay(200)
                        withContext(Dispatchers.Main) {

                            isradioButtonChangeListenerEnabled = true
                        }
                    }

                 }
                 hideProgressBar()
             }
     }


     private fun showProgressBar() {

         try {
             binding.progressBar.show()
         } catch (e: IllegalStateException) {
             e.printStackTrace()
         }
     }

     private fun hideProgressBar() {
         try {
             binding.progressBar.hide()
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
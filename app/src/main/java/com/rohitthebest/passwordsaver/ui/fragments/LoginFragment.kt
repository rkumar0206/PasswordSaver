package com.rohitthebest.passwordsaver.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.other.Constants.NO_INTERNET_MESSAGE
import com.rohitthebest.passwordsaver.other.Constants.RC_SIGN_IN
import com.rohitthebest.passwordsaver.other.Functions.Companion.isInternetAvailable
import com.rohitthebest.passwordsaver.other.Functions.Companion.showToast
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val TAG = "LoginFragment"
    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = Firebase.auth

    }

    override fun onStart() {
        super.onStart()

        if (isInternetAvailable(requireContext())) {
            if (mAuth.currentUser == null) {
                signIn()
            } else {
                signOut()
            }
        } else {
            showToast(requireContext(), NO_INTERNET_MESSAGE)
            requireActivity().onBackPressed()
            //findNavController().navigate(R.id.a)
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
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                // [START_EXCLUDE]
                showToast(requireContext(), "SignIn Un-successful")
                requireActivity().onBackPressed()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        // [START_EXCLUDE silent]
        showProgressBar()
        // [END_EXCLUDE]
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    showToast(requireContext(), "SignIn successful")
                    showToast(requireContext(), "signInWithCredential:success")

                    requireActivity().onBackPressed()
                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    showToast(requireContext(), "Authentication Failed.")
                    hideProgressBar()
                    //findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    requireActivity().onBackPressed()
                }
                // [START_EXCLUDE]
                hideProgressBar()
                // [END_EXCLUDE]
            }
    }

    private fun signOut() {

        try {
            AlertDialog.Builder(requireContext())
                .setTitle("Are You Sure?")
                .setPositiveButton("Yes") { _, _ ->
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
                            showToast(requireContext(), "Google signOut Successful")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    showToast(requireContext(), "SignOut Successful")
                    //todo : after signOut
                    //findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                    requireActivity().onBackPressed()
                }
                .create()
                .show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun showProgressBar() {

        try {
            loginProgressBar.visibility = View.VISIBLE
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun hideProgressBar() {
        try {
            loginProgressBar.visibility = View.GONE
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

}
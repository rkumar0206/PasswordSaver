package com.rohitthebest.passwordsaver.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.database.entity.Password
import com.rohitthebest.passwordsaver.databinding.FragmentHomeBinding
import com.rohitthebest.passwordsaver.other.Constants
import com.rohitthebest.passwordsaver.other.Constants.NO_INTERNET_MESSAGE
import com.rohitthebest.passwordsaver.other.Constants.ONLINE
import com.rohitthebest.passwordsaver.other.Constants.SHARED_PREFERENCE_KEY
import com.rohitthebest.passwordsaver.other.Constants.SHARED_PREFERENCE_NAME
import com.rohitthebest.passwordsaver.other.Constants.SYNCED
import com.rohitthebest.passwordsaver.other.Constants.TARGET_FRAGMENT_MESSAGE
import com.rohitthebest.passwordsaver.other.Constants.TARGET_FRAGMENT_REQUEST_CODE
import com.rohitthebest.passwordsaver.other.Constants.TARGET_FRAGMENT_REQUEST_CODE2
import com.rohitthebest.passwordsaver.other.Functions.Companion.closeKeyboard
import com.rohitthebest.passwordsaver.other.Functions.Companion.convertPasswordToJson
import com.rohitthebest.passwordsaver.other.Functions.Companion.isInternetAvailable
import com.rohitthebest.passwordsaver.other.Functions.Companion.showToast
import com.rohitthebest.passwordsaver.other.encryption.EncryptData
import com.rohitthebest.passwordsaver.services.UploadSavedPasswordService
import com.rohitthebest.passwordsaver.ui.adapters.SavedPasswordRVAdapter
import com.rohitthebest.passwordsaver.ui.viewModels.AppSettingViewModel
import com.rohitthebest.passwordsaver.ui.viewModels.PasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import kotlin.random.Random

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), View.OnClickListener,
    SavedPasswordRVAdapter.OnClickListener, android.widget.PopupMenu.OnMenuItemClickListener,
    PopupMenu.OnMenuItemClickListener {

    //private val TAG = "HomeFragment"

    private val viewModel: AppSettingViewModel by viewModels()
    private val passwordViewModel: PasswordViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var appSetting: AppSetting? = null

    private lateinit var mAdapter: SavedPasswordRVAdapter
    private var pass: String? = ""
    private var account: String? = ""

    private lateinit var mAuth: FirebaseAuth

    private var isPasswordAdded = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = SavedPasswordRVAdapter()

        loadData()

        mAuth = Firebase.auth

        getAppSetting()

        GlobalScope.launch {

            delay(300)
            withContext(Dispatchers.Main) {

                getAllSavedPassword()
            }
        }

        initListeners()
    }

    private fun getAppSetting() {

        viewModel.getAppSettingByID().observe(viewLifecycleOwner, Observer {

            if (it.isNotEmpty()) {

                appSetting = it[0]

                if (it[0].mode == ONLINE
                    && mAuth.currentUser != null
                    && isInternetAvailable(requireContext())
                    && !isPasswordAdded
                ) {
                    syncData()
                }

            }
        })
    }

    private fun syncData() {

        try {
            try {
                passwordViewModel.deleteBySync(SYNCED)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            FirebaseFirestore.getInstance()
                .collection(getString(R.string.savedPasswords))
                .whereEqualTo("uid", appSetting?.uid)
                .get()
                .addOnSuccessListener {

                    var flag = false
                    for (snapshot in it) {

                        val password = snapshot.toObject(Password::class.java)

                        passwordViewModel.insert(password)

                        isPasswordAdded = true
                        saveData()
                        flag = true
                    }

                    if (!flag) {

                        showToast(
                            requireContext(),
                            "You haven't saved any passwords on cloud yet!!!"
                        )
                        saveData()
                    }
                }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveData() {

        val sharedPreferences =
            requireActivity().getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()

        editor.putBoolean(SHARED_PREFERENCE_KEY, isPasswordAdded)

        editor.apply()
    }

    private fun loadData() {

        val sharedPreferences =
            requireActivity().getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)

        try {

            isPasswordAdded = sharedPreferences.getBoolean(SHARED_PREFERENCE_KEY, false)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun getAllSavedPassword() {

        try {

            passwordViewModel.getAllPasswordsList().observe(viewLifecycleOwner, Observer {

                if (it.isNotEmpty()) {

                    hideNoPassTV()
                    setUpRecyclerView(it)

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

                                        val filteredList = it.filter { passwrd ->

                                            passwrd.accountName?.toLowerCase(Locale.ROOT)!!
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

                } else {

                    showNoPassTV()
                    try {
                        setUpRecyclerView(it)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }

            })

        } catch (e: Exception) {
            e.printStackTrace()
        }

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

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onItemClickListener(password: Password?) {

        val action = HomeFragmentDirections
            .actionHomeFragmentToAddPasswordFragment(convertPasswordToJson(password))

        findNavController().navigate(action)
    }

    override fun onSyncBtnClickListener(password: Password?) {

        if (isInternetAvailable(requireContext())) {

            if (password?.isSynced != SYNCED) {
                showToast(requireContext(), " Syncing...")

                password?.isSynced = SYNCED
                password?.key =
                    "${System.currentTimeMillis().toString(36)}_${Random.nextInt(1000, 1000000)
                        .toString(36)}_${password?.uid}"

                passwordViewModel.insert(password!!)
                uploadToFirebase(password)
            } else {

                showToast(requireContext(), "Already Synced")
            }
        } else {
            showToast(requireContext(), NO_INTERNET_MESSAGE)
        }
    }

    override fun onCopyBtnClickListener(password: Password?) {

        pass = password?.password
        account = password?.accountName

        if (appSetting?.enterPasswordForCopy == getString(R.string.t)) {

            openDialog(TARGET_FRAGMENT_REQUEST_CODE2)
        } else {

            copyToClipboard(EncryptData().decryptAES(password?.password, appSetting?.appPassword))
        }

    }

    override fun onDeleteClick(password: Password?) {

        AlertDialog.Builder(requireContext())
            .setTitle("Are Yo Sure?")
            .setPositiveButton("Delete") { dialogInterface, _ ->

                deletePassword(password)
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->

                dialogInterface.dismiss()
            }.create()
            .show()
    }

    private fun deletePassword(password: Password?) {

        password?.let {

            if (password.key == "") {

                passwordViewModel.delete(password)

                try {
                    Snackbar.make(
                        binding.homeFragCoordinatorLayout,
                        "Password Deleted",
                        Snackbar.LENGTH_LONG
                    )
                        .setAction("Undo") {

                            passwordViewModel.insert(password)
                            showToast(requireContext(), "Password Restored")
                        }
                        .show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } else {

                if (isInternetAvailable(requireContext())) {

                    passwordViewModel.delete(password)

                    password.key?.let { key ->
                        FirebaseFirestore.getInstance()
                            .collection(getString(R.string.savedPasswords))
                            .document(key)
                            .delete()
                            .addOnSuccessListener {

                                try {
                                    Snackbar.make(
                                        binding.homeFragCoordinatorLayout,
                                        "Password Deleted",
                                        Snackbar.LENGTH_LONG
                                    )
                                        .setAction("Undo") {

                                            passwordViewModel.insert(password)

                                            uploadToFirebase(password)

                                            showToast(requireContext(), "Password Restored")
                                        }
                                        .show()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                            }
                            .addOnFailureListener {

                                showToast(requireContext(), "${it.message}")
                            }
                    }
                } else {
                    showToast(requireContext(), NO_INTERNET_MESSAGE)
                }
            }

        }

    }

    override fun onEditClick(password: Password?) {

        val action = HomeFragmentDirections
            .actionHomeFragmentToAddPasswordFragment(convertPasswordToJson(password))

        findNavController().navigate(action)
    }

    private fun uploadToFirebase(password: Password?) {

        val passwordString = convertPasswordToJson(password)

        val foregroundServiceIntent =
            Intent(
                requireContext(),
                UploadSavedPasswordService::class.java
            )
        foregroundServiceIntent.putExtra(
            Constants.SAVED_PASSWORD_SERVICE_MESSAGE,
            passwordString
        )

        ContextCompat.startForegroundService(
            requireContext(),
            foregroundServiceIntent
        )
    }

    override fun onSeePasswordBtnClickListener(password: Password?) {

        pass = password?.password
        account = password?.accountName

        if (appSetting?.enterPasswordForVisibility == getString(R.string.t)) {

            openDialog(TARGET_FRAGMENT_REQUEST_CODE)
        } else {

            showPasswordInAlertMessage(
                account,
                EncryptData().decryptAES(pass, appSetting?.appPassword)
            )
        }
    }

    private fun openDialog(requestCode: Int) {

        val dialogFragment = CheckForPasswordDialog().getInstance()
        dialogFragment.setTargetFragment(this, requestCode)
        parentFragmentManager.let { dialogFragment.show(it, "CheckForPasswordDialog") }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == TARGET_FRAGMENT_REQUEST_CODE) {

            data?.getStringExtra(TARGET_FRAGMENT_MESSAGE)?.let {

                if (it != getString(R.string.f)) {

                    try {

                        showPasswordInAlertMessage(account, EncryptData().decryptAES(pass, it))

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                } else {
                    showToast(requireContext(), "Password does not match!!!")
                }
            }
        }
        if (requestCode == TARGET_FRAGMENT_REQUEST_CODE2) {

            data?.getStringExtra(TARGET_FRAGMENT_MESSAGE)?.let {

                if (it != getString(R.string.f)) {

                    try {
                        copyToClipboard(EncryptData().decryptAES(pass, it))

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun copyToClipboard(text: String?) {

        val clipboardManager: ClipboardManager =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val clipData =
            ClipData.newPlainText(
                "password",
                text
            )

        clipboardManager.setPrimaryClip(clipData)
        showToast(requireContext(), "Copied Password")
    }

    //making intent for dialog fragment
    fun newIntent(message: String): Intent {

        val intent = Intent()
        intent.putExtra(TARGET_FRAGMENT_MESSAGE, message)

        return intent
    }

    private fun showPasswordInAlertMessage(accountName: String?, decryptedPass: String?) {

        AlertDialog.Builder(requireContext())
            .setTitle("Your Password for $accountName")
            .setMessage(decryptedPass)
            .setPositiveButton("Ok") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }.create()
            .show()

    }

    private fun initListeners() {

        binding.addPasswordFAB.setOnClickListener(this)
        binding.homeFragCoordinatorLayout.setOnClickListener(this)
        binding.menuBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.addPasswordFAB.id -> {

                findNavController().navigate(R.id.action_homeFragment_to_addPasswordFragment)
            }

            binding.menuBtn.id -> {

                showPopupMenu(binding.menuBtn)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {

            closeKeyboard(requireActivity())
        }
    }

    @SuppressLint("RestrictedApi")
    private fun showPopupMenu(view: View) {

        try {
            val popup = PopupMenu(requireContext(), view)
            popup.menuInflater.inflate(R.menu.home_menu, popup.menu)
            popup.setOnMenuItemClickListener(this)

            val menuHelper = MenuPopupHelper(requireContext(), popup.menu as MenuBuilder, view)
            menuHelper.setForceShowIcon(true)
            menuHelper.gravity = Gravity.END

            menuHelper.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {

        when (item?.itemId) {

            R.id.menu_setting -> {

                findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
            }
        }

        return false
    }

    private fun showNoPassTV() {

        binding.noPassAddedTV.visibility = View.VISIBLE
        binding.savedPasswordRV.visibility = View.GONE
        binding.searchView.visibility = View.GONE
    }

    private fun hideNoPassTV() {

        binding.noPassAddedTV.visibility = View.GONE
        binding.savedPasswordRV.visibility = View.VISIBLE
        binding.searchView.visibility = View.VISIBLE

    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}
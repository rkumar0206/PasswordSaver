package com.rohitthebest.passwordsaver.ui.fragments

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.database.entity.Password
import com.rohitthebest.passwordsaver.databinding.FragmentHomeBinding
import com.rohitthebest.passwordsaver.other.Constants.TARGET_FRAGMENT_REQUEST_CODE
import com.rohitthebest.passwordsaver.other.Functions.Companion.showToast
import com.rohitthebest.passwordsaver.other.encryption.EncryptData
import com.rohitthebest.passwordsaver.ui.adapters.SavedPasswordRVAdapter
import com.rohitthebest.passwordsaver.ui.viewModels.AppSettingViewModel
import com.rohitthebest.passwordsaver.ui.viewModels.PasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), View.OnClickListener,
    SavedPasswordRVAdapter.OnClickListener {

    private val TARGET_FRAGMENT_MESSAGE = "message"

    private val viewModel: AppSettingViewModel by viewModels()
    private val passwordViewModel: PasswordViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var appSetting: AppSetting? = null

    private lateinit var mAdapter: SavedPasswordRVAdapter
    private var pass: String? = ""

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

        getAppSetting()

        GlobalScope.launch {

            delay(150)
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
            }
        })
    }

    private fun getAllSavedPassword() {

        try {

            passwordViewModel.getAllPasswordsList().observe(viewLifecycleOwner, Observer {

                if (it.isNotEmpty()) {

                    setUpRecyclerView(it)

                } else {

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

        //todo : handle click listener
    }

    override fun onSyncBtnClickListener(password: Password?) {

        showToast(requireContext(), "sync Button Clicked")
    }

    override fun onCopyBtnClickListener(password: Password?) {

        val clipboardManager: ClipboardManager =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val clipData =
            ClipData.newPlainText(
                "password",
                EncryptData().decryptAES(password?.password, appSetting?.appPassword)
            )

        clipboardManager.setPrimaryClip(clipData)
        showToast(requireContext(), "Copied Password")
    }

    override fun onSeePasswordBtnClickListener(password: Password?) {

        pass = password?.password

        val dialogFragment = MyDialogFragment().getInstance()
        dialogFragment.setTargetFragment(this, TARGET_FRAGMENT_REQUEST_CODE)

        parentFragmentManager.let { dialogFragment.show(it, "MyDialogFragment") }
    }

    override fun onDeleteClick(password: Password?) {

        //todo : handle delete
    }

    override fun onEditClick(password: Password?) {

        //todo : edit password
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == TARGET_FRAGMENT_REQUEST_CODE) {

            data?.getStringExtra(TARGET_FRAGMENT_MESSAGE)?.let {

                if (it != getString(R.string.f)) {

                    var decryptedPass: String? = ""
                    try {

                        decryptedPass = EncryptData().decryptAES(pass, it)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    AlertDialog.Builder(requireContext())
                        .setTitle("Your Password")
                        .setMessage(decryptedPass)
                        .setPositiveButton("Ok") { dialogInterface, _ ->
                            dialogInterface.dismiss()
                        }.create()
                        .show()

                } else {
                    showToast(requireContext(), "Password does not match!!!")
                }
            }
        }
    }

    //making intent for dialog fragment
    fun newIntent(message: String): Intent {

        val intent = Intent()
        intent.putExtra(TARGET_FRAGMENT_MESSAGE, message)

        return intent
    }

    private fun initListeners() {

        binding.addPasswordFAB.setOnClickListener(this)
        binding.menuBtn.setOnClickListener(this)
    }


    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }


    override fun onClick(v: View?) {

        when (v?.id) {

            binding.addPasswordFAB.id -> {

                findNavController().navigate(R.id.action_homeFragment_to_addPasswordFragment)
            }

            binding.menuBtn.id -> {

                //todo : Show popup menu
            }
        }
    }

}
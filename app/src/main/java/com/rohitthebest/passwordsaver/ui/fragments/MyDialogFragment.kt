package com.rohitthebest.passwordsaver.ui.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.other.Functions.Companion.closeKeyboard
import com.rohitthebest.passwordsaver.other.Functions.Companion.showToast
import com.rohitthebest.passwordsaver.other.encryption.EncryptData
import com.rohitthebest.passwordsaver.ui.viewModels.AppSettingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_my_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyDialogFragment : DialogFragment() {

    private val viewModel: AppSettingViewModel by viewModels()

    private var appSetting: AppSetting? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_my_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getAppSetting()

        confirmBtn.setOnClickListener {

            try {

                if (dialogPasswordET?.editText?.text.toString().trim().isNotBlank()) {

                    checkPassword(dialogPasswordET?.editText?.text.toString().trim())
                } else {

                    showToast(requireContext(), "No password entered!!!")
                    dismiss()
                }

                CoroutineScope(Dispatchers.IO).launch {

                    closeKeyboard(requireActivity())
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getAppSetting() {

        viewModel.getAppSettingByID().observe(viewLifecycleOwner, Observer {

            if (it.isNotEmpty()) {

                appSetting = it[0]
            }
        })
    }

    private fun checkPassword(password: String) {

        if (EncryptData().encryptWithSHA(password) == appSetting?.appPassword) {

            sendMessage(appSetting?.appPassword!!)
        } else {

            sendMessage(getString(R.string.f))
        }

    }

    private fun sendMessage(message: String) {

        try {
            if (targetFragment == null) {
                return
            }

            val intent = HomeFragment().newIntent(message)

            targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
            dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(requireContext(), "Something went wrong!!!")
        }
    }

    fun getInstance(): MyDialogFragment {

        return MyDialogFragment()
    }
}
package com.rohitthebest.passwordsaver.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.database.entity.AppSetting
import com.rohitthebest.passwordsaver.databinding.FragmentSettingsBinding
import com.rohitthebest.passwordsaver.other.Constants.OFFLINE
import com.rohitthebest.passwordsaver.other.Constants.ONLINE
import com.rohitthebest.passwordsaver.ui.viewModels.AppSettingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment(), View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private val appSettingViewModel: AppSettingViewModel by viewModels()

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var appSetting: AppSetting? = null

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

        initListeners()
        getAppSetting()


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
    }

    private fun initListeners() {

        binding.backBtn.setOnClickListener(this)
        binding.saveBtn.setOnClickListener(this)
        binding.changePasswordIB.setOnClickListener(this)
        binding.changePasswordTV.setOnClickListener(this)
        binding.modeChangeRG.setOnCheckedChangeListener(this)
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

            //todo : open change password dialog
        }

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

                    //todo : delete all passwords from firestore databse
                    OFFLINE
                } else {
                    //todo : upload appSetting to database
                    //todo : update the field isSynced in sqlite database
                    ONLINE
                }

            appSettingViewModel.insert(it)
        }

    }

    override fun onCheckedChanged(radioGroup: RadioGroup?, checkedId: Int) {

        if (checkedId == binding.offlineModeRB.id) {

            val message =
                "${getString(R.string.offline_text)}\n2 .If you choose offline mode all " +
                        "the passwords saved on cloud will" +
                        " be permanently deleted and cannot be retrieved again."
            showAlertMessage(message)
        } else {
            showAlertMessage(getString(R.string.online_text))
        }

    }

    private fun showAlertMessage(message: String) {

        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton("Ok") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }.create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}
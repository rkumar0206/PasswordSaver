package com.rohitthebest.passwordsaver.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.databinding.FragmentAppSetupBinding
import com.rohitthebest.passwordsaver.other.Constants.EDITTEXT_EMPTY_MESSAGE
import com.rohitthebest.passwordsaver.ui.viewModels.AppSettingViewModel

class AppSetupFragment : Fragment(), View.OnClickListener,
    RadioGroup.OnCheckedChangeListener {

    private val viewModel: AppSettingViewModel by viewModels()

    private var _binding: FragmentAppSetupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

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

        viewModel.getAppSetting().observe(viewLifecycleOwner, Observer {

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
        //binding.include.fingerPrintCB.setOnCheckedChangeListener(this)
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.nextBtn.id -> {

                //todo : save app setting to the database
            }

        }
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {

        when (checkedId) {

            binding.include.modeOfflineRB.id -> {

                binding.include.selectedModeTV.text = getString(R.string.offline)
            }

            binding.include.modeOnlineRB.id -> {

                binding.include.selectedModeTV.text = getString(R.string.online)
            }

            binding.include.modeTrySignInRB.id -> {

                binding.include.selectedModeTV.text = getString(R.string.trySignIn_text)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}
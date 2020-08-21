package com.rohitthebest.passwordsaver.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rohitthebest.passwordsaver.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

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
    }

    private fun initListeners() {

        binding.backBtn.setOnClickListener(this)
        binding.saveBtn.setOnClickListener(this)
        binding.changePasswordIB.setOnClickListener(this)
        binding.changePasswordTV.setOnClickListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
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

        if (v?.id == binding.changePasswordIB.id || v?.id == binding.changePasswordTV.id) {

            //todo : open change password dialog
        }

    }
}
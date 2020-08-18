package com.rohitthebest.passwordsaver.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.databinding.FragmentHomeBinding
import com.rohitthebest.passwordsaver.ui.viewModels.AppSettingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: AppSettingViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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


        getAppSettingsData()
    }

    private fun getAppSettingsData() {

/*
        try {

            viewModel.getAppSettingByID().observe(viewLifecycleOwner, Observer {

                if (it.isEmpty()) {

                    findNavController().navigate(R.id.action_homeFragment_to_introductionFragment)

                } else {

                    if (it[0].appPassword == "") {

                        findNavController().navigate(R.id.action_homeFragment_to_appPasswordFragment)
                    }
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
        }
*/

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
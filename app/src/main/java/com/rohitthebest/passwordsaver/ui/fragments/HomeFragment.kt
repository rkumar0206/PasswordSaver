package com.rohitthebest.passwordsaver.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.databinding.FragmentHomeBinding
import com.rohitthebest.passwordsaver.ui.viewModels.AppSettingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), View.OnClickListener {

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


        initListeners()

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
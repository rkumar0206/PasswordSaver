package com.rohitthebest.passwordsaver.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.databinding.FragmentAddPasswordBinding

class AddPasswordFragment : Fragment(R.layout.fragment_add_password) {

    private var _binding : FragmentAddPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAddPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}

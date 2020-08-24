package com.rohitthebest.passwordsaver.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.rohitthebest.passwordsaver.R
import kotlinx.android.synthetic.main.help_fragment_layout.*

class HelpFragment : Fragment(R.layout.fragment_help) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backBtn.setOnClickListener {

            requireActivity().onBackPressed()
        }
    }
}
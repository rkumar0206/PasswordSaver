package com.rohitthebest.passwordsaver.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.databinding.FragmentAddPasswordBinding
import com.rohitthebest.passwordsaver.other.Constants.EDITTEXT_EMPTY_MESSAGE
import com.rohitthebest.passwordsaver.other.Functions
import com.rohitthebest.passwordsaver.util.CheckPasswordPattern
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddPasswordFragment : Fragment(R.layout.fragment_add_password), View.OnClickListener {

    private var _binding: FragmentAddPasswordBinding? = null
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

        initListeners()
        textWatcher()
    }

    private fun initListeners() {

        binding.backBtn.setOnClickListener(this)
        binding.saveBtn.setOnClickListener(this)
        binding.checkPasswordStrengthBtn.setOnClickListener(this)
        binding.mainCL.setOnClickListener(this) // for closing keyboard
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.saveBtn.id -> {

                //todo : save password to database
            }

            binding.backBtn.id -> {

                requireActivity().onBackPressed()
            }

            binding.checkPasswordStrengthBtn.id -> {

                if (binding.passwordStrengthCL.visibility == View.VISIBLE) {

                    hideCheckPassStrengthCL()
                } else {
                    showCheckPassStrengthCL()
                }
            }

        }

        //closing keyboard if opened
        CoroutineScope(Dispatchers.IO).launch {

            Functions.closeKeyboard(requireActivity())
        }
    }

    private fun textWatcher() {


        binding.accountNameET.editText?.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(str: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (str?.isEmpty()!!) {

                    binding.accountNameET.error = EDITTEXT_EMPTY_MESSAGE
                } else {

                    binding.accountNameET.error = null
                }
            }
        })

        binding.passwordET.editText?.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(str: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (str?.isEmpty()!!) {

                    binding.passwordET.error = EDITTEXT_EMPTY_MESSAGE
                } else {

                    checkPasswordStrength(str.toString())
                    binding.passwordET.error = null
                }
            }
        })
    }

    private fun checkPasswordStrength(passwordString: String) {

        if (CheckPasswordPattern().check_for_a_capital_letter(passwordString) &&
            CheckPasswordPattern().check_for_a_small_letter(passwordString)
        ) {

            binding.uppercaseLowercaseIndicator.setImageResource(R.drawable.ic_baseline_check_24)
        } else {

            binding.uppercaseLowercaseIndicator.setImageResource(R.drawable.ic_baseline_close_24)

        }

        if (CheckPasswordPattern().check_for_min_length(passwordString)) {

            binding.atleast8CharacterIndicator.setImageResource(R.drawable.ic_baseline_check_24)

        } else {

            binding.atleast8CharacterIndicator.setImageResource(R.drawable.ic_baseline_close_24)

        }

        if (CheckPasswordPattern().check_for_special_character(passwordString)) {

            binding.specialCharacterIndicator.setImageResource(R.drawable.ic_baseline_check_24)

        } else {

            binding.specialCharacterIndicator.setImageResource(R.drawable.ic_baseline_close_24)

        }

        if (CheckPasswordPattern().check_for_digit(passwordString)) {

            binding.aDigitIndicator.setImageResource(R.drawable.ic_baseline_check_24)
        } else {

            binding.aDigitIndicator.setImageResource(R.drawable.ic_baseline_close_24)

        }

        if (passwordString.contains("<") || passwordString.contains(">")) {

            binding.dontuseIndicator.setImageResource(R.drawable.ic_baseline_close_24)
        } else {

            binding.dontuseIndicator.setImageResource(R.drawable.ic_baseline_check_24)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null

        //closing keyboard if opened
        try {
            CoroutineScope(Dispatchers.IO).launch {

                Functions.closeKeyboard(requireActivity())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showCheckPassStrengthCL() {

        binding.passwordStrengthCL.visibility = View.VISIBLE
    }

    private fun hideCheckPassStrengthCL() {

        binding.passwordStrengthCL.visibility = View.INVISIBLE
    }

}

package com.rohitthebest.passwordsaver.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.databinding.FragmentPasswordGeneratorBinding
import com.rohitthebest.passwordsaver.databinding.PasswordGeneratorLayoutBinding
import com.rohitthebest.passwordsaver.other.Constants.CAPITAL_CASE_LETTERS
import com.rohitthebest.passwordsaver.other.Constants.LOWER_CASE_LETTERS
import com.rohitthebest.passwordsaver.other.Constants.NUMBERS
import com.rohitthebest.passwordsaver.other.Constants.SPECIAL_CHARACTERS
import com.rohitthebest.passwordsaver.util.*
import kotlin.random.Random

//private const val TAG = "PasswordGeneratorFragme"

class PasswordGeneratorFragment : Fragment(R.layout.fragment_password_generator) {

    private var _binding: FragmentPasswordGeneratorBinding? = null
    private val binding get() = _binding!!
    private lateinit var includeBinding: PasswordGeneratorLayoutBinding

    private var passwordLength = 8

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPasswordGeneratorBinding.bind(view)

        includeBinding = binding.include
        includeBinding.passwordLengthTV.text = passwordLength.toString()
        generatePassword()
        initListeners()
    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        includeBinding.passwordLengthSeekbar.onProgressChangeListener { progress ->

            passwordLength = progress + 4

            includeBinding.passwordLengthTV.text = passwordLength.toString()
        }

        includeBinding.generatePasswordBtn.setOnClickListener {

            generatePassword()
        }

        includeBinding.copyPasswordFAB.setOnClickListener {

            Functions.copyToClipBoard(
                requireActivity(),
                includeBinding.passwordTV.text.toString().trim()
            )
        }

        checkBoxListeners()

    }

    private fun checkBoxListeners() {

        includeBinding.includeOnlyDigitsCB.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {

                includeBinding.upperLowerCaseMixtureCB.isChecked = false
                includeBinding.includeSpecialCharactersCB.isChecked = false
                includeBinding.includeDigitsCB.isChecked = false
            }
        }

        includeBinding.includeSpecialCharactersCB.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {

                includeBinding.includeOnlyDigitsCB.isChecked = false
            }
        }
        includeBinding.upperLowerCaseMixtureCB.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {

                includeBinding.includeOnlyDigitsCB.isChecked = false
            }
        }
        includeBinding.includeDigitsCB.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {

                includeBinding.includeOnlyDigitsCB.isChecked = false
            }
        }
    }

    private fun generatePassword() {

        var password = ""

        var combinedCharacters = LOWER_CASE_LETTERS

        if (includeBinding.includeOnlyDigitsCB.isChecked) {

            combinedCharacters = NUMBERS
        } else {

            if (includeBinding.includeSpecialCharactersCB.isChecked) {

                combinedCharacters += SPECIAL_CHARACTERS
            }

            if (includeBinding.includeDigitsCB.isChecked) {

                combinedCharacters += NUMBERS
            }

            if (includeBinding.upperLowerCaseMixtureCB.isChecked) {

                combinedCharacters += CAPITAL_CASE_LETTERS
            }
        }

        if (includeBinding.mustIncludeET.isTextValid()) {

            val l = includeBinding.mustIncludeET.getLength()

            val text = includeBinding.mustIncludeET.text.toString().trim()

            // if edittext length is less than the selected password length then reduce the
            // length of the selected password length so that the left charactes will be appended
            // to the string
            if (l < passwordLength) {

                passwordLength -= l

                password += text
            } else {

                // if the edittext length is greater than or equal to the password length
                // then shuffle the characters exit the function

                password += text.shuffle() // extension function defined in ExtensionFunctions.kt
                includeBinding.passwordTV.text = password
                return
            }
        }

        for (i in 0 until passwordLength) {

            password += combinedCharacters[Random.nextInt(combinedCharacters.length)]
        }

        // after generating the string add the subtracted length from the specified password length
        if (includeBinding.mustIncludeET.isTextValid()) {

            passwordLength += includeBinding.mustIncludeET.getLength()
        }

        includeBinding.passwordTV.text = password.shuffle()
    }


    override fun onDestroyView() {
        super.onDestroyView()

        Functions.hideKeyBoard(requireActivity())

        _binding = null
    }
}


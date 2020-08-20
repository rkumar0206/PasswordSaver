package com.rohitthebest.passwordsaver.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.database.entity.Password
import com.rohitthebest.passwordsaver.databinding.FragmentHomeBinding
import com.rohitthebest.passwordsaver.other.Constants.TARGET_FRAGMENT_REQUEST_CODE
import com.rohitthebest.passwordsaver.other.Functions.Companion.showToast
import com.rohitthebest.passwordsaver.ui.adapters.SavedPasswordRVAdapter
import com.rohitthebest.passwordsaver.ui.viewModels.PasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), View.OnClickListener,
    SavedPasswordRVAdapter.OnClickListener {

    private val TARGET_FRAGMENT_MESSAGE = "message"

    //private val viewModel: AppSettingViewModel by viewModels()
    private val passwordViewModel: PasswordViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var mAdapter: SavedPasswordRVAdapter

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


        mAdapter = SavedPasswordRVAdapter()

        GlobalScope.launch {

            delay(150)
            withContext(Dispatchers.Main) {

                getAllSavedPassword()
            }
        }

        initListeners()
    }

    private fun getAllSavedPassword() {

        try {

            passwordViewModel.getAllPasswordsList().observe(viewLifecycleOwner, Observer {

                if (it.isNotEmpty()) {

                    setUpRecyclerView(it)

                } else {

                    try {
                        setUpRecyclerView(it)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }

            })

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun setUpRecyclerView(passwordList: List<Password>?) {

        try {

            passwordList?.let {

                mAdapter.submitList(it)

                binding.savedPasswordRV.apply {

                    layoutManager = LinearLayoutManager(requireContext())
                    setHasFixedSize(true)
                    adapter = mAdapter

                }
                mAdapter.setOnClickListener(this)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onItemClickListener(password: Password?) {

        showToast(
            requireContext(), "accountName : ${password?.accountName}\n" +
                    "password : ${password?.password}\n" +
                    "isSynced : ${password?.isSynced}\n" +
                    "uid : ${password?.uid}\\n" +
                    "timeStamp : ${password?.timeStamp}"
            , Toast.LENGTH_LONG
        )
    }

    override fun onSyncBtnClickListener(password: Password?) {

        showToast(requireContext(), "sync Button Clicked")
    }

    override fun onCopyBtnClickListener(password: Password?) {

        showToast(requireContext(), "Copy Btn Clicked")
    }

    override fun onSeePasswordBtnClickListener(password: Password?) {

        val dialogFragment = MyDialogFragment().getInstance()
        dialogFragment.setTargetFragment(this, TARGET_FRAGMENT_REQUEST_CODE)

        parentFragmentManager.let { dialogFragment.show(it, "MyDialogFragment") }

        showToast(requireContext(), "Password visibility clicked")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == TARGET_FRAGMENT_REQUEST_CODE) {

            //todo : do something with received text
            //data?.getStringExtra(TARGET_FRAGMENT_MESSAGE)?.let { showToast(requireContext(), it) }
        }
    }

    //making intent for dialog fragment
    fun newIntent(message: String): Intent {

        val intent = Intent()
        intent.putExtra(TARGET_FRAGMENT_MESSAGE, message)

        return intent
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
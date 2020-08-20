package com.rohitthebest.passwordsaver.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.database.entity.Password
import com.rohitthebest.passwordsaver.databinding.FragmentHomeBinding
import com.rohitthebest.passwordsaver.ui.adapters.SavedPasswordRVAdapter
import com.rohitthebest.passwordsaver.ui.viewModels.PasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), View.OnClickListener {

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

            delay(250)
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
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
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
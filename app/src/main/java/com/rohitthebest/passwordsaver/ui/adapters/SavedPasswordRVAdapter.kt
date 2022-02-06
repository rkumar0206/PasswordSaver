package com.rohitthebest.passwordsaver.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.passwordsaver.database.entity.Password
import com.rohitthebest.passwordsaver.databinding.AdapterShowSavedPasswordBinding

class SavedPasswordRVAdapter :
    ListAdapter<Password, SavedPasswordRVAdapter.ShowPasswordViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class ShowPasswordViewHolder(val binding: AdapterShowSavedPasswordBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {

            binding.root.setOnClickListener(this)
        }

        fun setData(password: Password?) {

            password?.let {

                binding.apply {

                    siteNameTV.text = if (it.siteName != "") {

                        it.siteName
                    } else {
                        it.userName
                    }
                    userIdTV.text = it.userName

                }
            }
        }

        override fun onClick(v: View?) {

            when (v?.id) {

                binding.root.id -> {

                    if (absoluteAdapterPosition != RecyclerView.NO_POSITION && mListener != null) {

                        mListener?.onItemClick(getItem(absoluteAdapterPosition))
                    }
                }

            }

        }
    }

    companion object {

        class DiffUtilCallback : DiffUtil.ItemCallback<Password>() {

            override fun areItemsTheSame(oldItem: Password, newItem: Password): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Password, newItem: Password): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowPasswordViewHolder {

        val binding = AdapterShowSavedPasswordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ShowPasswordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShowPasswordViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClick(password: Password?)
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }
}
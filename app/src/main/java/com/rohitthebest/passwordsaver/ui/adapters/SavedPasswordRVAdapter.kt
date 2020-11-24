package com.rohitthebest.passwordsaver.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.passwordsaver.database.entity.Password
import com.rohitthebest.passwordsaver.databinding.AdapterShowSavedPasswordBinding

class SavedPasswordRVAdapter : ListAdapter<Password,
        SavedPasswordRVAdapter.SavedPasswordViewHolder>(DiffUtilCallBack()) {

    private var mListener: OnClickListener? = null

    inner class SavedPasswordViewHolder(private val itemBinding: AdapterShowSavedPasswordBinding) :
        RecyclerView.ViewHolder(itemBinding.root), View.OnClickListener {

        fun setData(password: Password?) {

            itemBinding.apply {

                siteNameTV.text = if (password?.siteName != "") {

                    password?.siteName
                } else {
                    "Not added"
                }
                userIdTV.text = password?.userName
            }
        }

        init {

            itemBinding.adapterSavedPasswordCV.setOnClickListener(this)
            itemBinding.adapterSyncBtn.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

            when (v?.id) {

                itemBinding.adapterSavedPasswordCV.id -> {

                    if (absoluteAdapterPosition != RecyclerView.NO_POSITION && mListener != null) {

                        mListener?.onItemClickListener(getItem(absoluteAdapterPosition))
                    }
                }

                itemBinding.adapterSyncBtn.id -> {

                    if (absoluteAdapterPosition != RecyclerView.NO_POSITION && mListener != null) {

                        mListener?.onSyncBtnClickListener(getItem(absoluteAdapterPosition))
                    }
                }
            }
        }
    }

    class DiffUtilCallBack : DiffUtil.ItemCallback<Password>() {
        override fun areItemsTheSame(oldItem: Password, newItem: Password): Boolean {

            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Password, newItem: Password): Boolean {

            return oldItem.id == newItem.id &&
                    oldItem.userName == newItem.userName &&
                    oldItem.isSynced == newItem.isSynced &&
                    oldItem.password == newItem.password &&
                    oldItem.timeStamp == newItem.timeStamp
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedPasswordViewHolder {

        val itemBinding = AdapterShowSavedPasswordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return SavedPasswordViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: SavedPasswordViewHolder, position: Int) {

        holder.setData(getItem(position))
    }

    interface OnClickListener {

        fun onItemClickListener(password: Password?)
        fun onSyncBtnClickListener(password: Password?)
    }

    fun setOnClickListener(listener: OnClickListener) {

        mListener = listener
    }
}
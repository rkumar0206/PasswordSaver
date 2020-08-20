package com.rohitthebest.passwordsaver.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.database.entity.Password
import com.rohitthebest.passwordsaver.databinding.SavedPasswordAdapterLayoutBinding
import com.rohitthebest.passwordsaver.other.Constants.SYNCED

class SavedPasswordRVAdapter : ListAdapter<Password,
        SavedPasswordRVAdapter.SavedPasswordViewHolder>(DiffUtilCallBack()) {

    private var mListener: OnClickListener? = null

    inner class SavedPasswordViewHolder(private val itemBinding: SavedPasswordAdapterLayoutBinding) :
        RecyclerView.ViewHolder(itemBinding.root), View.OnClickListener {

        fun setData(password: Password?) {

            itemBinding.apply {

                accountNameTV.text = password?.accountName
                passwordTV.text = password?.password
                if (password?.uid == "") {

                    syncBtn.visibility = View.GONE
                } else {

                    syncBtn.visibility = View.VISIBLE

                    if (password?.isSynced == SYNCED) {

                        syncBtn.setImageResource(R.drawable.ic_baseline_sync_24)
                    } else {

                        syncBtn.setImageResource(R.drawable.ic_baseline_sync_disabled_24)
                    }
                }

            }
        }

        init {

            itemBinding.cardView.setOnClickListener(this)
            itemBinding.copyBtn.setOnClickListener(this)
            itemBinding.syncBtn.setOnClickListener(this)
            itemBinding.visibilityBtn.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

            when (v?.id) {

                itemBinding.cardView.id -> {

                    if (absoluteAdapterPosition != RecyclerView.NO_POSITION && mListener != null) {

                        mListener?.onItemClickListener(getItem(absoluteAdapterPosition))
                    }
                }

                itemBinding.visibilityBtn.id -> {

                    if (absoluteAdapterPosition != RecyclerView.NO_POSITION && mListener != null) {

                        mListener?.onSeePasswordBtnClickListener(getItem(absoluteAdapterPosition))
                    }
                }

                itemBinding.copyBtn.id -> {

                    if (absoluteAdapterPosition != RecyclerView.NO_POSITION && mListener != null) {

                        mListener?.onCopyBtnClickListener(getItem(absoluteAdapterPosition))
                    }
                }

                itemBinding.syncBtn.id -> {

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
                    oldItem.accountName == newItem.accountName &&
                    oldItem.isSynced == newItem.isSynced &&
                    oldItem.password == newItem.password &&
                    oldItem.timeStamp == newItem.timeStamp
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedPasswordViewHolder {

        val itemBinding = SavedPasswordAdapterLayoutBinding.inflate(
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
        fun onCopyBtnClickListener(password: Password?)
        fun onSeePasswordBtnClickListener(password: Password?)
    }

    fun setOnClickListener(listener: OnClickListener) {

        mListener = listener
    }
}
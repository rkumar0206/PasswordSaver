package com.rohitthebest.passwordsaver.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.database.entity.Password
import com.rohitthebest.passwordsaver.other.Constants.NOT_SYNCED
import com.rohitthebest.passwordsaver.other.Constants.OFFLINE
import com.rohitthebest.passwordsaver.util.Functions.Companion.hide
import com.rohitthebest.passwordsaver.util.Functions.Companion.show
import kotlinx.android.synthetic.main.adapter_show_saved_password.view.*

class SavedPasswordRVAdapter :
    ListAdapter<Password, SavedPasswordRVAdapter.ShowPasswordViewHolder>(DiffUtilCallback()) {

    private var mListener: OnClickListener? = null

    inner class ShowPasswordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        fun setData(password: Password?) {

            password?.let {

                itemView.apply {

                    siteNameTV.text = if (it.siteName != "") {

                        it.siteName
                    } else {
                        "Not added"
                    }
                    userIdTV.text = it.userName

                    if (it.mode == OFFLINE) {

                        adapterSyncBtn.hide()
                    } else {

                        adapterSyncBtn.show()

                        if (it.isSynced == NOT_SYNCED) {

                            adapterSyncBtn.setImageResource(R.drawable.ic_baseline_sync_disabled_24)
                        } else {

                            adapterSyncBtn.setImageResource(R.drawable.ic_baseline_sync_24)
                        }
                    }

                }
            }
        }

        init {

            itemView.setOnClickListener(this)
            itemView.adapterSyncBtn.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

            when (v?.id) {

                itemView.id -> {

                    if (absoluteAdapterPosition != RecyclerView.NO_POSITION && mListener != null) {

                        mListener?.onItemClickListener(getItem(absoluteAdapterPosition))
                    }
                }

                itemView.adapterSyncBtn.id -> {

                    if (absoluteAdapterPosition != RecyclerView.NO_POSITION && mListener != null) {

                        mListener?.onSyncBtnClickListener(getItem(absoluteAdapterPosition))
                    }
                }
            }
        }
    }

    class DiffUtilCallback : DiffUtil.ItemCallback<Password>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowPasswordViewHolder {

        return ShowPasswordViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_show_saved_password, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ShowPasswordViewHolder, position: Int) {

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

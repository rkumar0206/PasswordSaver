package com.rohitthebest.passwordsaver.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rohitthebest.passwordsaver.R
import com.rohitthebest.passwordsaver.database.entity.Password

class SavedPasswordRVAdapter : ListAdapter<Password,
        SavedPasswordRVAdapter.SavedPasswordViewHolder>(DiffUtilCallBack()) {

    inner class SavedPasswordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

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

        return SavedPasswordViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.saved_password_adapter_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SavedPasswordViewHolder, position: Int) {


    }

}
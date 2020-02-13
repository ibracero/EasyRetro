package com.ibracero.retrum.ui.board.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ibracero.retrum.R
import kotlinx.android.synthetic.main.item_user.view.*

class UserListAdapter : ListAdapter<String, UserViewHolder>(UserDiffCalback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder =
        UserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false))

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(userEmail: String) {
        itemView.user_label.text = if (userEmail.isNotEmpty()) userEmail[0].toUpperCase().toString() else ""
    }
}

class UserDiffCalback : DiffUtil.ItemCallback<String>() {

    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
        oldItem == newItem
}
package com.easyretro.ui.board.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.easyretro.R
import com.easyretro.common.CachedRecyclerViewHolder
import com.easyretro.common.extensions.gone
import com.easyretro.common.extensions.visible
import com.easyretro.data.local.User
import kotlinx.android.synthetic.main.item_user.*
import java.util.*

class UserListAdapter : ListAdapter<User, UserViewHolder>(UserDiffCalback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder =
        UserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false))

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class UserViewHolder(view: View) : CachedRecyclerViewHolder(view) {

    fun bind(user: User) {
        if (user.photoUrl.isNotEmpty()) {
            user_label.gone()
            user_image.visible()

            Glide.with(containerView)
                .load(user.photoUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(user_image)
        } else {
            user_label.text = getInitials(user).toUpperCase(Locale.getDefault())
            user_image.gone()
            user_label.visible()
        }
    }

    private fun getInitials(user: User) =
        listOf(user.firstName, user.lastName).filter { it.isNotEmpty() }.map { it[0] }.joinToString("")
}

class UserDiffCalback : DiffUtil.ItemCallback<User>() {

    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
        oldItem.email == newItem.email

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
        oldItem == newItem
}
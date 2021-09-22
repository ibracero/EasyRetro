package com.easyretro.ui.board.users

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.easyretro.common.extensions.gone
import com.easyretro.common.extensions.visible
import com.easyretro.databinding.ItemUserBinding
import com.easyretro.domain.model.User
import java.util.*

class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = ItemUserBinding.bind(itemView)

    fun bind(user: User) {
        with(binding) {
            if (user.photoUrl.isNotEmpty()) {
                userLabel.gone()
                userImage.visible()

                Glide.with(itemView.context)
                    .load(user.photoUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(userImage)
            } else {
                userLabel.text = getInitials(user).toUpperCase(Locale.getDefault())
                userImage.gone()
                userLabel.visible()
            }
        }
    }

    private fun getInitials(user: User): String {
        val initials = listOf(user.firstName, user.lastName)
            .filter { it.isNotEmpty() }
            .map { it[0] }
            .joinToString("")

        return if (initials.isNotEmpty()) initials
        else user.email.getOrElse(0) { '?' }.toString()
    }
}
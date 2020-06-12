package com.easyretro.ui.board.users

import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.easyretro.common.CachedRecyclerViewHolder
import com.easyretro.common.extensions.gone
import com.easyretro.common.extensions.visible
import com.easyretro.domain.model.User
import kotlinx.android.synthetic.main.item_user.*
import java.util.*

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

    private fun getInitials(user: User): String {
        val initials = listOf(user.firstName, user.lastName)
            .filter { it.isNotEmpty() }
            .map { it[0] }
            .joinToString("")

        return if (initials.isNotEmpty()) initials
        else user.email.getOrElse(0) { '?' }.toString()
    }
}
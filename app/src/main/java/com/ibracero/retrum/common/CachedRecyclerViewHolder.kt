package com.ibracero.retrum.common

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer

open class CachedRecyclerViewHolder(
    override val containerView: View
) : RecyclerView.ViewHolder(containerView), LayoutContainer
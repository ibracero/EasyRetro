package com.ibracero.retrum.ui.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ibracero.retrum.R

class ActionsFragment : Fragment() {

    private lateinit var actionsViewModel: ActionsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        actionsViewModel =
            ViewModelProviders.of(this).get(ActionsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_actions, container, false)
        val textView: TextView = root.findViewById(R.id.text_notifications)
        actionsViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}
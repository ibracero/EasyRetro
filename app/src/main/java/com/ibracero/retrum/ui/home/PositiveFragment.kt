package com.ibracero.retrum.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ibracero.retrum.R

class PositiveFragment : Fragment() {

    private lateinit var positiveViewModel: PositiveViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        positiveViewModel =
            ViewModelProviders.of(this).get(PositiveViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_positive, container, false)
        val textView: TextView = root.findViewById(R.id.text_positive)
        positiveViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}
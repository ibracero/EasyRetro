package com.ibracero.retrum.ui.negative

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ibracero.retrum.R

class NegativeFragment : Fragment() {

    private lateinit var negativeViewModel: NegativeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        negativeViewModel =
            ViewModelProviders.of(this).get(NegativeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_negative, container, false)
        val textView: TextView = root.findViewById(R.id.text_dashboard)
        negativeViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}
package com.ibracero.retrum.ui.positive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ibracero.retrum.R
import kotlinx.android.synthetic.main.fragment_positive.*
import org.koin.android.viewmodel.ext.android.viewModel

class PositiveFragment : Fragment() {

    private val positiveViewModel: PositiveViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_positive, container, false)
//        positiveViewModel.text.observe(this, Observer { text_positive.text = it })
        create_retro.setOnClickListener { positiveViewModel.openRetro() }
        return root
    }
}
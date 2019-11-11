package com.ibracero.retrum.ui.positive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.ibracero.retrum.R
import com.ibracero.retrum.data.local.Statement
import kotlinx.android.synthetic.main.fragment_positive.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber

class PositiveFragment : Fragment() {

    private val positiveViewModel: PositiveViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_positive, container, false)
        positiveViewModel.positivePoints.observe(this, Observer { processPositivePoints(it) })
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        add_statement.setOnClickListener { positiveViewModel.addPositivePoint("") }
    }

    private fun processPositivePoints(positivePoints: List<Statement>) {
        Timber.d("processing $positivePoints")
    }
}
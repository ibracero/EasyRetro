package com.ibracero.retrum.ui.board.positive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.ibracero.retrum.R
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.data.local.Statement
import com.ibracero.retrum.ui.BottomNavFragment.Companion.RETRO_ATTR
import kotlinx.android.synthetic.main.fragment_positive.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber

class PositiveFragment : Fragment() {

    private val positiveViewModel: PositiveViewModel by viewModel()

    private val adapter = StatementListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_positive, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        
        positiveViewModel
            .getPositivePoints(getRetroArgument().uuid)
            .observe(this, Observer { processPositivePoints(it) })
    }

    private fun initUi() {
        positive_recycler_view.adapter = adapter
    }

    private fun processPositivePoints(positivePoints: List<Statement>) {
        adapter.submitList(positivePoints)
    }

    private fun getRetroArgument(): Retro = arguments?.getSerializable(RETRO_ATTR) as Retro
}
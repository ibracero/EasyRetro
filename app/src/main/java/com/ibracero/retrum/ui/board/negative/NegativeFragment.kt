package com.ibracero.retrum.ui.board.negative

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.ibracero.retrum.R
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.data.local.Statement
import com.ibracero.retrum.ui.BottomNavFragment.Companion.ARGUMENT_RETRO
import com.ibracero.retrum.ui.board.StatementListAdapter
import kotlinx.android.synthetic.main.statement_list.*
import org.koin.android.viewmodel.ext.android.viewModel

class NegativeFragment : Fragment() {

    private val negativeViewModel: NegativeViewModel by viewModel()

    private val adapter = StatementListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.statement_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()

        negativeViewModel
            .getNegativePoints(getRetroArgument()?.uuid.orEmpty())
            .observe(this, Observer { processStatements(it) })

        (activity as AppCompatActivity?)?.supportActionBar?.title = getRetroArgument()?.title.orEmpty()
    }

    private fun initUi() {
        statement_recycler_view.adapter = adapter
    }

    private fun processStatements(positivePoints: List<Statement>) {
        adapter.submitList(positivePoints)
    }

    private fun getRetroArgument(): Retro? = arguments?.getSerializable(ARGUMENT_RETRO) as Retro?
}
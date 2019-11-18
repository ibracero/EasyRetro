package com.ibracero.retrum.ui.board

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.ibracero.retrum.R
import com.ibracero.retrum.common.NetworkStatus
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.data.local.Statement
import com.ibracero.retrum.domain.StatementType
import kotlinx.android.synthetic.main.fragment_retro_list.*
import kotlinx.android.synthetic.main.statement_list.*
import org.koin.android.viewmodel.ext.android.viewModel

abstract class StatementFragment : Fragment() {

    private val statementViewModel: StatementViewModel by viewModel()

    private val adapter = StatementListAdapter(::onAddClicked, ::onRemoveClicked)

    abstract val statementType: StatementType

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.statement_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()

        statementViewModel
            .getStatements(getRetroArgument()?.uuid.orEmpty(), statementType)
            .observe(this, Observer { processPositivePoints(it) })

        getRetroArgument()?.title?.let {
            (activity as AppCompatActivity?)?.supportActionBar?.title = it
        }
    }

    private fun initUi() {
        statement_recycler_view.adapter = adapter
    }

    private fun processPositivePoints(positivePoints: List<Statement>) {
        adapter.submitList(positivePoints)
    }

    private fun getRetroArgument(): Retro? = arguments?.getSerializable(BoardFragment.ARGUMENT_RETRO) as Retro?

    private fun onAddClicked(description: String) {
        statementViewModel.addStatement(getRetroArgument()?.uuid.orEmpty(), description, statementType)
    }

    private fun onRemoveClicked(statement: Statement) {
        val safeContext = context ?: return
        createAlertDialog(safeContext, statement).show()
    }

    private fun createAlertDialog(context: Context, statement: Statement): AlertDialog {
        return AlertDialog.Builder(context)
            .setCancelable(true)
            .setTitle(R.string.remove_confirmation_title)
            .setMessage(statement.description)
            .setPositiveButton(R.string.action_yes) { _, _ -> statementViewModel.removeStatement(statement) }
            .setNegativeButton(R.string.action_no) { dialogInterface, _ -> dialogInterface.dismiss() }
            .create()
    }
}
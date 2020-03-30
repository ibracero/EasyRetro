package com.easyretro.ui.board

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import arrow.core.Either
import com.easyretro.R
import com.easyretro.data.local.Statement
import com.easyretro.domain.Failure
import com.easyretro.domain.StatementType
import com.easyretro.ui.Payload
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
            .getStatements(getRetroUuidArgument().orEmpty(), statementType)
            .observe(this, Observer { processPositivePoints(it) })
    }

    private fun initUi() {
        statement_recycler_view.adapter = adapter
    }


    private fun processPositivePoints(positivePoints: List<Statement>) {
        adapter.submitList(positivePoints)
    }

    private fun getRetroUuidArgument() = arguments?.getString(BoardFragment.ARGUMENT_RETRO_UUID)

    private fun onAddClicked(description: String) {
        statementViewModel.addStatement(getRetroUuidArgument().orEmpty(), description, statementType)
            .observe(this, Observer { processStatementAdded(it) })
    }

    private fun processStatementAdded(result: Either<Failure, Unit>) {
        result.fold(
            {
                //showError
                adapter.notifyItemChanged(0, Payload.CreateStatementPayload(success = false))
            },
            {
                adapter.notifyItemChanged(0, Payload.CreateStatementPayload(success = true))
            }
        )
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
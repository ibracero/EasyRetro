package com.easyretro.ui.board

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.easyretro.R
import com.easyretro.common.BaseFragment
import com.easyretro.common.extensions.exhaustive
import com.easyretro.common.extensions.showErrorSnackbar
import com.easyretro.data.local.Statement
import com.easyretro.domain.StatementType
import com.easyretro.ui.Payload
import kotlinx.android.synthetic.main.fragment_statements.*
import org.koin.android.viewmodel.ext.android.viewModel

abstract class StatementFragment :
    BaseFragment<StatementListViewState, StatementListViewEffect, StatementListViewEvent, StatementViewModel>() {

    override val viewModel: StatementViewModel by viewModel()

    private val adapter = StatementListAdapter(::onAddClicked, ::onRemoveClicked)

    abstract val statementType: StatementType

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_statements, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()

        fetchStatements()
    }

    private fun fetchStatements() {
        viewModel.process(
            StatementListViewEvent.FetchStatements(
                retroUuid = getRetroUuidArgument().orEmpty(),
                type = statementType
            )
        )
    }

    private fun initUi() {
        statement_recycler_view.adapter = adapter
    }

    override fun renderViewState(viewState: StatementListViewState) {
        adapter.submitList(viewState.statements)
        when (viewState.addState) {
            StatementAddState.None, StatementAddState.Added -> resetAddItem(success = true)
            StatementAddState.NotAdded -> resetAddItem(success = false)
        }.exhaustive
    }

    override fun renderViewEffect(viewEffect: StatementListViewEffect) {
        when (viewEffect) {
            is StatementListViewEffect.ShowSnackBar ->
                statement_list_root.showErrorSnackbar(message = viewEffect.errorMessage)
        }.exhaustive
    }

    private fun getRetroUuidArgument() = arguments?.getString(BoardFragment.ARGUMENT_RETRO_UUID)

    private fun onAddClicked(description: String) {
        viewModel.process(
            StatementListViewEvent.AddStatement(
                getRetroUuidArgument().orEmpty(),
                description,
                statementType
            )
        )
    }

    private fun resetAddItem(success: Boolean) {
        adapter.notifyItemChanged(0, Payload.CreateStatementPayload(success = success))
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
            .setPositiveButton(R.string.action_yes) { _, _ ->
                viewModel.process(StatementListViewEvent.RemoveStatement(statement))
            }
            .setNegativeButton(R.string.action_no) { dialogInterface, _ -> dialogInterface.dismiss() }
            .create()
    }
}
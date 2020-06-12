package com.easyretro.ui.board

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.easyretro.R
import com.easyretro.analytics.Screen
import com.easyretro.analytics.UiValue
import com.easyretro.analytics.events.StatementCreatedEvent
import com.easyretro.analytics.events.TapEvent
import com.easyretro.analytics.reportAnalytics
import com.easyretro.common.BaseFragment
import com.easyretro.common.extensions.exhaustive
import com.easyretro.common.extensions.showErrorSnackbar
import com.easyretro.domain.model.Statement
import com.easyretro.domain.model.StatementType
import com.easyretro.ui.Payload
import kotlinx.android.synthetic.main.fragment_statements.*
import org.koin.android.viewmodel.ext.android.viewModel

abstract class StatementFragment :
    BaseFragment<StatementListViewState, StatementListViewEffect, StatementListViewEvent, StatementViewModel>() {

    override val viewModel: StatementViewModel by viewModel()

    private val adapter by lazy { StatementListAdapter(::onAddClicked, ::onRemoveClicked) }

    abstract val statementType: StatementType

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_statements, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    override fun onStart() {
        super.onStart()
        val retroUuid = getRetroUuidArgument().orEmpty()
        viewModel.process(StatementListViewEvent.FetchStatements(retroUuid = retroUuid, type = statementType))
        viewModel.process(StatementListViewEvent.CheckRetroLock(retroUuid = retroUuid))
    }

    override fun renderViewState(viewState: StatementListViewState) {
        adapter.submitList(viewState.statements)
        when (viewState.addState) {
            StatementAddState.Shown -> showAddItem()
            StatementAddState.Hidden -> hideAddItem()
        }.exhaustive
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    override fun renderViewEffect(viewEffect: StatementListViewEffect) {
        when (viewEffect) {
            is StatementListViewEffect.ShowSnackBar -> statement_list_root.showErrorSnackbar(viewEffect.errorMessage)
            StatementListViewEffect.CreateItemSuccess -> {
                reportAnalytics(event = StatementCreatedEvent)
                resetAddItem(success = true)
            }
            StatementListViewEffect.CreateItemFailed -> resetAddItem(success = false)
        }.exhaustive
    }

    private fun initUi() {
        statement_recycler_view.adapter = adapter
    }

    private fun getRetroUuidArgument() = arguments?.getString(BoardFragment.ARGUMENT_RETRO_UUID)

    private fun onAddClicked(description: String) {
        reportAnalytics(
            event = TapEvent(
                screen = Screen.RETRO_BOARD,
                uiValue = UiValue.STATEMENT_CREATE
            )
        )
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

    private fun showAddItem() {
        adapter.notifyItemChanged(0, Payload.RetroLockPayload(retroLocked = false))
    }

    private fun hideAddItem() {
        adapter.notifyItemChanged(0, Payload.RetroLockPayload(retroLocked = true))
    }

    private fun onRemoveClicked(statement: Statement) {
        val safeContext = context ?: return
        reportAnalytics(
            event = TapEvent(
                screen = Screen.RETRO_BOARD,
                uiValue = UiValue.STATEMENT_REMOVE
            )
        )
        createRemoveStatementConfirmationDialog(safeContext, statement).show()
    }

    private fun createRemoveStatementConfirmationDialog(context: Context, statement: Statement): AlertDialog {
        return AlertDialog.Builder(context)
            .setCancelable(true)
            .setTitle(R.string.remove_confirmation_title)
            .setMessage(statement.description)
            .setPositiveButton(R.string.action_yes) { _, _ ->
                reportAnalytics(
                    event = TapEvent(
                        screen = Screen.RETRO_BOARD,
                        uiValue = UiValue.STATEMENT_REMOVE_CONFIRMATION
                    )
                )
                viewModel.process(
                    StatementListViewEvent.RemoveStatement(
                        retroUuid = statement.retroUuid,
                        statementUuid = statement.uuid
                    )
                )
            }
            .setNegativeButton(R.string.action_no) { dialogInterface, _ ->
                reportAnalytics(
                    event = TapEvent(
                        screen = Screen.RETRO_BOARD,
                        uiValue = UiValue.STATEMENT_REMOVE_DISMISS
                    )
                )
                dialogInterface.dismiss()
            }
            .create()
    }
}
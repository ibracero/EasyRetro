package com.easyretro.ui.board

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.easyretro.R
import com.easyretro.analytics.Screen
import com.easyretro.analytics.UiValue
import com.easyretro.analytics.events.PageEnterEvent
import com.easyretro.analytics.events.TapEvent
import com.easyretro.analytics.reportAnalytics
import com.easyretro.common.BaseFragment
import com.easyretro.common.extensions.*
import com.easyretro.ui.board.action.ActionsFragment
import com.easyretro.ui.board.negative.NegativeFragment
import com.easyretro.ui.board.positive.PositiveFragment
import com.easyretro.ui.board.users.UserListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_board.*
import kotlin.math.min


@AndroidEntryPoint
class BoardFragment : BaseFragment<BoardViewState, BoardViewEffect, BoardViewEvent, BoardViewModel>() {

    companion object {
        const val ARGUMENT_RETRO_UUID = "arg_retro_uuid"
    }

    override val viewModel: BoardViewModel by viewModels()

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navigateToRetroList()
        }
    }

    private val userListAdapter = UserListAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_board, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        users_recyclerview.adapter = userListAdapter

        getRetroUuidArgument()?.let { uuid ->
            viewModel.process(BoardViewEvent.JoinRetro(retroUuid = uuid))
            viewModel.process(BoardViewEvent.GetRetroInfo(retroUuid = uuid))
        }

        dismiss_protected_message_button.setOnClickListener {
            protected_message.gone()
            dismiss_protected_message_button.gone()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (!isPortraitMode() && isTablet()) initLandscapeUi()
        else initPortraitUi()
    }

    override fun onStart() {
        super.onStart()
        reportAnalytics(event = PageEnterEvent(screen = Screen.RETRO_BOARD))

        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)

        getRetroUuidArgument()?.let {
            viewModel.process(BoardViewEvent.SubscribeRetroDetails(it))
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.process(BoardViewEvent.UnsubscribeRetroDetails)

        backPressedCallback.remove()

        view.hideKeyboard()
    }

    override fun renderViewState(viewState: BoardViewState) {
        initToolbar(retroUuid = viewState.retro.uuid, retroTitle = viewState.retro.title)
        userListAdapter.submitList(viewState.retro.users)
        setupLockMode(retroProtected = viewState.retro.protected, lockingAllowed = viewState.retro.lockingAllowed)
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    override fun renderViewEffect(viewEffect: BoardViewEffect) {
        when (viewEffect) {
            is BoardViewEffect.ShowSnackBar -> board_root.showErrorSnackbar(message = viewEffect.errorMessage)
            is BoardViewEffect.ShowShareSheet ->
                displayShareSheet(retroName = viewEffect.retroTitle, shortLink = viewEffect.deepLink)
        }.exhaustive
    }

    private fun initToolbar(retroUuid: String, retroTitle: String?) {
        board_toolbar?.run {
            title = retroTitle
            setNavigationOnClickListener {
                reportAnalytics(event = TapEvent(
                    screen = Screen.RETRO_BOARD,
                    uiValue = UiValue.BACK
                )
                )
                backPressedCallback.handleOnBackPressed()
            }
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_invite -> onInviteClicked()
                    R.id.action_lock -> context?.let { createLockConfirmationDialog(it, retroUuid).show() }
                    R.id.action_unlock -> onUnprotectClicked(retroUuid)
                }
                true
            }
        }
    }

    private fun setupLockMode(retroProtected: Boolean, lockingAllowed: Boolean) {
        if (lockingAllowed) {
            board_toolbar.menu.findItem(R.id.action_lock).isVisible = !retroProtected
            board_toolbar.menu.findItem(R.id.action_unlock).isVisible = retroProtected
        }
        protected_message.visibleOrGone(retroProtected && !lockingAllowed)
        dismiss_protected_message_button.visibleOrGone(retroProtected && !lockingAllowed)
    }

    private fun initPortraitUi() {
        val navController = findNavController(requireActivity(), R.id.bottom_nav_host_fragment)
            .apply { setGraph(R.navigation.board_nav_graph, arguments) }

        nav_view?.setupWithNavController(navController)
        nav_view?.setOnNavigationItemSelectedListener { menuItem ->
            onTabSelected(menuItem, navController)
            true
        }
    }

    private fun onTabSelected(menuItem: MenuItem, navController: NavController) {
        val selectedPositionId = when (menuItem.itemId) {
            R.id.positive_tab_button -> R.id.navigation_positive
            R.id.negative_tab_button -> R.id.navigation_negative
            R.id.actions_tab_button -> R.id.navigation_actions
            else -> -1
        }

        val currentPositionId = navController.currentDestination?.id
        if (selectedPositionId != -1 && selectedPositionId != currentPositionId) {
            navController.navigate(
                selectedPositionId,
                arguments,
                getTransitionAnimation(currentPositionId!!, selectedPositionId)
            )
        }
    }

    private fun getTransitionAnimation(currentPositionId: Int, selectedPositionId: Int): NavOptions {
        val builder = NavOptions.Builder()
        return when {
            currentPositionId == R.id.navigation_positive -> {
                builder.apply { swipeRight() }
            }
            currentPositionId == R.id.navigation_negative && selectedPositionId == R.id.navigation_positive -> {
                builder.apply { swipeLeft() }
            }
            currentPositionId == R.id.navigation_negative && selectedPositionId == R.id.navigation_actions -> {
                builder.apply { swipeRight() }
            }
            currentPositionId == R.id.navigation_actions -> {
                builder.apply { swipeLeft() }
            }
            else -> builder
        }.build()
    }

    private fun NavOptions.Builder.swipeLeft() {
        setEnterAnim(R.anim.enter_from_left)
        setExitAnim(R.anim.exit_to_right)
        setPopEnterAnim(R.anim.enter_from_right)
        setPopExitAnim(R.anim.exit_to_left)
    }

    private fun NavOptions.Builder.swipeRight() {
        setEnterAnim(R.anim.enter_from_right)
        setExitAnim(R.anim.exit_to_left)
        setPopEnterAnim(R.anim.enter_from_left)
        setPopExitAnim(R.anim.exit_to_right)
    }

    private fun initLandscapeUi() {
        childFragmentManager.beginTransaction().run {
            add(R.id.positive_container, PositiveFragment.newInstance(getRetroUuidArgument()))
            add(R.id.negative_container, NegativeFragment.newInstance(getRetroUuidArgument()))
            add(R.id.actions_container, ActionsFragment.newInstance(getRetroUuidArgument()))
        }.commit()
    }

    private fun navigateToRetroList() {
        findNavController().navigateUp()
    }

    private fun displayShareSheet(shortLink: String, retroName: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.retro_invitation_message, retroName, shortLink)
            )
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun getRetroUuidArgument(): String? = arguments?.getString(ARGUMENT_RETRO_UUID)

    private fun isPortraitMode() = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    private fun isTablet(): Boolean {
        val metrics = context?.resources?.displayMetrics ?: return false
        val widthDp = metrics.widthPixels / metrics.density
        val heightDp = metrics.heightPixels / metrics.density
        return min(widthDp, heightDp) >= 720
    }

    private fun createLockConfirmationDialog(context: Context, retroUuid: String): AlertDialog {
        reportAnalytics(event = TapEvent(
            screen = Screen.RETRO_BOARD,
            uiValue = UiValue.RETRO_PROTECT
        )
        )
        return AlertDialog.Builder(context)
            .setCancelable(true)
            .setTitle(R.string.lock_confirmation_title)
            .setMessage(R.string.lock_confirmation_message)
            .setPositiveButton(R.string.action_yes) { _, _ ->
                onProtectConfirmed(retroUuid = retroUuid)
            }
            .setNegativeButton(R.string.action_no) { dialogInterface, _ ->
                reportAnalytics(event = TapEvent(
                    screen = Screen.RETRO_BOARD,
                    uiValue = UiValue.RETRO_PROTECT_DISMISS
                )
                )
                dialogInterface.dismiss()
            }
            .create()
    }

    private fun onProtectConfirmed(retroUuid: String) {
        reportAnalytics(event = TapEvent(
            screen = Screen.RETRO_BOARD,
            uiValue = UiValue.RETRO_PROTECT_CONFIRMATION
        )
        )
        viewModel.process(BoardViewEvent.ProtectRetro(retroUuid = retroUuid))
    }

    private fun onUnprotectClicked(retroUuid: String) {
        reportAnalytics(event = TapEvent(
            screen = Screen.RETRO_BOARD,
            uiValue = UiValue.RETRO_UNPROTECT
        )
        )
        viewModel.process(BoardViewEvent.UnprotectRetro(retroUuid = retroUuid))
    }

    private fun onInviteClicked() {
        reportAnalytics(event = TapEvent(
            screen = Screen.RETRO_BOARD,
            uiValue = UiValue.RETRO_INVITE
        )
        )
        viewModel.process(BoardViewEvent.ShareRetroLink)
    }
}
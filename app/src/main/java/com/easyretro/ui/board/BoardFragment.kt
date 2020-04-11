package com.easyretro.ui.board

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.easyretro.R
import com.easyretro.common.BaseFragment
import com.easyretro.common.extensions.exhaustive
import com.easyretro.common.extensions.hideKeyboard
import com.easyretro.common.extensions.showErrorSnackbar
import com.easyretro.ui.board.action.ActionsFragment
import com.easyretro.ui.board.negative.NegativeFragment
import com.easyretro.ui.board.positive.PositiveFragment
import com.easyretro.ui.board.users.UserListAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_board.*
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.math.min


class BoardFragment : BaseFragment<BoardViewState, BoardViewEffect, BoardViewEvent, BoardViewModel>() {

    companion object {
        const val ARGUMENT_RETRO_UUID = "arg_retro_uuid"
    }

    override val viewModel: BoardViewModel by viewModel()

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navigateToRetroList()
        }
    }

    private val userListAdapter = UserListAdapter()

    private val offlineSnackbar by lazy {
        Snackbar.make(
            board_root,
            R.string.offline_message,
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            view.setBackgroundResource(R.color.colorAccent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (!isPortraitMode() && isTablet()) initLandscapeUi()
        else initPortraitUi()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.board_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                navigateToRetroList()
                true
            }
            R.id.action_invite -> {
                getRetroUuidArgument()?.let { uuid ->
                    viewModel.process(
                        BoardViewEvent.ShareRetroLink(
                            retroUuid = uuid,
                            link = getString(R.string.retro_join_link_format, uuid)
                        )
                    )
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onStart() {
        super.onStart()

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
        initToolbar(viewState.retro.title)
        userListAdapter.submitList(viewState.retro.users)
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    override fun renderViewEffect(viewEffect: BoardViewEffect) {
        when (viewEffect) {
            is BoardViewEffect.ShowSnackBar -> board_root.showErrorSnackbar(message = viewEffect.errorMessage)
            is BoardViewEffect.ShowShareSheet ->
                displayShareSheet(retroName = viewEffect.retroTitle, shortLink = viewEffect.shortLink)
        }.exhaustive
    }

    private fun initToolbar(title: String?) {
        board_toolbar.title = title
        (requireActivity() as AppCompatActivity).setSupportActionBar(board_toolbar)
    }

    private fun initPortraitUi() {
        val navController =
            findNavController(requireActivity(), R.id.bottom_nav_host_fragment)
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

    private fun displayShareSheet(shortLink: Uri?, retroName: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.retro_invitation_message, retroName, shortLink?.toString())
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
}
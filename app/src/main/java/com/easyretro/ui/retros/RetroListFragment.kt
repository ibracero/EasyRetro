package com.easyretro.ui.retros

import android.os.Bundle
import android.view.*
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.easyretro.R
import com.easyretro.analytics.Screen
import com.easyretro.analytics.UiValue
import com.easyretro.analytics.events.PageEnterEvent
import com.easyretro.analytics.events.TapEvent
import com.easyretro.analytics.reportAnalytics
import com.easyretro.common.BaseFragment
import com.easyretro.common.extensions.hideKeyboard
import com.easyretro.common.extensions.showErrorSnackbar
import com.easyretro.domain.model.Retro
import com.easyretro.ui.Payload
import com.easyretro.ui.board.BoardFragment.Companion.ARGUMENT_RETRO_UUID
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_retro_list.*
import timber.log.Timber

@AndroidEntryPoint
class RetroListFragment :
    BaseFragment<RetroListViewState, RetroListViewEffect, RetroListViewEvent, RetroListViewModel>() {

    override val viewModel: RetroListViewModel by viewModels()

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            requireActivity().finish()
        }
    }

    private val retroListAdapter = RetroListAdapter(::onRetroClicked, ::onAddClicked)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_retro_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as ComponentActivity).onBackPressedDispatcher.addCallback(backPressedCallback)

        initUi()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.retro_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_logout -> {
                reportAnalytics(
                    event = TapEvent(
                        screen = Screen.RETRO_LIST,
                        uiValue = UiValue.SIGN_OUT
                    )
                )
                showLogoutConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onStart() {
        super.onStart()
        reportAnalytics(event = PageEnterEvent(screen = Screen.RETRO_LIST))

        handleDeepLink()
        viewModel.process(viewEvent = RetroListViewEvent.FetchRetros)
    }

    override fun onStop() {
        super.onStop()
        view.hideKeyboard()
    }

    override fun renderViewState(viewState: RetroListViewState) {
        when (viewState.fetchRetrosStatus) {
            is FetchRetrosStatus.Fetched -> showRetros(viewState.fetchRetrosStatus.retros)
        }

        when (viewState.retroCreationStatus) {
            RetroCreationStatus.Created -> {
                retroListAdapter.notifyItemChanged(0, Payload.CreateRetroPayload(true))
            }
            RetroCreationStatus.NotCreated -> retroListAdapter.notifyItemChanged(0, Payload.CreateRetroPayload(false))
        }
    }

    override fun renderViewEffect(viewEffect: RetroListViewEffect) {
        when (viewEffect) {
            is RetroListViewEffect.ShowSnackBar -> showError(viewEffect.errorMessage)
            is RetroListViewEffect.OpenRetroDetail -> navigateToRetroBoard(viewEffect.retroUuid)
        }
    }

    private fun showRetros(retros: List<Retro>?) {
        retroListAdapter.submitList(retros)
    }

    private fun initUi() {
        retro_list_toolbar.title = ""
        (requireActivity() as AppCompatActivity).setSupportActionBar(retro_list_toolbar)
        retro_recycler_view?.adapter = retroListAdapter
    }

    private fun onRetroClicked(retro: Retro) {
        reportAnalytics(
            event = TapEvent(
                screen = Screen.RETRO_LIST,
                uiValue = UiValue.RETRO_ITEM
            )
        )
        viewModel.process(viewEvent = RetroListViewEvent.RetroClicked(retroUuid = retro.uuid))
    }

    private fun onAddClicked(retroTitle: String) {
        reportAnalytics(
            event = TapEvent(
                screen = Screen.RETRO_LIST,
                uiValue = UiValue.RETRO_CREATE
            )
        )
        viewModel.process(viewEvent = RetroListViewEvent.CreateRetroClicked(retroTitle = retroTitle))
    }

    private fun showError(@StringRes errorMessage: Int) {
        retro_list_root.showErrorSnackbar(message = errorMessage)
    }

    private fun showLogoutConfirmationDialog() {
        val safeContext = context ?: return

        AlertDialog.Builder(safeContext)
            .setCancelable(true)
            .setTitle(R.string.confirm_logout)
            .setMessage(getString(R.string.confirm_logout_message))
            .setPositiveButton(R.string.action_yes) { _, _ ->
                reportAnalytics(
                    event = TapEvent(
                        screen = Screen.RETRO_LIST,
                        uiValue = UiValue.SIGN_OUT_CONFIRMATION
                    )
                )
                onLogoutConfirmed()
            }
            .setNegativeButton(R.string.action_no) { dialogInterface, _ ->
                reportAnalytics(
                    event = TapEvent(
                        screen = Screen.RETRO_LIST,
                        uiValue = UiValue.SIGN_OUT_DISMISS
                    )
                )
                dialogInterface.dismiss()
            }
            .create()
            .show()
    }

    private fun onLogoutConfirmed() {
        logoutFromGoogle()
        navigateToLoginScreen()
        viewModel.process(viewEvent = RetroListViewEvent.LogoutClicked)
    }

    private fun logoutFromGoogle() {
        GoogleSignIn.getClient(requireActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
    }

    private fun navigateToRetroBoard(retroUuid: String) {
        val args = Bundle().apply {
            putString(ARGUMENT_RETRO_UUID, retroUuid)
        }
        findNavController().navigate(R.id.action_retro_clicked, args)
    }

    private fun navigateToLoginScreen() {
        findNavController().navigate(R.id.action_logout_clicked)
    }

    private fun handleDeepLink() {
        activity?.intent?.let { intent ->
            FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener {
                    activity?.intent = null
                    Timber.d("User is logged in. Handling deeplink: ${it?.link?.toString()}")
                    it?.link?.let { uri ->
                        navigateToRetroBoard(uri.lastPathSegment.orEmpty())
                    }
                }
                .addOnFailureListener {
                    Timber.e(it)
                }
        }
    }
}

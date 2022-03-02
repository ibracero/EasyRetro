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
import com.easyretro.common.BaseFlowFragment
import com.easyretro.common.extensions.hideKeyboard
import com.easyretro.common.extensions.showErrorSnackbar
import com.easyretro.common.extensions.viewBinding
import com.easyretro.databinding.FragmentRetroListBinding
import com.easyretro.domain.model.Retro
import com.easyretro.ui.Payload
import com.easyretro.ui.board.BoardFragment.Companion.ARGUMENT_RETRO_UUID
import com.easyretro.ui.retros.RetroListContract.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class RetroListFragment : BaseFlowFragment<State, Effect, Event, RetroListViewModel>(R.layout.fragment_retro_list) {

    override val viewModel: RetroListViewModel by viewModels()

    private val binding by viewBinding(FragmentRetroListBinding::bind)

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
        viewModel.process(Event.ScreenLoaded)
    }

    override fun onStop() {
        super.onStop()
        view.hideKeyboard()
    }

    override fun renderViewState(uiState: State) {
        when (val retroListState = uiState.retroListState) {
            is RetroListState.ShowRetroList -> showRetros(retroListState.retros)
            else -> Unit
        }

        when (uiState.newRetroState) {
            NewRetroState.AddRetroShown -> retroListAdapter.notifyItemChanged(0, Payload.CreateRetroPayload(true))
            NewRetroState.TextInputShown -> retroListAdapter.notifyItemChanged(0, Payload.CreateRetroPayload(false))
            else -> Unit
        }
    }

    override fun renderViewEffect(uiEffect: Effect) {
        when (uiEffect) {
            is Effect.OpenRetroDetail -> navigateToRetroBoard(uiEffect.retroUuid)
            is Effect.ShowSnackBar -> showError(uiEffect.errorMessage)
        }
    }

    private fun showRetros(retros: List<Retro>?) {
        retroListAdapter.submitList(retros)
    }

    private fun initUi() {
        binding.retroListToolbar.title = ""
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.retroListToolbar)
        binding.retroRecyclerView.adapter = retroListAdapter
    }

    private fun onRetroClicked(retro: Retro) {
        reportAnalytics(event = TapEvent(screen = Screen.RETRO_LIST, uiValue = UiValue.RETRO_ITEM))
        viewModel.process(Event.RetroClicked(retro.uuid))
    }

    private fun onAddClicked(retroTitle: String) {
        reportAnalytics(event = TapEvent(screen = Screen.RETRO_LIST, uiValue = UiValue.RETRO_CREATE))
        viewModel.process(Event.CreateRetroClicked(retroTitle = retroTitle))
    }

    private fun showError(@StringRes errorMessage: Int) {
        binding.root.showErrorSnackbar(message = errorMessage)
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
        viewModel.process(Event.LogoutClicked)
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

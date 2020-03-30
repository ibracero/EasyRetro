package com.easyretro.ui.retros

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.easyretro.R
import com.easyretro.common.BaseFragment
import com.easyretro.common.extensions.hideKeyboard
import com.easyretro.data.local.Retro
import com.easyretro.ui.Payload
import com.easyretro.ui.board.BoardFragment.Companion.ARGUMENT_RETRO_UUID
import kotlinx.android.synthetic.main.fragment_retro_list.*
import org.koin.android.viewmodel.ext.android.viewModel

class RetroListFragment :
    BaseFragment<RetroListViewState, RetroListViewEffect, RetroListViewEvent, RetroListViewModel>() {

    override val viewModel: RetroListViewModel by viewModel()

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
                showLogoutConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun logoutFromGoogle() {
        GoogleSignIn.getClient(requireActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
    }

    override fun onStart() {
        super.onStart()
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
            RetroCreationStatus.Created -> retroListAdapter.notifyItemChanged(0, Payload.CreateRetroPayload(true))
            RetroCreationStatus.NotCreated -> retroListAdapter.notifyItemChanged(0, Payload.CreateRetroPayload(false))
        }
    }

    override fun renderViewEffect(viewEffect: RetroListViewEffect) {
        when (viewEffect) {
            is RetroListViewEffect.ShowSnackBar -> showError()
            is RetroListViewEffect.OpenRetroDetail -> navigateToRetroBoard(viewEffect.retroUuid)
        }
    }

    private fun showRetros(it: List<Retro>?) {
        retroListAdapter.submitList(it)
    }

    private fun initUi() {
        retro_list_toolbar.title = ""
        (requireActivity() as AppCompatActivity).setSupportActionBar(retro_list_toolbar)
        retro_recycler_view?.adapter = retroListAdapter
    }

    private fun onRetroClicked(retro: Retro) {
        viewModel.process(viewEvent = RetroListViewEvent.RetroClicked(retro = retro))
    }

    private fun onAddClicked(retroTitle: String) {
        viewModel.process(viewEvent = RetroListViewEvent.CreateRetroClicked(retroName = retroTitle))
    }

    private fun showError() {
        Toast.makeText(context, "Couldn't create retro", Toast.LENGTH_SHORT).show()
    }

    private fun showLogoutConfirmationDialog() {

        val safeContext = context ?: return

        AlertDialog.Builder(safeContext)
            .setCancelable(true)
            .setTitle(R.string.confirm_logout)
            .setMessage(getString(R.string.confirm_logout_message))
            .setPositiveButton(R.string.action_yes) { _, _ -> onLogoutConfirmed() }
            .setNegativeButton(R.string.action_no) { dialogInterface, _ -> dialogInterface.dismiss() }
            .create()
            .show()
    }

    private fun onLogoutConfirmed() {
        logoutFromGoogle()
        navigateToLoginScreen()
        viewModel.process(viewEvent = RetroListViewEvent.LogoutClicked)
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

}

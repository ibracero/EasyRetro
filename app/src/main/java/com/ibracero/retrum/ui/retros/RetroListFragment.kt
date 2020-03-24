package com.ibracero.retrum.ui.retros

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import arrow.core.Either
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.ibracero.retrum.R
import com.ibracero.retrum.common.NetworkStatus.ONLINE
import com.ibracero.retrum.common.extensions.hideKeyboard
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.domain.Failure
import com.ibracero.retrum.ui.Payload
import com.ibracero.retrum.ui.board.BoardFragment.Companion.ARGUMENT_LOGOUT
import com.ibracero.retrum.ui.board.BoardFragment.Companion.ARGUMENT_RETRO_UUID
import kotlinx.android.synthetic.main.fragment_retro_list.*
import org.koin.android.viewmodel.ext.android.viewModel

class RetroListFragment : Fragment() {

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            requireActivity().finish()
        }
    }

    private val retroListViewModel: RetroListViewModel by viewModel()
    private val retroListAdapter = RetroListAdapter(::onRetroClicked, ::onAddClicked)

    private val offlineSnackbar by lazy {
        Snackbar.make(
            retro_list_root,
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_retro_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as ComponentActivity).onBackPressedDispatcher.addCallback(backPressedCallback)

        initUi()
        retroListViewModel.retroLiveData.observe(this@RetroListFragment, Observer { showRetros(it) })
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
        retroListViewModel.startObservingRetros()
        retroListViewModel.connectivityLiveData.observe(this@RetroListFragment, Observer {
            when (it) {
                ONLINE -> offlineSnackbar.dismiss()
                else -> offlineSnackbar.show()
            }
        })
    }

    override fun onStop() {
        super.onStop()
        retroListViewModel.stopObservingRetros()
        view.hideKeyboard()
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
        navigateToRetroBoard(retro)
    }

    private fun onAddClicked(retroTitle: String) {
        retroListViewModel.createRetro(retroTitle).observe(this@RetroListFragment,
            Observer { retroEither ->
                processCreateRetroResponse(retroEither)
            })
    }

    private fun processCreateRetroResponse(retroEither: Either<Failure, Retro>) {
        retroEither.fold({
            retroListAdapter.notifyItemChanged(0, Payload.CreateRetroPayload(false))
            showError()
        }, { retro ->
            retroListAdapter.notifyItemChanged(0, Payload.CreateRetroPayload(true))
            navigateToRetroBoard(retro = retro)
        })
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
        retroListViewModel.logout()
        logoutFromGoogle()
        navigateToLoginScreen()
    }

    private fun navigateToRetroBoard(retro: Retro) {
        val args = Bundle().apply {
            putString(ARGUMENT_RETRO_UUID, retro.uuid)
        }
        findNavController().navigate(R.id.action_retro_clicked, args)
    }

    private fun navigateToLoginScreen() {
        findNavController().navigate(R.id.action_logout_clicked)
    }
}

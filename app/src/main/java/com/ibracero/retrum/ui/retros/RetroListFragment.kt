package com.ibracero.retrum.ui.retros

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import arrow.core.Either
import com.google.android.material.snackbar.Snackbar
import com.ibracero.retrum.R
import com.ibracero.retrum.common.NetworkStatus.ONLINE
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.data.remote.ServerError
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
    }

    private fun showRetros(it: List<Retro>?) {
        retroListAdapter.submitList(it)
    }

    private fun initUi() {
        retro_recycler_view?.adapter = retroListAdapter
    }

    private fun onRetroClicked(retro: Retro) {
        navigateToRetroBoard(retro)
    }

    private fun onAddClicked(retroTitle: String) {
        retroListViewModel.retroLiveData.removeObservers(this)
        retroListViewModel.createRetro(retroTitle).observe(this@RetroListFragment,
            Observer { retroEither ->
                processCreateRetroResponse(retroEither)
            })
    }

    private fun processCreateRetroResponse(retroEither: Either<ServerError, Retro>) {
        retroEither.fold({
            showError()
        }, { retro ->
            navigateToRetroBoard(retro = retro)
        })
    }

    private fun showError() {
        Toast.makeText(context, "Couldn't create retro", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToRetroBoard(retro: Retro) {
        val args = Bundle().apply {
            putString(ARGUMENT_RETRO_UUID, retro.uuid)
        }
        findNavController().navigate(R.id.action_retro_clicked, args)
    }
}

package com.ibracero.retrum.ui.retros

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ibracero.retrum.R
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.ui.BottomNavFragment.Companion.ARGUMENT_RETRO
import kotlinx.android.synthetic.main.fragment_retro_list.*
import org.koin.android.viewmodel.ext.android.viewModel

class RetroListFragment : Fragment() {

    private val retroListViewModel: RetroListViewModel by viewModel()
    private val retroListAdapter = RetroListAdapter(::onRetroClicked, ::onAddClicked)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_retro_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.app_name)

        initUi()
        retroListViewModel.retroLiveData.observe(this@RetroListFragment, Observer { showRetros(it) })
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
        retroListViewModel.createRetro(retroTitle).observe(this@RetroListFragment, Observer {
            navigateToRetroBoard(retro = it)
        })
    }

    private fun navigateToRetroBoard(retro: Retro) {
        val args = Bundle().apply { putSerializable(ARGUMENT_RETRO, retro) }
        findNavController().navigate(R.id.action_retro_clicked, args)
    }
}

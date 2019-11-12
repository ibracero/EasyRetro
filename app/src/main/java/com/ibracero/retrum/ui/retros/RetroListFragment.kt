package com.ibracero.retrum.ui.retros

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.ibracero.retrum.R
import kotlinx.android.synthetic.main.fragment_retro_list.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber

class RetroListFragment : Fragment() {

    private val retroListViewModel: RetroListViewModel by viewModel()
    private val retroListAdapter = RetroListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_retro_list, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initUi()
        retroListViewModel.retroLiveData.observe(this@RetroListFragment, Observer {
            Timber.d("Retro list: $it")
            retroListAdapter.submitList(it)
        })
    }

    private fun initUi() {
        retro_recycler_view?.adapter = retroListAdapter
    }
}
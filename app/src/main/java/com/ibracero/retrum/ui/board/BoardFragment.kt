package com.ibracero.retrum.ui.board

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.ibracero.retrum.R
import com.ibracero.retrum.common.NetworkStatus
import com.ibracero.retrum.common.RetrumConnectionManager
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.domain.BoardRepository
import com.ibracero.retrum.ui.board.action.ActionsFragment
import com.ibracero.retrum.ui.board.negative.NegativeFragment
import kotlinx.android.synthetic.main.fragment_board.*
import org.koin.android.ext.android.inject
import com.ibracero.retrum.ui.board.positive.PositiveFragment


class BoardFragment : Fragment() {

    companion object {
        const val ARGUMENT_RETRO = "arg_retro"
    }

    private val boardRepository: BoardRepository by inject()
    private val connectionManager: RetrumConnectionManager by inject()

    private val offlineSnackbar by lazy {
        Snackbar.make(
            board_root,
            R.string.offline_message,
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            view.setBackgroundResource(R.color.colorAccent)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_board, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) initPortraitUi()
        else initLandscapeUi()
    }

    override fun onStart() {
        super.onStart()
        getRetroArgument()?.let {
            boardRepository.startObservingStatements(it.uuid)
        }

        connectionManager.connectionLiveData.observe(this@BoardFragment, Observer {
            when (it) {
                NetworkStatus.ONLINE -> offlineSnackbar.dismiss()
                else -> offlineSnackbar.show()
            }
        })
    }

    override fun onStop() {
        super.onStop()
        boardRepository.stopObservingStatements()
    }

    private fun initPortraitUi() {
        val navController =
            findNavController(requireActivity(), R.id.bottom_nav_host_fragment)
                .apply { setGraph(R.navigation.board_nav_graph, arguments) }

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_positive,
                R.id.navigation_negative,
                R.id.navigation_actions
            )
        )
        (requireActivity() as AppCompatActivity).setSupportActionBar(board_toolbar)
        setupActionBarWithNavController(requireActivity() as AppCompatActivity, navController, appBarConfiguration)
        nav_view?.setupWithNavController(navController)
        nav_view?.setOnNavigationItemSelectedListener { menuItem ->

            val destinationId = when (menuItem.itemId) {
                R.id.navigation_positive -> R.id.navigation_positive
                R.id.navigation_negative -> R.id.navigation_negative
                R.id.navigation_actions -> R.id.navigation_actions
                else -> -1
            }

            if (destinationId != -1 && destinationId != navController.currentDestination?.id) {
                navController.navigate(destinationId, arguments)
            }

            true
        }
    }

    private fun initLandscapeUi() {

        val fragmentTransaction = childFragmentManager.beginTransaction()

        fragmentTransaction.add(
            R.id.positive_container,
            PositiveFragment().apply { this.arguments = this@BoardFragment.arguments })
        fragmentTransaction.add(
            R.id.negative_container,
            NegativeFragment().apply { this.arguments = this@BoardFragment.arguments })
        fragmentTransaction.add(
            R.id.actions_container,
            ActionsFragment().apply { this.arguments = this@BoardFragment.arguments })

        fragmentTransaction.commit()
    }

    private fun getRetroArgument() = arguments?.getSerializable(ARGUMENT_RETRO) as Retro?
}
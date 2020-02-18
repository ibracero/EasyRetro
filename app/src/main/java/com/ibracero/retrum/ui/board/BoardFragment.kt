package com.ibracero.retrum.ui.board

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.ibracero.retrum.R
import com.ibracero.retrum.common.NetworkStatus
import com.ibracero.retrum.common.RetrumConnectionManager
import com.ibracero.retrum.domain.BoardRepository
import com.ibracero.retrum.ui.board.action.ActionsFragment
import com.ibracero.retrum.ui.board.negative.NegativeFragment
import com.ibracero.retrum.ui.board.positive.PositiveFragment
import com.ibracero.retrum.ui.board.users.UserListAdapter
import kotlinx.android.synthetic.main.fragment_board.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel


class BoardFragment : Fragment() {

    companion object {
        const val ARGUMENT_RETRO_UUID = "arg_retro_uuid"
    }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navigateToRetroList()
        }
    }
    private val boardViewModel: BoardViewModel by viewModel()
    private val boardRepository: BoardRepository by inject()
    private val connectionManager: RetrumConnectionManager by inject()

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
            boardViewModel.getRetroInfo(uuid).observe(this@BoardFragment, Observer { retro ->
                initToolbar(retro?.title)
                userListAdapter.submitList(retro?.users ?: emptyList())
            })
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (isPortraitMode()) initPortraitUi()
        else initLandscapeUi()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.board_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> navigateToRetroList()
            R.id.action_invite -> displayShareSheet()
        }

        return true
    }

    override fun onStart() {
        super.onStart()

        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)

        getRetroUuidArgument()?.let {
            boardRepository.startObservingStatements(it)
            boardRepository.startObservingRetroUsers(it)
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
        boardRepository.stopObservingRetroUsers()

        backPressedCallback.remove()
    }

    private fun initToolbar(title: String?) {
        board_toolbar.title = title ?: getString(R.string.app_name)
        (requireActivity() as AppCompatActivity).setSupportActionBar(board_toolbar)
    }

    private fun initPortraitUi() {
        val navController =
            findNavController(requireActivity(), R.id.bottom_nav_host_fragment)
                .apply { setGraph(R.navigation.board_nav_graph, arguments) }

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
        childFragmentManager.beginTransaction().run {
            add(R.id.positive_container, PositiveFragment.newInstance(getRetroUuidArgument()))
            add(R.id.negative_container, NegativeFragment.newInstance(getRetroUuidArgument()))
            add(R.id.actions_container, ActionsFragment.newInstance(getRetroUuidArgument()))
        }.commit()
    }

    private fun navigateToRetroList() {
        findNavController().navigate(R.id.navigation_retro_list)
    }

    private fun displayShareSheet() {
        val link = getString(R.string.retro_join_link_format, getRetroUuidArgument().orEmpty())
        val retroTitle = getRetroUuidArgument()?.let {
            boardViewModel.getRetroInfo(it).value?.title
        }.orEmpty()

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.retro_invitation_message, retroTitle, link)
            )
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun getRetroUuidArgument() = arguments?.getString(ARGUMENT_RETRO_UUID)

    private fun isPortraitMode() = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
}
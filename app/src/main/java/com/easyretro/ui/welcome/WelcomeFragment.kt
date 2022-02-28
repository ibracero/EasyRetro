package com.easyretro.ui.welcome

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.easyretro.R
import com.easyretro.analytics.Screen
import com.easyretro.analytics.events.PageEnterEvent
import com.easyretro.analytics.reportAnalytics
import com.easyretro.common.BaseFlowFragment
import com.easyretro.common.extensions.*
import com.easyretro.databinding.FragmentWelcomeBinding
import com.easyretro.ui.account.AccountFragment.Companion.ARG_IS_NEW_ACCOUNT
import com.easyretro.ui.welcome.WelcomeContract.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class WelcomeFragment : BaseFlowFragment<State, Effect, Event, WelcomeViewModel>(R.layout.fragment_welcome) {

    override val viewModel: WelcomeViewModel by viewModels()

    private val binding by viewBinding(FragmentWelcomeBinding::bind)
    private val welcomeViewModel: WelcomeViewModel by viewModels()

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            welcomeViewModel.process(Event.GoogleSignInResultReceived(account))
        } catch (e: ApiException) {
            Timber.e(e)
            welcomeViewModel.process(Event.GoogleSignInResultReceived(null))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        welcomeViewModel.process(Event.ScreenLoaded)
        initUi()
    }

    override fun onStart() {
        super.onStart()
        reportAnalytics(event = PageEnterEvent(screen = Screen.WELCOME))
    }

    override fun renderViewState(uiState: State) {
        binding.groupPostIt.visible()
        binding.groupButtons.visibleOrInvisible(uiState.areLoginButtonsShown)
        binding.loading.visibleOrGone(uiState.isLoadingShown)
    }

    override fun renderViewEffect(uiEffect: Effect) {
        when (uiEffect) {
            Effect.NavigateToGoogleSignIn -> launchGoogleSignIn()
            Effect.NavigateToEmailLogin -> navigateToLogin()
            Effect.NavigateToRetros -> navigateToRetroList()
            Effect.NavigateToSignUp -> navigateToRegister()
            is Effect.GoogleSignInError -> onGoogleSignInError(uiEffect.errorRes)
        }
    }

    private fun onGoogleSignInError(@StringRes errorRes: Int) {
        binding.root.showErrorSnackbar(message = errorRes, duration = Snackbar.LENGTH_LONG)
        binding.loading.gone()
        binding.groupButtons.visible()
    }

    private fun initUi() {
        with(binding) {
            googleSignIn.setOnClickListener {
                viewModel.process(Event.GoogleSignInClicked)
            }
            emailSignIn.setOnClickListener {
                viewModel.process(Event.EmailSignInClicked)
            }
            signUpButton.setOnClickListener {
                viewModel.process(Event.SignUpClicked)
            }
        }
    }

    private fun launchGoogleSignIn() {
        val signInIntent = GoogleSignIn.getClient(requireActivity(), getSignInOptions()).signInIntent
        resultLauncher.launch(signInIntent)
    }

    private fun navigateToRetroList() {
        findNavController().navigate(R.id.action_logged_in)
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_sign_in_with_email)
    }

    private fun navigateToRegister() {
        val bundle = Bundle().apply { putBoolean(ARG_IS_NEW_ACCOUNT, true) }
        findNavController().navigate(R.id.action_register, bundle)
    }

    private fun getSignInOptions() =
        GoogleSignInOptions.Builder()
            .requestEmail()
            .requestProfile()
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()
}
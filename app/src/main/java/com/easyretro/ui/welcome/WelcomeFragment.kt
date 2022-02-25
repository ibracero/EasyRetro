package com.easyretro.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class WelcomeFragment :
    BaseFlowFragment<WelcomeViewState, WelcomeViewEffect, WelcomeViewEvent, WelcomeViewModel>(R.layout.fragment_welcome) {

    override val viewModel: WelcomeViewModel by viewModels()

    private val binding by viewBinding(FragmentWelcomeBinding::bind)
    private val welcomeViewModel: WelcomeViewModel by viewModels()

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            welcomeViewModel.process(WelcomeViewEvent.GoogleSignInResultReceived(account))
        } catch (e: ApiException) {
            Timber.e(e)
            welcomeViewModel.process(WelcomeViewEvent.GoogleSignInResultReceived(null))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_welcome, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    override fun onStart() {
        super.onStart()
        reportAnalytics(event = PageEnterEvent(screen = Screen.WELCOME))
    }

    override fun renderViewState(viewState: WelcomeViewState) {
        when (viewState) {
            WelcomeViewState.Splash -> {
                binding.groupPostIt.visible()
                binding.groupButtons.gone()
            }
            WelcomeViewState.LoginInProgress -> {
                binding.groupPostIt.visible()
                binding.groupButtons.invisible()
                binding.loading.visible()
            }
            WelcomeViewState.LoginOptionsDisplayed -> showButtons()
        }
    }

    override fun renderViewEffect(viewEffect: WelcomeViewEffect) {
        when (viewEffect) {
            WelcomeViewEffect.NavigateToGoogleSignIn -> launchGoogleSignIn()
            WelcomeViewEffect.NavigateToEmailLogin -> navigateToLogin()
            WelcomeViewEffect.NavigateToRetros -> navigateToRetroList()
            WelcomeViewEffect.NavigateToSignUp -> navigateToRegister()
            is WelcomeViewEffect.GoogleSignInError -> onGoogleSignInError(viewEffect.errorRes)
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
                viewModel.process(WelcomeViewEvent.GoogleSignInClicked)
            }
            emailSignIn.setOnClickListener {
                viewModel.process(WelcomeViewEvent.EmailSignInClicked)
            }
            signUpButton.setOnClickListener {
                viewModel.process(WelcomeViewEvent.SignUpClicked)
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

    private fun showButtons() {
        binding.groupPostIt.visible()
        binding.loading.gone()
        binding.groupButtons.visible()
    }

    private fun getSignInOptions() =
        GoogleSignInOptions.Builder()
            .requestEmail()
            .requestProfile()
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()
}
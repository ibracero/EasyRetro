package com.easyretro.ui.welcome

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import arrow.core.Either
import com.easyretro.R
import com.easyretro.analytics.Screen
import com.easyretro.analytics.UiValue
import com.easyretro.analytics.events.PageEnterEvent
import com.easyretro.analytics.events.TapEvent
import com.easyretro.analytics.events.UserGoogleSignedInEvent
import com.easyretro.analytics.reportAnalytics
import com.easyretro.common.extensions.*
import com.easyretro.databinding.FragmentWelcomeBinding
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.UserStatus
import com.easyretro.ui.FailureMessage
import com.easyretro.ui.account.AccountFragment.Companion.ARG_IS_NEW_ACCOUNT
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class WelcomeFragment : Fragment(R.layout.fragment_welcome) {

    companion object {
        const val STARTUP_DELAY = 1500L
    }

    private val binding by viewBinding(FragmentWelcomeBinding::bind)
    private val buttonsLayoutHandler = Handler(Looper.getMainLooper())
    private val welcomeViewModel: WelcomeViewModel by viewModels()

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            binding.groupButtons.invisible()
            binding.loading.visible()
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            welcomeViewModel.handleSignInResult(account)
        } catch (e: ApiException) {
            Timber.e(e)
            processGoogleSignInResponse(Either.left(Failure.UnknownError))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi()
        welcomeViewModel.googleSignInLiveData.observe(viewLifecycleOwner) { processGoogleSignInResponse(it) }
        welcomeViewModel.userSessionLiveData.observe(viewLifecycleOwner) { processUserSession(it) }
    }

    override fun onStart() {
        super.onStart()
        reportAnalytics(event = PageEnterEvent(screen = Screen.WELCOME))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        buttonsLayoutHandler.removeCallbacksAndMessages(null)
    }

    private fun processUserSession(userStatus: Either<Failure, UserStatus>) {
        userStatus.fold({
            showButtons()
        }, {
            when (it) {
                UserStatus.VERIFIED -> buttonsLayoutHandler.postDelayed({ navigateToRetroList() }, STARTUP_DELAY)
                else -> showButtons()
            }
        })
    }

    private fun processGoogleSignInResponse(response: Either<Failure, Unit>) {
        response.fold({
            binding.root.showErrorSnackbar(message = FailureMessage.parse(it), duration = Snackbar.LENGTH_LONG)
            binding.loading.gone()
            binding.groupButtons.visible()
        }, {
            reportAnalytics(event = UserGoogleSignedInEvent)
            navigateToRetroList()
        })
    }

    private fun initUi() {
        with(binding) {
            groupPostIt.visible()

            googleSignIn.setOnClickListener {
                reportAnalytics(
                    event = TapEvent(
                        screen = Screen.WELCOME,
                        uiValue = UiValue.GOOGLE_SIGN_IN
                    )
                )
                launchGoogleSignIn(it)
            }
            emailSignIn.setOnClickListener {
                reportAnalytics(
                    event = TapEvent(
                        screen = Screen.WELCOME,
                        uiValue = UiValue.WELCOME_EMAIL_SIGN_IN
                    )
                )
                navigateToLogin()
            }
            signUpButton.setOnClickListener {
                reportAnalytics(
                    event = TapEvent(
                        screen = Screen.WELCOME,
                        uiValue = UiValue.WELCOME_EMAIL_SIGN_UP
                    )
                )
                navigateToRegister()
            }
        }
    }

    private fun showButtons() {
        binding.groupButtons.visible()
    }

    private fun launchGoogleSignIn(it: View) {
        val signInIntent = GoogleSignIn.getClient(it.context, getSignInOptions()).signInIntent
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
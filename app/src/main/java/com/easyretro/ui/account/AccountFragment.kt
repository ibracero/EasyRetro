package com.easyretro.ui.account

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.easyretro.R
import com.easyretro.analytics.Screen
import com.easyretro.analytics.UiValue
import com.easyretro.analytics.events.PageEnterEvent
import com.easyretro.analytics.events.TapEvent
import com.easyretro.analytics.events.UserSignedInEvent
import com.easyretro.analytics.reportAnalytics
import com.easyretro.common.BaseFragment
import com.easyretro.common.extensions.*
import com.easyretro.databinding.FragmentAccountBinding
import com.easyretro.ui.account.ResetPasswordFragment.Companion.ARG_EMAIL
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AccountFragment : BaseFragment<AccountViewState, AccountViewEffect, AccountViewEvent, AccountViewModel>() {

    companion object {
        private const val MINIMUM_PASSWORD_LENGHT = 6
        private const val PASSWORD_PATTERN = "[a-zA-Z0-9]{$MINIMUM_PASSWORD_LENGHT,}"
        const val ARG_IS_NEW_ACCOUNT = "arg_is_new_account"
    }

    override val viewModel: AccountViewModel by viewModels()

    private val binding by viewBinding(FragmentAccountBinding::bind)

    private val passwordRegex = PASSWORD_PATTERN.toRegex()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_account, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi(getIsNewAccountArgument())
    }

    override fun onStart() {
        super.onStart()
        logPageEnter()
    }

    override fun renderViewState(viewState: AccountViewState) {
        view?.hideKeyboard()
        when (viewState.signingState) {
            SigningState.Loading -> binding.showLoading()
            SigningState.RequestDone -> binding.hideLoading()
        }
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    override fun renderViewEffect(viewEffect: AccountViewEffect) {
        when (viewEffect) {
            is AccountViewEffect.ShowGenericSnackBar -> showError(viewEffect.errorMessage)
            is AccountViewEffect.ShowExistingUserSnackBar -> showExistingUserError()
            is AccountViewEffect.ShowUnknownUserSnackBar -> showUnknownUserError()
            AccountViewEffect.OpenResetPassword -> navigateToResetPassword()
            AccountViewEffect.OpenEmailVerification -> navigateToEmailVerification()
            AccountViewEffect.OpenRetroList -> {
                reportAnalytics(event = UserSignedInEvent)
                navigateToRetroList()
            }
        }.exhaustive
    }

    private fun initUi(isNewAccount: Boolean) {
        with(binding) {
            if (isNewAccount) setupSignUp()
            else setupSignIn()

            binding.signInToolbar.setNavigationOnClickListener {
                reportAnalytics(
                    event = TapEvent(
                        screen = getAnalyticsScreen(),
                        uiValue = UiValue.BACK
                    )
                )
                findNavController().navigateUp()
            }

            setupResetPassword()
            setupTextWatchers()

            signInButton.setOnClickListener { onSignInButtonClicked() }

            checkFieldErrors()
        }
    }

    private fun FragmentAccountBinding.onSignInButtonClicked() {
        reportAnalytics(
            event = TapEvent(
                screen = getAnalyticsScreen(),
                uiValue = if (getAnalyticsScreen() == Screen.SIGN_IN) UiValue.ACCOUNT_SIGN_IN else UiValue.ACCOUNT_SIGN_UP
            )
        )

        val email = emailInputField.text.toString()
        val password = passwordInputField.text.toString()

        if (!confirmPasswordInputLayout.isVisible())
            viewModel.process(viewEvent = AccountViewEvent.SignIn(email, password))
        else viewModel.process(viewEvent = AccountViewEvent.SignUp(email, password))
    }

    private fun FragmentAccountBinding.setupTextWatchers() {
        emailInputField.addTextWatcher(afterTextChanged = {
            if (Patterns.EMAIL_ADDRESS.toRegex().matches(emailInputField.text.toString())) {
                emailInputLayout.error = null
            } else emailInputLayout.error = getString(R.string.email_validation_error)

            checkFieldErrors()
        })

        passwordInputField.addTextWatcher(afterTextChanged = {
            when {
                passwordRegex.matches(passwordInputField.text.toString()) -> passwordInputLayout.error = null
                passwordInputField.length() < MINIMUM_PASSWORD_LENGHT -> {
                    passwordInputLayout.error = getString(R.string.password_minimum_length_validation_error)
                }
                else -> passwordInputLayout.error = getString(R.string.password_alphanumeric_validation_error)
            }

            checkFieldErrors()
        })

        confirmPasswordInputField.addTextWatcher(afterTextChanged = {
            when {
                confirmPasswordInputField.length() == 0 -> confirmPasswordInputLayout.error = null
                passwordInputField.text.toString() == confirmPasswordInputField.text.toString() ->
                    confirmPasswordInputLayout.error = null
                else -> confirmPasswordInputLayout.error = getString(R.string.confirm_password_validation_error)
            }

            checkFieldErrors()
        })
    }

    private fun FragmentAccountBinding.setupResetPassword() {
        val resetPasswordClickListener = { _: View ->
            reportAnalytics(
                event = TapEvent(
                    screen = getAnalyticsScreen(),
                    uiValue = UiValue.ACCOUNT_RESET_PASSWORD
                )
            )
            viewModel.process(AccountViewEvent.ResetPassword)
        }
        resetPasswordButton.setOnClickListener(resetPasswordClickListener)
        resetPasswordLabel.setOnClickListener(resetPasswordClickListener)
    }

    private fun FragmentAccountBinding.setupSignUp() {
        signInToolbar.setTitle(R.string.sign_up_with_email)
        signInButton.setText(R.string.sign_up)
        confirmPasswordInputLayout.visible()
        resetPasswordLabel.gone()
        resetPasswordButton.gone()
    }

    private fun FragmentAccountBinding.setupSignIn() {
        signInToolbar.setTitle(R.string.sign_in_with_email)
        signInButton.setText(R.string.sign_in)
        confirmPasswordInputLayout.gone()
        resetPasswordLabel.visible()
        resetPasswordButton.visible()
    }

    private fun FragmentAccountBinding.checkFieldErrors() {
        val signInEnabled = when {
            !confirmPasswordInputLayout.isVisible() -> {
                emailInputLayout.hasValidText() && passwordInputLayout.hasValidText()
            }
            else -> emailInputLayout.hasValidText()
                    && passwordInputLayout.hasValidText()
                    && confirmPasswordInputLayout.hasValidText()
        }

        signInButton.isEnabled = signInEnabled

        if (emailInputField.text.toString().isEmpty()) emailInputLayout.error = null
        if (passwordInputField.text.toString().isEmpty()) passwordInputLayout.error = null
        if (confirmPasswordInputField.text.toString().isEmpty()) confirmPasswordInputLayout.error = null
    }

    private fun showError(@StringRes messageRes: Int) {
        binding.root.showErrorSnackbar(message = messageRes, duration = Snackbar.LENGTH_LONG)
    }

    private fun navigateToRetroList() {
        findNavController().navigate(R.id.action_logged_in)
    }

    private fun navigateToEmailVerification() {
        findNavController().navigate(R.id.action_verify_email)
    }

    private fun navigateToResetPassword() {
        val bundle = Bundle().apply { putString(ARG_EMAIL, binding.emailInputField.text.toString()) }
        findNavController().navigate(R.id.action_reset_password, bundle)
    }

    private fun showUnknownUserError(): Snackbar {
        return binding.root.showErrorSnackbar(
            message = R.string.error_invalid_user,
            duration = Snackbar.LENGTH_INDEFINITE,
            actionText = R.string.sign_up
        ) {
            binding.setupSignUp()
        }
    }

    private fun showExistingUserError(): Snackbar {
        return binding.root.showErrorSnackbar(
            message = R.string.error_user_collision,
            duration = Snackbar.LENGTH_INDEFINITE,
            actionText = R.string.sign_in
        ) {
            binding.setupSignIn()
        }
    }

    private fun FragmentAccountBinding.showLoading() {
        signInButton.gone()
        resetPasswordButton.gone()
        resetPasswordLabel.gone()
        loading.visible()
    }

    private fun FragmentAccountBinding.hideLoading() {
        signInButton.visible()
        resetPasswordButton.visible()
        resetPasswordLabel.visible()
        loading.gone()
    }

    private fun getIsNewAccountArgument(): Boolean = arguments?.getBoolean(ARG_IS_NEW_ACCOUNT) ?: false

    private fun logPageEnter() {
        val event = if (!getIsNewAccountArgument()) PageEnterEvent(
            screen = Screen.SIGN_IN
        )
        else PageEnterEvent(screen = Screen.SIGN_UP)
        reportAnalytics(event = event)
    }

    private fun getAnalyticsScreen(): Screen = if (getIsNewAccountArgument()) Screen.SIGN_UP else Screen.SIGN_IN
}
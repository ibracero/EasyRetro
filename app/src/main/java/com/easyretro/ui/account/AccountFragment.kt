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
import com.easyretro.ui.account.ResetPasswordFragment.Companion.ARG_EMAIL
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_account.*


@AndroidEntryPoint
class AccountFragment : BaseFragment<AccountViewState, AccountViewEffect, AccountViewEvent, AccountViewModel>() {

    companion object {
        private const val MINIMUM_PASSWORD_LENGHT = 6
        private const val PASSWORD_PATTERN = "[a-zA-Z0-9]{$MINIMUM_PASSWORD_LENGHT,}"
        const val ARG_IS_NEW_ACCOUNT = "arg_is_new_account"
    }

    override val viewModel: AccountViewModel by viewModels()
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
            SigningState.Loading -> showLoading()
            SigningState.RequestDone -> hideLoading()
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
        if (isNewAccount) setupSignUp()
        else setupSignIn()

        sign_in_toolbar.setNavigationOnClickListener {
            reportAnalytics(
                event = TapEvent(
                    screen = getAnalyticsScreen(),
                    uiValue = UiValue.BACK
                )
            )
            findNavController().navigateUp()
        }

        val resetPasswordClickListener = { _: View ->
            reportAnalytics(
                event = TapEvent(
                    screen = getAnalyticsScreen(),
                    uiValue = UiValue.ACCOUNT_RESET_PASSWORD
                )
            )
            viewModel.process(AccountViewEvent.ResetPassword)
        }
        reset_password_button.setOnClickListener(resetPasswordClickListener)
        reset_password_label.setOnClickListener(resetPasswordClickListener)

        email_input_field.addTextWatcher(afterTextChanged = {
            if (Patterns.EMAIL_ADDRESS.toRegex().matches(email_input_field.text.toString())) {
                email_input_layout.error = null
            } else email_input_layout.error = getString(R.string.email_validation_error)

            checkFieldErrors()
        })

        password_input_field.addTextWatcher(afterTextChanged = {
            when {
                passwordRegex.matches(password_input_field.text.toString()) -> password_input_layout.error =
                    null
                password_input_field.length() < MINIMUM_PASSWORD_LENGHT -> {
                    password_input_layout.error =
                        getString(R.string.password_minimum_length_validation_error)
                }
                else -> password_input_layout.error =
                    getString(R.string.password_alphanumeric_validation_error)
            }

            checkFieldErrors()
        })

        confirm_password_input_field.addTextWatcher(afterTextChanged = {
            when {
                confirm_password_input_field.length() == 0 -> confirm_password_input_layout.error =
                    null
                password_input_field.text.toString() == confirm_password_input_field.text.toString() ->
                    confirm_password_input_layout.error = null
                else -> confirm_password_input_layout.error = getString(R.string.confirm_password_validation_error)
            }

            checkFieldErrors()
        })

        sign_in_button.setOnClickListener {
            reportAnalytics(
                event = TapEvent(
                    screen = getAnalyticsScreen(),
                    uiValue = if (getAnalyticsScreen() == Screen.SIGN_IN) UiValue.ACCOUNT_SIGN_IN else UiValue.ACCOUNT_SIGN_UP
                )
            )

            val email = email_input_field.text.toString()
            val password = password_input_field.text.toString()

            if (!confirm_password_input_layout.isVisible())
                viewModel.process(viewEvent = AccountViewEvent.SignIn(email, password))
            else viewModel.process(viewEvent = AccountViewEvent.SignUp(email, password))
        }

        checkFieldErrors()
    }

    private fun setupSignUp() {
        sign_in_toolbar.setTitle(R.string.sign_up_with_email)
        sign_in_button.setText(R.string.sign_up)
        confirm_password_input_layout.visible()
        reset_password_label.gone()
        reset_password_button.gone()
    }

    private fun setupSignIn() {
        sign_in_toolbar.setTitle(R.string.sign_in_with_email)
        sign_in_button.setText(R.string.sign_in)
        confirm_password_input_layout.gone()
        reset_password_label.visible()
        reset_password_button.visible()
    }

    private fun checkFieldErrors() {
        val signInEnabled = when {
            !confirm_password_input_layout.isVisible() -> {
                email_input_layout.hasValidText() && password_input_layout.hasValidText()
            }
            else -> email_input_layout.hasValidText()
                    && password_input_layout.hasValidText()
                    && confirm_password_input_layout.hasValidText()
        }

        sign_in_button.isEnabled = signInEnabled

        if (email_input_field.text.toString().isEmpty()) email_input_layout.error = null
        if (password_input_field.text.toString().isEmpty()) password_input_layout.error = null
        if (confirm_password_input_field.text.toString()
                .isEmpty()
        ) confirm_password_input_layout.error = null
    }

    private fun showError(@StringRes messageRes: Int) {
        account_root.showErrorSnackbar(message = messageRes, duration = Snackbar.LENGTH_LONG)
    }

    private fun navigateToRetroList() {
        findNavController().navigate(R.id.action_logged_in)
    }

    private fun navigateToEmailVerification() {
        findNavController().navigate(R.id.action_verify_email)
    }

    private fun navigateToResetPassword() {
        val bundle = Bundle().apply { putString(ARG_EMAIL, email_input_field.text.toString()) }
        findNavController().navigate(R.id.action_reset_password, bundle)
    }

    private fun showUnknownUserError(): Snackbar {
        return account_root.showErrorSnackbar(
            message = R.string.error_invalid_user,
            duration = Snackbar.LENGTH_INDEFINITE,
            actionText = R.string.sign_up
        ) {
            setupSignUp()
        }
    }

    private fun showExistingUserError(): Snackbar {
        return account_root.showErrorSnackbar(
            message = R.string.error_user_collision,
            duration = Snackbar.LENGTH_INDEFINITE,
            actionText = R.string.sign_in
        ) {
            setupSignIn()
        }
    }

    private fun showLoading() {
        sign_in_button?.gone()
        reset_password_button?.gone()
        reset_password_label?.gone()
        loading?.visible()
    }

    private fun hideLoading() {
        sign_in_button?.visible()
        reset_password_button?.visible()
        reset_password_label?.visible()
        loading?.gone()
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
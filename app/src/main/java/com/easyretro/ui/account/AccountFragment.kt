package com.easyretro.ui.account

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import arrow.core.Either
import com.easyretro.R
import com.easyretro.common.extensions.*
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.UserStatus
import com.easyretro.ui.FailureMessage
import com.easyretro.ui.account.ResetPasswordFragment.Companion.ARG_EMAIL
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_account.*
import org.koin.android.viewmodel.ext.android.viewModel

class AccountFragment : Fragment(R.layout.fragment_account) {

    companion object {
        private const val MINIMUM_PASSWORD_LENGHT = 6
        private const val PASSWORD_PATTERN = "[a-zA-Z0-9]{$MINIMUM_PASSWORD_LENGHT,}"
        const val ARG_IS_NEW_ACCOUNT = "arg_is_new_account"
    }

    private val accountViewModel: AccountViewModel by viewModel()
    private val passwordRegex = PASSWORD_PATTERN.toRegex()
    private val signInObserver = Observer<Either<Failure, UserStatus>> { processSignInResult(it) }
    private val signUpObserver = Observer<Either<Failure, Unit>> { processSignUpResult(it) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi(arguments?.getBoolean(ARG_IS_NEW_ACCOUNT) ?: false)
    }

    override fun onStart() {
        super.onStart()
        accountViewModel.onStart()

        accountViewModel.run {
            signInLiveData?.observe(this@AccountFragment, signInObserver)
            signUpLiveData?.observe(this@AccountFragment, signUpObserver)
        }
    }

    override fun onStop() {
        super.onStop()
        accountViewModel.onStop()
    }

    private fun initUi(isNewAccount: Boolean) {
        if (isNewAccount) setupSignUp()
        else setupSignIn()

        sign_in_toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val resetPasswordClickListener = { _: View -> navigateToResetPassword() }
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
                passwordRegex.matches(password_input_field.text.toString()) -> password_input_layout.error = null
                password_input_field.length() < MINIMUM_PASSWORD_LENGHT -> {
                    password_input_layout.error = getString(R.string.password_minimum_length_validation_error)
                }
                else -> password_input_layout.error = getString(R.string.password_alphanumeric_validation_error)
            }

            checkFieldErrors()
        })

        confirm_password_input_field.addTextWatcher(afterTextChanged = {
            when {
                confirm_password_input_field.length() == 0 -> confirm_password_input_layout.error = null
                password_input_field.text.toString() == confirm_password_input_field.text.toString() ->
                    confirm_password_input_layout.error = null
                else -> confirm_password_input_layout.error = getString(R.string.confirm_password_validation_error)
            }

            checkFieldErrors()
        })

        sign_in_button.setOnClickListener {
            val email = email_input_field.text.toString()
            val password = password_input_field.text.toString()

            if (!confirm_password_input_layout.isVisible()) accountViewModel.signIn(email, password)
            else accountViewModel.signUp(email, password)
        }

        checkFieldErrors()
    }

    private fun processSignInResult(response: Either<Failure, UserStatus>) {
        response.fold({
            when (it) {
                is Failure.InvalidUserFailure -> {
                    account_root.showErrorSnackbar(
                        message = R.string.error_invalid_user,
                        duration = Snackbar.LENGTH_INDEFINITE,
                        actionText = R.string.sign_up
                    ) {
                        setupSignUp()
                    }
                }
                else -> showError(FailureMessage.parse(it))
            }
        }, {
            when (it) {
                UserStatus.VERIFIED -> navigateToRetroList()
                UserStatus.NON_VERIFIED -> navigateToEmailVerification()
            }
        })
    }

    private fun processSignUpResult(response: Either<Failure, Unit>) {
        response.fold({
            when (it) {
                is Failure.UserCollisionFailure -> {
                    account_root.showErrorSnackbar(
                        message = R.string.error_user_collision,
                        duration = Snackbar.LENGTH_INDEFINITE,
                        actionText = R.string.sign_in
                    ) {
                        setupSignIn()
                    }
                }
                else -> showError(FailureMessage.parse(it))
            }
        }, {
            navigateToEmailVerification()
        })
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
        if (confirm_password_input_field.text.toString().isEmpty()) confirm_password_input_layout.error = null
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
}
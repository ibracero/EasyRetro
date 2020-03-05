package com.ibracero.retrum.ui.account

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import arrow.core.Either
import com.ibracero.retrum.R
import com.ibracero.retrum.common.addTextWatcher
import com.ibracero.retrum.common.hasValidText
import com.ibracero.retrum.common.isVisible
import com.ibracero.retrum.common.visible
import com.ibracero.retrum.data.remote.ServerError
import com.ibracero.retrum.domain.AccountRepository
import com.ibracero.retrum.ui.account.ResetPasswordFragment.Companion.ARG_EMAIL
import kotlinx.android.synthetic.main.fragment_login_email.*
import org.koin.android.viewmodel.ext.android.viewModel

class AccountFragment : Fragment(R.layout.fragment_login_email) {

    companion object {
        private const val MINIMUM_PASSWORD_LENGHT = 6
        private const val PASSWORD_PATTERN = "[a-zA-Z0-9]{$MINIMUM_PASSWORD_LENGHT,}"
        const val ARG_IS_NEW_ACCOUNT = "arg_is_new_account"
    }

    private val accountViewModel: AccountViewModel by viewModel()
    private val passwordRegex = PASSWORD_PATTERN.toRegex()
    private val signInObserver = Observer<Either<ServerError, AccountRepository.UserStatus>> { processSignInResult(it) }
    private val signUpObserver = Observer<Either<ServerError, Unit>> { processSignUpResult(it) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi(arguments?.getBoolean(ARG_IS_NEW_ACCOUNT) ?: false)
    }

    override fun onStart() {
        super.onStart()

        accountViewModel.run {
            signInLiveData.observe(this@AccountFragment, signInObserver)
            signUpLiveData.observe(this@AccountFragment, signUpObserver)
        }
    }

    override fun onStop() {
        super.onStop()
        accountViewModel.run {
            signInLiveData.removeObserver(signInObserver)
            signUpLiveData.removeObserver(signUpObserver)
        }
    }

    private fun processSignInResult(response: Either<ServerError, AccountRepository.UserStatus>) {
        response.fold({
            //showSnackbar
        }, {
            when (it) {
                AccountRepository.UserStatus.VERIFIED -> navigateToRetroList()
                AccountRepository.UserStatus.NON_VERIFIED -> navigateToEmailVerification()
                AccountRepository.UserStatus.UNKNOWN -> Unit
            }
        })
    }

    private fun processSignUpResult(response: Either<ServerError, Unit>) {
        response.fold({
            //showSnackbar
        }, {
            navigateToEmailVerification()
        })
    }

    private fun initUi(isNewAccount: Boolean) {

        if (isNewAccount) {
            sign_in_toolbar.setTitle(R.string.sign_up_with_email)
            sign_in_button.setText(R.string.sign_up)
            confirm_password_input_layout.visible()
        } else {
            sign_in_toolbar.setTitle(R.string.sign_in_with_email)
            sign_in_button.setText(R.string.sign_in)
            reset_password_label.visible()
            reset_password_button.visible()

            val resetPasswordClickListener = { _: View -> navigateToResetPassword() }
            reset_password_button.setOnClickListener(resetPasswordClickListener)
            reset_password_label.setOnClickListener(resetPasswordClickListener)
        }

        sign_in_toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

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

            if (!isNewAccount) accountViewModel.signIn(email, password)
            else accountViewModel.signUp(email, password)
        }

        checkFieldErrors()
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
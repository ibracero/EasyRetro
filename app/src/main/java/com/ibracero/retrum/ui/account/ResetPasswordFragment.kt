package com.ibracero.retrum.ui.account

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import arrow.core.Either
import com.ibracero.retrum.R
import com.ibracero.retrum.common.addTextWatcher
import com.ibracero.retrum.common.hasValidText
import com.ibracero.retrum.data.remote.ServerError
import kotlinx.android.synthetic.main.fragment_reset_password.*
import org.koin.android.viewmodel.ext.android.viewModel

class ResetPasswordFragment : Fragment(R.layout.fragment_reset_password) {

    companion object {
        const val ARG_EMAIL = "arg_email"
    }

    private val resetPasswordViewModel: ResetPasswordViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi(arguments?.getString(ARG_EMAIL).orEmpty())

        resetPasswordViewModel.resetPasswordLiveData.observe(this, Observer {
            processResetPasswordResponse(it)
        })
    }

    private fun initUi(email: String) {

        reset_password_toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        email_input_field.setText(email)

        email_input_field.addTextWatcher(afterTextChanged = {
            if (Patterns.EMAIL_ADDRESS.toRegex().matches(email_input_field.text.toString())) {
                email_input_layout.error = null
            } else email_input_layout.error = getString(R.string.email_validation_error)

            checkEmailField()
        })

        confirm_button.setOnClickListener { resetPasswordViewModel.resetPassword(email_input_field.text.toString()) }
        checkEmailField()
    }

    private fun checkEmailField() {
        confirm_button.isEnabled = email_input_layout.hasValidText()
    }

    private fun processResetPasswordResponse(response: Either<ServerError, Unit>) {
        response.fold({
            //showSnackbar
        }, {
            //show toast
            findNavController().navigateUp()
        })
    }
}
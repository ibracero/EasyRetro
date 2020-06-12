package com.easyretro.ui.account

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import arrow.core.Either
import com.easyretro.R
import com.easyretro.analytics.*
import com.easyretro.common.extensions.addTextWatcher
import com.easyretro.common.extensions.hasValidText
import com.easyretro.domain.model.Failure
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

        resetPasswordViewModel.resetPasswordLiveData.observe(viewLifecycleOwner, Observer {
            processResetPasswordResponse(it)
        })
    }

    override fun onStart() {
        super.onStart()
        reportAnalytics(event = PageEnterEvent(screen = Screen.RESET_PASSWORD))
    }

    private fun initUi(email: String) {

        reset_password_toolbar.setNavigationOnClickListener {
            reportAnalytics(event = TapEvent(screen = Screen.RESET_PASSWORD, uiValue = UiValue.BACK))
            findNavController().navigateUp()
        }

        email_input_field.setText(email)

        email_input_field.addTextWatcher(afterTextChanged = {
            if (Patterns.EMAIL_ADDRESS.toRegex().matches(email_input_field.text.toString())) {
                email_input_layout.error = null
            } else email_input_layout.error = getString(R.string.email_validation_error)

            checkEmailField()
        })

        confirm_button.setOnClickListener {
            reportAnalytics(event = TapEvent(screen = Screen.RESET_PASSWORD, uiValue = UiValue.RESET_PASSWORD_CONFIRM))
            resetPasswordViewModel.resetPassword(email_input_field.text.toString())
        }
        checkEmailField()
    }

    private fun checkEmailField() {
        confirm_button.isEnabled = email_input_layout.hasValidText()
    }

    private fun processResetPasswordResponse(response: Either<Failure, Unit>) {
        response.fold({
            //showSnackbar
        }, {
            //show toast
            findNavController().navigateUp()
        })
    }
}
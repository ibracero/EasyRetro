package com.easyretro.ui.account

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import arrow.core.Either
import com.easyretro.R
import com.easyretro.analytics.Screen
import com.easyretro.analytics.UiValue
import com.easyretro.analytics.events.PageEnterEvent
import com.easyretro.analytics.events.TapEvent
import com.easyretro.analytics.reportAnalytics
import com.easyretro.common.extensions.*
import com.easyretro.databinding.FragmentResetPasswordBinding
import com.easyretro.domain.model.Failure
import com.easyretro.ui.FailureMessage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetPasswordFragment : Fragment(R.layout.fragment_reset_password) {

    companion object {
        const val ARG_EMAIL = "arg_email"
    }

    private val binding by viewBinding(FragmentResetPasswordBinding::bind)
    private val resetPasswordViewModel: ResetPasswordViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi(arguments?.getString(ARG_EMAIL).orEmpty())

        resetPasswordViewModel.resetPasswordLiveData.observe(viewLifecycleOwner, {
            processResetPasswordResponse(it)
        })
    }

    override fun onStart() {
        super.onStart()
        reportAnalytics(event = PageEnterEvent(screen = Screen.RESET_PASSWORD))
    }

    private fun initUi(email: String) {
        with(binding) {
            resetPasswordToolbar.setNavigationOnClickListener {
                reportAnalytics(
                    event = TapEvent(
                        screen = Screen.RESET_PASSWORD,
                        uiValue = UiValue.BACK
                    )
                )
                findNavController().navigateUp()
            }

            emailInputField.setText(email)
            emailInputField.addTextWatcher(afterTextChanged = {
                if (Patterns.EMAIL_ADDRESS.toRegex().matches(emailInputField.text.toString())) {
                    emailInputLayout.error = null
                } else emailInputLayout.error = getString(R.string.email_validation_error)

                checkEmailField()
            })

            confirmButton.setOnClickListener {
                reportAnalytics(
                    event = TapEvent(
                        screen = Screen.RESET_PASSWORD,
                        uiValue = UiValue.RESET_PASSWORD_CONFIRM
                    )
                )
                resetPasswordViewModel.resetPassword(emailInputField.text.toString())
            }
            checkEmailField()
        }
    }

    private fun checkEmailField() {
        binding.confirmButton.isEnabled = binding.emailInputLayout.hasValidText()
    }

    private fun processResetPasswordResponse(response: Either<Failure, Unit>) {
        response.fold({
            binding.root.showErrorSnackbar(FailureMessage.parse(it))
        }, {
            binding.root.showSuccessSnackbar(R.string.reset_password_success)
            findNavController().navigateUp()
        })
    }
}
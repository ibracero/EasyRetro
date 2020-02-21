package com.ibracero.retrum.ui.account

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ibracero.retrum.R
import com.ibracero.retrum.common.addTextWatcher
import com.ibracero.retrum.common.hasValidText
import kotlinx.android.synthetic.main.fragment_reset_password.*
import org.koin.android.viewmodel.ext.android.viewModel

class ResetPasswordFragment : Fragment() {

    companion object {
        const val ARG_EMAIL = "arg_email"
    }

    private val resetPasswordViewModel: ResetPasswordViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_reset_password, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        initUi(arguments?.getString(ARG_EMAIL).orEmpty())
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
}
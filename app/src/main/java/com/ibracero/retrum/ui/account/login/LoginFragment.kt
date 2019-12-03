package com.ibracero.retrum.ui.account.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.ibracero.retrum.R
import kotlinx.android.synthetic.main.fragment_login_email.*
import org.koin.android.ext.android.inject

class LoginFragment : Fragment() {

    companion object {
        private const val MINIMUM_PASSWORD_LENGHT = 6
        private const val PASSWORD_PATTERN = "[a-zA-Z0-9]{$MINIMUM_PASSWORD_LENGHT,}"
    }

    private val presenter: LoginPresenter by inject()

    private val passwordRegex = PASSWORD_PATTERN.toRegex()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login_email, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        sign_in_toolbar.setNavigationOnClickListener { view ->
            view.findNavController().navigateUp()
        }

        setHasOptionsMenu(true)

        initUi()
    }

    private fun initUi() {
        email_input_field.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                if (Patterns.EMAIL_ADDRESS.toRegex().matches(editable.toString())) {
                    email_input_layout.error = null
                } else {
                    email_input_layout.error = "Please, insert a valid email address"
                }
            }

            override fun beforeTextChanged(editable: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(editable: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
        password_input_field.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                when {
                    passwordRegex.matches(editable.toString()) -> password_input_layout.error = null
                    editable.length < MINIMUM_PASSWORD_LENGHT -> {
                        password_input_layout.error = "Please, insert at least 6 alphanumeric characters"
                    }
                    else -> {
                        password_input_layout.error = "Please insert a valid password (alphanumeric characters only)"
                    }
                }
            }

            override fun beforeTextChanged(editable: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(editable: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        sign_in_button.setOnClickListener {
            presenter.signIn(
                email_input_field.text.toString(),
                password_input_field.text.toString()
            )
        }
    }
}
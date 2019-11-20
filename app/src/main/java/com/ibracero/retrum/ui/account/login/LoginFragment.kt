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

class LoginFragment : Fragment() {

    companion object {
        private const val PASSWORD_PATTERN = "^^([a-zA-Z0-9@*#]{8,15})$"

    }

    private val passwordRegex = Regex.fromLiteral(PASSWORD_PATTERN)

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
                    email_input_field.error = null
                } else {
                    email_input_field.error = "Please, insert a valid email address"
                }
            }

            override fun beforeTextChanged(editable: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(editable: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
        password_input_field.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                if (editable.toString().matches(passwordRegex)) {
                    password_input_field.error = null
                } else {
                    password_input_field.error = "Please, insert a valid email address"
                }
            }

            override fun beforeTextChanged(editable: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(editable: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }
}
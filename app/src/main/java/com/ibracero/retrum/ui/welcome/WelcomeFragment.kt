package com.ibracero.retrum.ui.welcome

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.ibracero.retrum.R
import com.ibracero.retrum.common.visible
import com.ibracero.retrum.domain.SignInCallback
import com.ibracero.retrum.ui.account.AccountFragment.Companion.ARG_IS_NEW_ACCOUNT
import kotlinx.android.synthetic.main.fragment_welcome.*
import org.koin.android.ext.android.inject


class WelcomeFragment : Fragment() {

    companion object {
        const val GOOGLE_SIGN_IN_REQUEST_CODE = 2901
        const val BUTTONS_SHOW_DELAY = 2000L

        const val ARG_IS_LOGOUT = "arg_logout"
    }

    private val buttonsLayoutHandler = Handler()
    private val welcomePresenter: WelcomePresenter by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        group_post_it.visible()
        if (arguments?.getBoolean(ARG_IS_LOGOUT) == true) {
            google_sign_in.visible()
            email_sign_in.visible()
        } else {
            buttonsLayoutHandler.postDelayed({
                if (!welcomePresenter.isSessionOpen()) {
                    google_sign_in.visible()
                    email_sign_in.visible()
                } else navigateToRetroList()
            }, BUTTONS_SHOW_DELAY)
        }

        google_sign_in.setOnClickListener { launchGoogleSignIn(it) }
        email_sign_in.setOnClickListener { navigateToLogin() }
        sign_up_button.setOnClickListener { navigateToRegister() }
    }

    private fun launchGoogleSignIn(it: View) {
        val signInIntent = GoogleSignIn.getClient(it.context, getSignInOptions()).signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        buttonsLayoutHandler.removeCallbacksAndMessages(null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            welcomePresenter.handleSignInResult(task, object : SignInCallback {
                override fun onSignedIn() {
                    navigateToRetroList()
                }

                override fun onError(throwable: Throwable) {
                    //show snackbar
                }
            })
        }
    }

    private fun navigateToRetroList() {
        findNavController().navigate(R.id.action_sign_in_success)
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.navigation_login_with_email)
    }

    private fun navigateToRegister() {
        val bundle = Bundle().apply { putBoolean(ARG_IS_NEW_ACCOUNT, true) }
        findNavController().navigate(R.id.navigation_login_with_email, bundle)
    }

    private fun getSignInOptions() =
        GoogleSignInOptions.Builder()
            .requestEmail()
            .requestProfile()
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()
}
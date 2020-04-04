package com.easyretro.ui.welcome

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.fragment.findNavController
import arrow.core.Either
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.easyretro.R
import com.easyretro.common.extensions.showErrorSnackbar
import com.easyretro.common.extensions.visible
import com.easyretro.domain.Failure
import com.easyretro.domain.UserStatus
import com.easyretro.ui.FailureMessage
import com.easyretro.ui.account.AccountFragment.Companion.ARG_IS_NEW_ACCOUNT
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import kotlinx.android.synthetic.main.fragment_welcome.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber


class WelcomeFragment : Fragment() {

    companion object {
        const val GOOGLE_SIGN_IN_REQUEST_CODE = 2901
    }

    private val buttonsLayoutHandler = Handler()
    private val welcomeViewModel: WelcomeViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi()
        welcomeViewModel.googleSignInLiveData.observe(viewLifecycleOwner, Observer { processGoogleSignInResponse(it) })
        welcomeViewModel.userSessionLiveData.observe(viewLifecycleOwner, Observer { processUserSession(it) })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task?.getResult(ApiException::class.java)
                welcomeViewModel.handleSignInResult(account)
            } catch (e: ApiException) {
                Timber.e(e)
                processGoogleSignInResponse(Either.left(Failure.UnknownError))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        buttonsLayoutHandler.removeCallbacksAndMessages(null)
    }

    private fun processUserSession(userStatus: Either<Failure, UserStatus>) {
        userStatus.fold({
            showButtons()
        }, {
            when (it) {
                UserStatus.VERIFIED -> navigateToRetroList()
                else -> showButtons()
            }
        })
    }

    private fun processGoogleSignInResponse(response: Either<Failure, Unit>) {
        response.fold({
            welcome_root.showErrorSnackbar(message = FailureMessage.parse(it), duration = Snackbar.LENGTH_LONG)
        }, {
            navigateToRetroList()
        })
    }

    private fun initUi() {
        group_post_it.visible()

        google_sign_in.setOnClickListener { launchGoogleSignIn(it) }
        email_sign_in.setOnClickListener { navigateToLogin() }
        sign_up_button.setOnClickListener { navigateToRegister() }
    }

    private fun showButtons() {
        google_sign_in.visible()
        email_sign_in.visible()
    }

    private fun launchGoogleSignIn(it: View) {
        val signInIntent = GoogleSignIn.getClient(it.context, getSignInOptions()).signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE)
    }

    private fun navigateToRetroList() {
        findNavController().navigate(R.id.action_logged_in)
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_sign_in_with_email)
    }

    private fun navigateToRegister() {
        val bundle = Bundle().apply { putBoolean(ARG_IS_NEW_ACCOUNT, true) }
        findNavController().navigate(R.id.action_register, bundle)
    }

    private fun getSignInOptions() =
        GoogleSignInOptions.Builder()
            .requestEmail()
            .requestProfile()
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()
}
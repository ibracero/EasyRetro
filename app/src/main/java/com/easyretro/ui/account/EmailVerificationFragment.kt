package com.easyretro.ui.account

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import arrow.core.Either
import com.google.android.material.snackbar.Snackbar
import com.easyretro.R
import com.easyretro.common.extensions.showErrorSnackbar
import com.easyretro.common.extensions.showSuccessSnackbar
import com.easyretro.domain.Failure
import com.easyretro.domain.UserStatus
import com.easyretro.ui.FailureMessage
import kotlinx.android.synthetic.main.fragment_email_verification.*
import org.koin.android.viewmodel.ext.android.viewModel


class EmailVerificationFragment : Fragment(R.layout.fragment_email_verification) {

    companion object {
        private const val USER_VERIFIED_NAVIGATION_DELAY = 1000L
    }

    private val viewModel: EmailVerificationViewModel by viewModel()
    private val userStatusObserver = Observer<Either<Failure, UserStatus>> { processUserStatus(it) }
    private val handler = Handler()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi()

        viewModel.sendVerificationLiveData.observe(this, Observer { processResendEmailResult(it) })
    }

    override fun onStart() {
        super.onStart()
        viewModel.userStatusLiveData.observe(this, userStatusObserver)
        viewModel.refreshUserStatus()
    }

    override fun onStop() {
        super.onStop()
        viewModel.userStatusLiveData.removeObserver(userStatusObserver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }

    private fun initUi() {
        email_verification_toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        resend_verification_email.setOnClickListener { viewModel.resendVerificationEmail() }
        open_email_app_button.setOnClickListener { openEmailIntent() }
    }

    private fun openEmailIntent() {
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_EMAIL) }
        requireActivity().startActivity(intent)
    }

    private fun processResendEmailResult(result: Either<Failure, Unit>) {
        result.fold({
            verification_root.showErrorSnackbar(message = FailureMessage.parse(it), duration = Snackbar.LENGTH_LONG)
        }, {
            verification_root.showSuccessSnackbar(
                message = R.string.confirmation_email_sent,
                duration = Snackbar.LENGTH_LONG
            )
        })
    }

    private fun processUserStatus(userStatusEither: Either<Failure, UserStatus>) {
        userStatusEither.fold(
            { failure ->
                showError(FailureMessage.parse(failure))
                Unit
            },
            {
                when (it) {
                    UserStatus.VERIFIED -> {
                        verification_root.showSuccessSnackbar(message = R.string.confirmation_user_verified)
                        navigateToRetroList()
                    }
                    UserStatus.NON_VERIFIED -> Unit
                }
            }
        )
    }

    private fun showError(@StringRes message: Int) =
        verification_root.showErrorSnackbar(message = message, duration = Snackbar.LENGTH_LONG)

    private fun navigateToRetroList() {
        handler.postDelayed({
            findNavController().navigate(R.id.action_email_verified)
        }, USER_VERIFIED_NAVIGATION_DELAY)
    }
}
package com.ibracero.retrum.ui.account

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import arrow.core.Either
import com.ibracero.retrum.R
import com.ibracero.retrum.domain.Failure
import com.ibracero.retrum.domain.UserStatus
import kotlinx.android.synthetic.main.fragment_email_verification.*
import org.koin.android.viewmodel.ext.android.viewModel


class EmailVerificationFragment : Fragment(R.layout.fragment_email_verification) {

    private val viewModel: EmailVerificationViewModel by viewModel()
    private val userStatusObserver = Observer<Either<Failure, UserStatus>> { processUserStatus(it) }

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
            //showSnackbar
        }, {

        })
    }

    private fun processUserStatus(userStatusEither: Either<Failure, UserStatus>) {
        userStatusEither.fold(
            {
                //process failure
            },
            {
                when (it) {
                    UserStatus.VERIFIED -> {
                        //showSnackbar
                        //delay
                        navigateToRetroList()
                    }
                    UserStatus.NON_VERIFIED -> Unit
                }
            }
        )
    }

    private fun navigateToRetroList() {
        findNavController().navigate(R.id.action_email_verified)
    }
}
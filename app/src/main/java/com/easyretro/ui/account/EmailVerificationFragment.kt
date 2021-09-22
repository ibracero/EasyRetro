package com.easyretro.ui.account

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.StringRes
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
import com.easyretro.analytics.events.UserSignedUpEvent
import com.easyretro.analytics.reportAnalytics
import com.easyretro.common.extensions.showErrorSnackbar
import com.easyretro.common.extensions.showSuccessSnackbar
import com.easyretro.common.extensions.viewBinding
import com.easyretro.databinding.FragmentEmailVerificationBinding
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.UserStatus
import com.easyretro.ui.FailureMessage
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class EmailVerificationFragment : Fragment(R.layout.fragment_email_verification) {

    companion object {
        private const val USER_VERIFIED_NAVIGATION_DELAY = 1000L
    }

    private val binding by viewBinding(FragmentEmailVerificationBinding::bind)
    private val viewModel: EmailVerificationViewModel by viewModels()
    private val userStatusObserver = Observer<Either<Failure, UserStatus>> { processUserStatus(it) }
    private val handler = Handler(Looper.getMainLooper())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi()

        viewModel.sendVerificationLiveData.observe(viewLifecycleOwner, { processResendEmailResult(it) })
    }

    override fun onStart() {
        super.onStart()
        reportAnalytics(event = PageEnterEvent(screen = Screen.VERIFY_EMAIL))

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
        binding.emailVerificationToolbar.setNavigationOnClickListener {
            reportAnalytics(
                event = TapEvent(
                    screen = Screen.VERIFY_EMAIL,
                    uiValue = UiValue.BACK
                )
            )
            findNavController().popBackStack()
        }
        binding.resendVerificationEmail.setOnClickListener {
            reportAnalytics(
                event = TapEvent(
                    screen = Screen.VERIFY_EMAIL,
                    uiValue = UiValue.RESEND_EMAIL_VERIFICATION
                )
            )
            viewModel.resendVerificationEmail()
        }
        binding.openEmailAppButton.setOnClickListener {
            reportAnalytics(
                event = TapEvent(
                    screen = Screen.VERIFY_EMAIL,
                    uiValue = UiValue.OPEN_EMAIL_APP
                )
            )
            openEmailIntent()
        }
    }

    private fun openEmailIntent() {
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_EMAIL) }
        requireActivity().startActivity(intent)
    }

    private fun processResendEmailResult(result: Either<Failure, Unit>) {
        result.fold({
            binding.root.showErrorSnackbar(message = FailureMessage.parse(it), duration = Snackbar.LENGTH_LONG)
        }, {
            binding.root.showSuccessSnackbar(
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
                        binding.root.showSuccessSnackbar(message = R.string.confirmation_user_verified)
                        reportAnalytics(event = UserSignedUpEvent)
                        navigateToRetroList()
                    }
                    UserStatus.NON_VERIFIED -> Unit
                }
            }
        )
    }

    private fun showError(@StringRes message: Int) =
        binding.root.showErrorSnackbar(message = message, duration = Snackbar.LENGTH_LONG)

    private fun navigateToRetroList() {
        handler.postDelayed({
            findNavController().navigate(R.id.action_email_verified)
        }, USER_VERIFIED_NAVIGATION_DELAY)
    }
}
package im.vector.app.features.onboarding.ftueauth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.mvrx.withState
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.core.extensions.hideKeyboard
import im.vector.app.databinding.FragmentFtueCombinedRegisterBinding
import im.vector.app.features.onboarding.OnboardingAction
import im.vector.app.features.onboarding.OnboardingAction.AuthenticateAction
import im.vector.app.features.onboarding.OnboardingViewState
import im.vector.lib.strings.CommonStrings
import javax.inject.Inject

private const val MINIMUM_PASSWORD_LENGTH = 8

@AndroidEntryPoint
class FtueAuthCombinedRegisterFragment :
        AbstractSSOFtueAuthFragment<FragmentFtueCombinedRegisterBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentFtueCombinedRegisterBinding {
        return FragmentFtueCombinedRegisterBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSubmitButton()
    }

    private fun setupSubmitButton() {
        views.registerSubmit.setOnClickListener {
            submit()
        }
    }

    private fun submit() {
        withState(viewModel) { state ->
            if (state.isLoading) return@withState

            cleanupUi()
            val fullName = views.registerDisplayNameInput.text.toString().trim()
            val mobile = views.registerUsernameInput.text.toString().trim()
            val password = views.registerPasswordInput.text.toString()

            // 1. REGEX for Indian Mobile: Starts with 6, 7, 8, or 9, followed by 9 digits
            val indianMobileRegex = "^[6-9]\\d{9}$".toRegex()

            var hasError = false

            // Validate Full Name
            if (fullName.isEmpty()) {
                views.registerDisplayNameInputLayout.error = "Please enter your full name"
                hasError = true
            }

            // 2. VALIDATE MOBILE: Must be exactly 10 digits and Indian format
            if (!mobile.matches(indianMobileRegex)) {
                views.registerUsernameInputLayout.error = "Enter a valid 10-digit Indian number (starts with 6-9)"
                hasError = true
            }

            // Validate Password
            if (password.length < MINIMUM_PASSWORD_LENGTH) {
                views.registerPasswordInputLayout.error = getString(CommonStrings.login_signup_password_hint)
                hasError = true
            }

            // Stop if there is any validation error
            if (hasError) return@withState

            views.registerSubmit.hideKeyboard()

            // 3. THE TOTAL SOLUTION: Add a 'u' prefix
            // This bypasses the "Numeric user IDs reserved" error 100% of the time.
            val safeId = "u$mobile"

            // 4. We fire ONLY ONE action to prevent the "App Closing" navigation crash.
            // We set the Full Name as the 'initialDeviceName' so the server gets it immediately.
            viewModel.handle(OnboardingAction.AuthenticateAction.Register(
                    username = safeId,
                    password = password,
                    initialDeviceName = fullName
            ))
        }
    }

    private fun cleanupUi() {
        views.registerSubmit.hideKeyboard()
        views.registerDisplayNameInputLayout.error = null
        views.registerUsernameInputLayout.error = null
        views.registerPasswordInputLayout.error = null
    }

    override fun onError(throwable: Throwable) {
        // Show server errors on the mobile number box
        views.registerUsernameInputLayout.error = errorFormatter.toHumanReadable(throwable)
    }

    override fun updateWithState(state: OnboardingViewState) {
        // FORCE all boxes to stay visible
        views.registerDisplayNameInputLayout.visibility = View.VISIBLE
        views.registerUsernameInputLayout.visibility = View.VISIBLE
        views.registerPasswordInputLayout.visibility = View.VISIBLE

        // Disable button while loading to prevent crashes
        views.registerSubmit.isEnabled = !state.isLoading

        // Update button text to show progress
        views.registerSubmit.text = if (state.isLoading) "CREATING ACCOUNT..." else "FINISH REGISTRATION"
    }



    override fun resetViewModel() {
        viewModel.handle(OnboardingAction.ResetAuthenticationAttempt)
    }
}

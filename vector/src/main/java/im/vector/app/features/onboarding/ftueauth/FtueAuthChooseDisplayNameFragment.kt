package im.vector.app.features.onboarding.ftueauth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.databinding.FragmentFtueDisplayNameBinding
import im.vector.app.features.onboarding.OnboardingAction
import im.vector.app.features.onboarding.OnboardingViewEvents
import im.vector.app.features.onboarding.OnboardingViewState

@AndroidEntryPoint
class FtueAuthChooseDisplayNameFragment :
        AbstractFtueAuthFragment<FragmentFtueDisplayNameBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentFtueDisplayNameBinding {
        return FragmentFtueDisplayNameBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        views.displayNameSubmit.debouncedClicks {
            val name = views.displayNameInput.editText?.text.toString()
            viewModel.handle(OnboardingAction.UpdateDisplayName(name))
        }
        views.displayNameSkip.debouncedClicks { viewModel.handle(OnboardingAction.UpdateDisplayNameSkipped) }
    }

    override fun updateWithState(state: OnboardingViewState) {
        views.displayNameSubmit.isEnabled = !views.displayNameInput.editText?.text.isNullOrEmpty()
    }

    override fun resetViewModel() {}

    override fun onBackPressed(toolbarButton: Boolean): Boolean {
        viewModel.handle(OnboardingAction.PostViewEvent(OnboardingViewEvents.OnTakeMeHome))
        return true
    }
}

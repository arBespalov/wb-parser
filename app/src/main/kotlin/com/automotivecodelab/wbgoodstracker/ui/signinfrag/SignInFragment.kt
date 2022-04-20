package com.automotivecodelab.wbgoodstracker.ui.signinfrag

import android.app.Activity
import android.content.IntentSender
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.automotivecodelab.wbgoodstracker.*
import com.automotivecodelab.wbgoodstracker.databinding.SignInFragmentBinding
import com.automotivecodelab.wbgoodstracker.ui.EventObserver
import com.automotivecodelab.wbgoodstracker.ui.ViewModelFactory
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import timber.log.Timber

class SignInFragment : Fragment() {

    private val oneTapClient: SignInClient by lazy { Identity.getSignInClient(requireContext()) }
    private val getUserCredentials = registerForActivityResult(ActivityResultContracts
        .StartIntentSenderForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                viewModel.signIn(credential.googleIdToken!!)
            } catch (e: Exception) {
                viewModel.setError(e)
            }
        }
    }
    private val viewModel: SignInViewModel by viewModels {
        ViewModelFactory(requireContext().appComponent.signInViewModel())
    }
    private var viewDataBinding: SignInFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.sign_in_fragment, container, false)

        viewDataBinding = SignInFragmentBinding.bind(view).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        postponeEnterTransition()
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        return view
    }

    override fun onDestroyView() {
        viewDataBinding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        val cancelButton = ResourcesCompat.getDrawable(
            resources,
            R.drawable.ic_baseline_close_24,
            requireActivity().theme
        )
        viewDataBinding?.apply {
            toolbar.setupWithNavController(navController, appBarConfiguration)
            toolbar.navigationIcon = cancelButton
            swipeRefresh.isEnabled = false
        }

        setupNavigation()
        viewModel.viewState.observe(viewLifecycleOwner) {
            when (it) {
                is SignInViewState.SignedOutState -> setSignedOutState()
                is SignInViewState.SignedInState -> setSignedInState(it.email)
                is SignInViewState.LoadingState -> setLoadingState()
            }
        }
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun setupNavigation() {
        viewModel.networkErrorEvent.observe(viewLifecycleOwner, EventObserver {
                val action = SignInFragmentDirections.actionSignInFragmentToErrorDialogFragment(it)
                navigate(action)
            }
        )
    }

    private fun setLoadingState() {
        viewDataBinding?.apply {
            hint.text = getString(R.string.please_wait)
            signInButton.visibility = View.INVISIBLE
            signOutButton.visibility = View.INVISIBLE
            swipeRefresh.isRefreshing = true
        }
    }

    private fun setSignedOutState() {
        viewDataBinding?.apply {
            hint.text = getString(R.string.sign_in_hint)
            signInButton.apply {
                visibility = View.VISIBLE
                setOnClickListener { beginAuthenticationFlow(signUp = false) }
            }
            signOutButton.visibility = View.INVISIBLE
            swipeRefresh.isRefreshing = false
        }
    }

    private fun setSignedInState(email: String?) {
        viewDataBinding?.apply {
            if (email.isNullOrEmpty()) {
                hint.text = getString(R.string.sign_out_hint_without_personalization)
            } else {
                hint.text = getString(R.string.sign_out_hint, email)
            }
            signOutButton.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    viewModel.signOut()
                    Snackbar.make(rootView, R.string.sign_out_message, Snackbar.LENGTH_LONG).show()
                }
            }
            signInButton.visibility = View.INVISIBLE
            swipeRefresh.isRefreshing = false
        }
    }

    private fun beginAuthenticationFlow(signUp: Boolean) {
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(BuildConfig.SERVER_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(!signUp)
                    .build()
            )
            // .setAutoSelectEnabled(true)
            .build()
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    getUserCredentials.launch(IntentSenderRequest.Builder(
                        result.pendingIntent.intentSender).build())
                } catch (e: IntentSender.SendIntentException) {
                    Timber.d("Couldn't start One Tap UI: ${e.message}")
                    viewModel.setError(e)
                }
            }
            .addOnFailureListener { e ->
                Timber.d(e.message.toString())
                if (signUp)
                    viewModel.setError(e)
                else
                    beginAuthenticationFlow(signUp = true)
            }
    }
}

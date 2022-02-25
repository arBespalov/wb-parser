package com.automotivecodelab.wbgoodstracker.ui.signinfrag

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.automotivecodelab.wbgoodstracker.*
import com.automotivecodelab.wbgoodstracker.databinding.SignInFragmentBinding
import com.automotivecodelab.wbgoodstracker.domain.models.User
import com.automotivecodelab.wbgoodstracker.ui.EventObserver
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.material.transition.MaterialSharedAxis

class SignInFragment : Fragment() {

    private val viewModel: SignInViewModel by viewModels {
        SignInViewModelFactory(getItemsRepository(), getUserRepository())
    }
    private var viewDataBinding: SignInFragmentBinding? = null
    private val REQ_ONE_TAP = 77
    private val oneTapClient: SignInClient by lazy { Identity.getSignInClient(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.sign_in_fragment, container, false)

        viewDataBinding = SignInFragmentBinding.bind(view).apply {
            lifecycleOwner = viewLifecycleOwner
        }

        return view
    }

    override fun onDestroyView() {
        viewDataBinding = null
        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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

        viewModel.start()
        setupNavigation()
        viewModel.viewState.observe(
            viewLifecycleOwner,
            Observer {
                when (it) {
                    is SignInViewState.SignedOutState -> setSignedOutState()
                    is SignInViewState.SignedInState -> setSignedInState(it.email)
                    is SignInViewState.LoadingState -> setLoadingState()
                }
            }
        )

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    private fun setupNavigation() {
        viewModel.networkErrorEvent.observe(
            viewLifecycleOwner,
            EventObserver {
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
                setOnClickListener { beginSignIn() }
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
                    oneTapClient.signOut()
                    viewModel.signOut()
                    Toast.makeText(
                        requireContext(),
                        R.string.sign_out_message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            signInButton.visibility = View.INVISIBLE
            swipeRefresh.isRefreshing = false
        }
    }

    private fun beginSignIn() {

        val signInRequest: BeginSignInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(BuildConfig.SERVER_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(true)
                    .build()
            )
            // .setAutoSelectEnabled(true)
            .build()

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null
                    )
                } catch (e: IntentSender.SendIntentException) {
                    log("Couldn't start One Tap UI: ${e.message}")
                }
            }
            .addOnFailureListener { e ->
                log(e.message.toString())
                beginSignUp()
            }
    }

    private fun beginSignUp() {
        val signUpRequest: BeginSignInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(BuildConfig.SERVER_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            ).build()

        oneTapClient.beginSignIn(signUpRequest)
            .addOnSuccessListener { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null
                    )
                } catch (e: IntentSender.SendIntentException) {
                    log("Couldn't start One Tap UI: ${e.message}")
                }
            }
            .addOnFailureListener { e ->
                log(e.message.toString())
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)

                    val user = User(credential.googleIdToken!!, email = null)

                    viewModel.handleSignInResult(user)
                } catch (e: ApiException) {
                    log(e.message.toString())
                }
            }
        }
    }
}

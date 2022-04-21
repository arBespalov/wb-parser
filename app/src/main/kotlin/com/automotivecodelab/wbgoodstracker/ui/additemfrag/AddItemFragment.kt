package com.automotivecodelab.wbgoodstracker.ui.additemfrag

import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat.getDrawable
import androidx.core.view.doOnPreDraw
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.automotivecodelab.wbgoodstracker.*
import com.automotivecodelab.wbgoodstracker.databinding.AddItemFragmentBinding
import com.automotivecodelab.wbgoodstracker.ui.EventObserver
import com.automotivecodelab.wbgoodstracker.ui.ViewModelFactory
import com.google.android.material.transition.MaterialContainerTransform

class AddItemFragment : Fragment() {

    private val viewModel: AddItemViewModel by viewModels {
        ViewModelFactory(requireContext().appComponent.addItemViewModel())
    }
    private var viewDataBinding: AddItemFragmentBinding? = null
    private val args: AddItemFragmentArgs by navArgs()
    private var isArgsUrlHandled = false
    private var inputTextChangedListener: TextWatcher? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_item_fragment, container, false)
        viewDataBinding = AddItemFragmentBinding.bind(view).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        postponeEnterTransition()
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            endContainerColor = requireContext().themeColor(R.attr.colorSurface)
        }
        sharedElementReturnTransition = MaterialContainerTransform().apply {
            startContainerColor = requireContext().themeColor(R.attr.colorSurface)
        }
        return view
    }

    override fun onDestroyView() {
        viewDataBinding?.input?.removeTextChangedListener(inputTextChangedListener)
        inputTextChangedListener = null
        viewDataBinding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        val cancelButton = getDrawable(
            resources,
            R.drawable.ic_baseline_close_24,
            requireActivity().theme
        )
        viewDataBinding?.apply {
            toolbar.setupWithNavController(navController, appBarConfiguration)
            toolbar.navigationIcon = cancelButton
            swipeRefresh.isEnabled = false
            fabSave.setOnClickListener { viewModel.saveItem() }
            viewModel.inputState.observe(viewLifecycleOwner) { state ->
                textInputLayout.error =  when (state) {
                    UserInputState.INVALID_URL -> getString(R.string.invalid_url)
                    UserInputState.INVALID_VENDOR_CODE -> getString(R.string.invalid_vendor_code)
                    UserInputState.OK -> null
                    else -> null
                }
            }
            viewModel.dataLoading.observe(viewLifecycleOwner) {
                if (it) {
                    swipeRefresh.isRefreshing = true
                    fabSave.hide()
                } else {
                    swipeRefresh.isRefreshing = false
                    fabSave.show()
                }
            }
            fabSave.isEnabled = false
            inputTextChangedListener = input.addTextChangedListener {
                fabSave.isEnabled = !it.isNullOrEmpty()
                viewModel.handleTextInput(it.toString())
            }
            if (!isArgsUrlHandled) {
                args.url?.let { input.setText(it) }
                isArgsUrlHandled = true
            }
        }
        viewModel.authorizationErrorEvent.observe(viewLifecycleOwner, EventObserver {
                requireView().syncErrorSnackbar()
            }
        )
        setupNavigation()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun setupNavigation() {
        viewModel.saveSuccessfulEvent.observe(viewLifecycleOwner, EventObserver {
                findNavController().navigateUp()
            }
        )
        viewModel.errorEvent.observe(viewLifecycleOwner, EventObserver {
                val action = AddItemFragmentDirections
                    .actionAddItemFragmentToErrorDialogFragment(it)
                navigate(action)
            }
        )
    }
}

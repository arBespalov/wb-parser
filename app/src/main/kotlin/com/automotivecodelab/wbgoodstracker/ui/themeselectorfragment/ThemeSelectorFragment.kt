package com.automotivecodelab.wbgoodstracker.ui.themeselectorfragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.automotivecodelab.wbgoodstracker.MyApplication
import com.automotivecodelab.wbgoodstracker.R
import com.automotivecodelab.wbgoodstracker.appComponent
import com.automotivecodelab.wbgoodstracker.databinding.ThemeSelectorFragmentBinding
import com.automotivecodelab.wbgoodstracker.ui.AppTheme
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ThemeSelectorFragment : Fragment() {

    private var viewDataBinding: ThemeSelectorFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.theme_selector_fragment, container, false)

        viewDataBinding = ThemeSelectorFragmentBinding.bind(view).apply {
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
        val cancelButton = ResourcesCompat
            .getDrawable(resources, R.drawable.ic_baseline_close_24, requireActivity().theme)
        val appThemeSource = requireContext().appComponent.appThemeSource()
        val currentTheme = runBlocking { appThemeSource.getAppTheme() }
        viewDataBinding?.apply {
            toolbar.setupWithNavController(navController, appBarConfiguration)
            toolbar.navigationIcon = cancelButton
            val currentThemeToRadioButton = mapOf(
                AppTheme.AUTO to systemDefault,
                AppTheme.LIGHT to day,
                AppTheme.DARK to night
            )
            currentThemeToRadioButton[currentTheme]?.isChecked = true
            currentThemeToRadioButton.forEach { (appTheme, button) ->
                button.setOnClickListener {
                    lifecycleScope.launch { appThemeSource.saveAndSetupAppTheme(appTheme) }
                }
            }
        }
        view.doOnPreDraw { startPostponedEnterTransition() }
    }
}

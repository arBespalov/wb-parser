package com.automotivecodelab.wbgoodstracker.ui.themeselectorfragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.automotivecodelab.wbgoodstracker.PREFS_NAME
import com.automotivecodelab.wbgoodstracker.R
import com.automotivecodelab.wbgoodstracker.SAVED_UI_MODE
import com.automotivecodelab.wbgoodstracker.databinding.ThemeSelectorFragmentBinding
import com.google.android.material.transition.MaterialSharedAxis

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
        val cancelButton = ResourcesCompat
            .getDrawable(resources, R.drawable.ic_baseline_close_24, requireActivity().theme)
        viewDataBinding?.apply {
            toolbar.setupWithNavController(navController, appBarConfiguration)
            toolbar.navigationIcon = cancelButton

            when (AppCompatDelegate.getDefaultNightMode()) {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> systemDefault.isChecked = true
                AppCompatDelegate.MODE_NIGHT_NO -> day.isChecked = true
                AppCompatDelegate.MODE_NIGHT_YES -> night.isChecked = true
                AppCompatDelegate.MODE_NIGHT_UNSPECIFIED -> systemDefault.isChecked = true
            }

            systemDefault.setOnClickListener {
                setUIMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            day.setOnClickListener { setUIMode(AppCompatDelegate.MODE_NIGHT_NO) }
            night.setOnClickListener { setUIMode(AppCompatDelegate.MODE_NIGHT_YES) }
        }

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    private fun setUIMode(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(mode)
        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(SAVED_UI_MODE, mode)
            .apply()
    }
}

package com.automotivecodelab.wbgoodstracker.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.automotivecodelab.wbgoodstracker.MainNavDirections
import com.automotivecodelab.wbgoodstracker.R
import com.automotivecodelab.wbgoodstracker.appComponent
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity(), KeyboardToggle {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appThemeSource = application.appComponent.appThemeSource()
        runBlocking {
            appThemeSource.saveAndSetupAppTheme(appThemeSource.getAppTheme())
        }
        setTheme(R.style.Theme_WBParser)
        setContentView(R.layout.activity_main)
        handleIntentAndNavigateToAddItemFragment(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        handleIntentAndNavigateToAddItemFragment(intent)
        super.onNewIntent(intent)
    }

    private fun handleIntentAndNavigateToAddItemFragment(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val intentValue = intent.getStringExtra(Intent.EXTRA_TEXT)
            // mark intent as handled for the case of activity recreation:
            intent.removeExtra(Intent.EXTRA_TEXT)
            if (intentValue != null && intentValue.contains("wildberries")) {
                val action = MainNavDirections.actionGlobalAddItemFragment(intentValue)
                // findNavController() will fail when launched from onCreate()
                (supportFragmentManager.findFragmentById(R.id.fragment) as? NavHostFragment)
                    ?.navController?.navigate(action)
            }
        }
    }

    override fun hideKeyboard(view: View) {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun showKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}

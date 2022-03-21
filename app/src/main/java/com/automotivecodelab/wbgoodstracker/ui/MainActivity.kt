package com.automotivecodelab.wbgoodstracker.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.automotivecodelab.wbgoodstracker.MainNavDirections
import com.automotivecodelab.wbgoodstracker.R

class MainActivity : AppCompatActivity(), KeyboardToggle {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                findNavController(R.id.fragment).navigate(action)
            }
        }
    }

    override fun hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun showKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}

package com.automotivecodelab.wbgoodstracker.ui

import android.view.View
import com.automotivecodelab.wbgoodstracker.R
import com.google.android.material.snackbar.Snackbar

class SignOutSnackbar {
    operator fun invoke(view: View, onAction: () -> Unit) {
        val snackbar = Snackbar.make(view, R.string.snackbar_sign_out_text, Snackbar.LENGTH_LONG)
        snackbar.setAction(R.string.sign_out) { onAction() }
        snackbar.show()
    }
}

package com.automotivecodelab.wbgoodstracker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.res.use
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import com.automotivecodelab.wbgoodstracker.domain.repositories.ItemsRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.SortRepository
import com.automotivecodelab.wbgoodstracker.domain.repositories.UserRepository
import com.google.android.material.snackbar.Snackbar
import kotlin.math.ceil

fun String.httpToHttps(): String {
    return if (!contains("https")) {
        "https" + drop(4)
    } else {
        this
    }
}

fun Long.millisToDays(): Int {
    return ceil((toDouble() / (24 * 60 * 60 * 1000))).toInt()
}

// avoiding duplicate navigation to prevent crashes when click 2 views simultaneously
fun Fragment.navigate(directions: NavDirections) {
    val controller = findNavController()
    val className = when (val currentDestination = controller.currentDestination) {
        is DialogFragmentNavigator.Destination -> {
            currentDestination.className
        }
        is FragmentNavigator.Destination -> {
            currentDestination.className
        }
        else -> return
    }
    if (className == this.javaClass.name) {
        controller.navigate(directions)
    }
}

fun Fragment.navigate(directions: NavDirections, extras: FragmentNavigator.Extras? = null) {
    val controller = findNavController()
    val className = when (val currentDestination = controller.currentDestination) {
        is DialogFragmentNavigator.Destination -> {
            currentDestination.className
        }
        is FragmentNavigator.Destination -> {
            currentDestination.className
        }
        else -> return
    }
    if (className == this.javaClass.name) {
        if (extras != null) controller.navigate(directions, extras)
        else controller.navigate(directions)
    }
}

@ColorInt
fun Context.themeColor(
    @AttrRes themeAttrId: Int
): Int {
    return obtainStyledAttributes(
        intArrayOf(themeAttrId)
    ).use {
        it.getColor(0, Color.MAGENTA)
    }
}

fun View.signOutSnackbar(onSignOutButtonClick: () -> Unit) {
    val snackbar = Snackbar.make(this, R.string.snackbar_sign_out_text, Snackbar.LENGTH_LONG)
    snackbar.setAction(R.string.sign_out) { onSignOutButtonClick() }
    snackbar.show()
}

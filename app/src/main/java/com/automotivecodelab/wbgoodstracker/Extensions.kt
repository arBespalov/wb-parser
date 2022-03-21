package com.automotivecodelab.wbgoodstracker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
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
import kotlin.math.ceil

fun httpToHttps(url: String): String {
    return if (!url.contains("https")) {
        "https" + url.drop(4)
    } else {
        url
    }
}

fun log(text: String) {
    Log.d("happy", text)
}

fun millisToDays(millis: Long): Int {
    return ceil((millis.toDouble() / (24 * 60 * 60 * 1000))).toInt()
}

fun Fragment.getItemsRepository(): ItemsRepository {
    return (requireActivity().application as MyApplication).appContainer.itemsRepository
}

fun Fragment.getUserRepository(): UserRepository {
    return (requireActivity().application as MyApplication).appContainer.userRepository
}

fun Fragment.getSortRepository(): SortRepository {
    return (requireActivity().application as MyApplication).appContainer.sortRepository
}

// avoiding duplicate navigation to prevent crashes when click 2 views simultaneously
fun Fragment.navigate(directions: NavDirections) {
    val controller = findNavController()
    when (val currentDestination = controller.currentDestination) {
        is DialogFragmentNavigator.Destination -> {
            if (currentDestination.className == this.javaClass.name) {
                controller.navigate(directions)
            }
        }
        is FragmentNavigator.Destination -> {
            if (currentDestination.className == this.javaClass.name) {
                controller.navigate(directions)
            }
        }
    }
}

fun Fragment.navigate(directions: NavDirections, extras: FragmentNavigator.Extras) {
    val controller = findNavController()
    when (val currentDestination = controller.currentDestination) {
        is DialogFragmentNavigator.Destination -> {
            if (currentDestination.className == this.javaClass.name) {
                controller.navigate(directions, extras)
            }
        }
        is FragmentNavigator.Destination -> {
            if (currentDestination.className == this.javaClass.name) {
                controller.navigate(directions, extras)
            }
        }
    }
}

@ColorInt
@SuppressLint("Recycle")
fun Context.themeColor(
    @AttrRes themeAttrId: Int
): Int {
    return obtainStyledAttributes(
        intArrayOf(themeAttrId)
    ).use {
        it.getColor(0, Color.MAGENTA)
    }
}

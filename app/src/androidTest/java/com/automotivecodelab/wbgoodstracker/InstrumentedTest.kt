package com.automotivecodelab.wbgoodstracker

import android.widget.AutoCompleteTextView
import androidx.core.view.isVisible
import androidx.test.espresso.*
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.automotivecodelab.wbgoodstracker.ui.MainActivity
import com.automotivecodelab.wbgoodstracker.ui.itemsfrag.recyclerview.ItemsAdapter
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.CoreMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
// selection mode is not covered here
// should be at least 11 items in recycler
class InstrumentedTest {

    private val stringToBeTyped = "example"

    @get:Rule
    var activityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    private fun getResourceString(id: Int): String {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        return targetContext.resources.getString(id)
    }

    private fun toggleOrientation() {
        Thread.sleep(1500)
        if (UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            .isNaturalOrientation
        ) {
            setAlbumOrientation()
        } else {
            setPortraitOrientation()
        }
        Thread.sleep(1500)
    }

    private fun setPortraitOrientation() {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).setOrientationNatural()
    }

    private fun setAlbumOrientation() {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).setOrientationLeft()
    }

    @Test
    fun addItemRotation() {
        // goto additem screen
        onView(withId(R.id.fab_additem))
            .perform(click())
        // type non-url text
        onView(withId(R.id.URL))
            .perform(
                typeText(stringToBeTyped),
                androidx.test.espresso.action.ViewActions.closeSoftKeyboard()
            )
        // trigger error
        onView(withId(R.id.fab_save))
            .perform(click())
        toggleOrientation()
        // check that text and error message survives, then goto main screen
        onView(withId(R.id.URL))
            .check(matches(withText(stringToBeTyped)))
        onView(withId(R.id.text_input_layout))
            .check { view, _ ->
                assert((view as TextInputLayout).error == getResourceString(R.string.invalid_url))
            }
            .perform(androidx.test.espresso.action.ViewActions.pressBack())
        // check that we back on main screen
        onView(withId(R.id.fab_additem))
            .check { view, _ -> assert(view.isVisible) }
    }

    @Test
    fun testAddGroupAndDeleteGroupRotation() {
        // wait for db to fill up the recycler
        Thread.sleep(1500)
        // add new group
        val exampleGroupName = "example group"
        openActionBarOverflowOrOptionsMenu(
            InstrumentationRegistry.getInstrumentation()
                .targetContext
        ) // click on "..."
        onView(withText(R.string.new_group))
            .perform(click())
        onView(withId(R.id.group_name))
            .perform(typeText(exampleGroupName))
        // rotate dialog and check
        toggleOrientation()
        onView(withId(R.id.group_name))
            .perform(androidx.test.espresso.action.ViewActions.closeSoftKeyboard())
            .check(matches(withText(exampleGroupName)))
            .perform(pressImeActionButton())
        // select example group
        onView(withId(R.id.spinner))
            .perform(click())
        onView(withText(exampleGroupName))
            .perform(click())
        // check selected group after rotation
        toggleOrientation()
        onView(withText(exampleGroupName))
            .check(matches(withText(exampleGroupName)))
        // delete group
        openActionBarOverflowOrOptionsMenu(
            InstrumentationRegistry.getInstrumentation()
                .targetContext
        )
        onView(withText(R.string.delete_group))
            .perform(click())
        toggleOrientation()
        onView(withId(R.id.ok))
            .perform(click())
    }

    @Test
    fun rotateMainScreenAndCheckRecyclerPositionRestoring() {
        // if rotate from portrait to album, item we are looking for might be outside the screen
        setAlbumOrientation()
        // goto detail screen
        onView(withId(R.id.recycler_view_items))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ItemsAdapter.ItemsViewHolder>(
                    10,
                    click()
                )
            )
        // goto edit screen
        onView(withId(R.id.menu_edit))
            .perform(click())
        // fill the inputs in editscreen
        onView(withId(R.id.name))
            .perform(
                replaceText(stringToBeTyped),
                androidx.test.espresso.action.ViewActions.closeSoftKeyboard()
            )
        // save and exit
        onView(withId(R.id.fab_save))
            .perform(click())
        setPortraitOrientation()
        onView(withId(R.id.collapsing_toolbar))
            .perform(androidx.test.espresso.action.ViewActions.pressBack())
        // find for itemName in recycler
        onView(withText(stringToBeTyped))
            .perform(click())
        // goto edit screen
        onView(withId(R.id.menu_edit))
            .perform(click())
        // restore name
        onView(withId(R.id.name))
            .perform(clearText())
        // save and exit
        onView(withId(R.id.fab_save))
            .perform(click())
        onView(withId(R.id.collapsing_toolbar))
            .perform(androidx.test.espresso.action.ViewActions.pressBack())
        // check main screen not crashing during rotation
        toggleOrientation()
    }

    @Test
    fun deleteItemRotation() {
        Thread.sleep(1500)
        // goto details
        onView(withId(R.id.recycler_view_items))
            .perform(
                RecyclerViewActions
                    .actionOnItemAtPosition<ItemsAdapter.ItemsViewHolder>(0, click())
            )
        // goto delete confirmation dialog
        onView(withId(R.id.menu_delete))
            .perform(click())
        // rotate dialog and press cancel in it
        toggleOrientation()
        onView(withId(R.id.cancel))
            .perform(click())
    }

    @Test
    fun editItemRotation() {
        // add group
        val exampleGroupName = "example group"
        openActionBarOverflowOrOptionsMenu(
            InstrumentationRegistry.getInstrumentation().targetContext
        ) // click on "..."
        onView(withText(R.string.new_group))
            .perform(click())
        onView(withId(R.id.group_name))
            .perform(typeText(exampleGroupName), pressImeActionButton())
        // goto details
        Thread.sleep(1500)
        onView(withId(R.id.recycler_view_items))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ItemsAdapter.ItemsViewHolder>(
                    10,
                    click()
                )
            )
        // goto edit screen
        onView(withId(R.id.menu_edit))
            .perform(click())
        // fill the inputs in edit screen
        onView(withId(R.id.name))
            .perform(replaceText(stringToBeTyped))
        onView(withId(R.id.auto_complete_text_view))
            .perform(click())
        onView(withText(exampleGroupName))
            .inRoot(RootMatchers.isPlatformPopup())
            .perform(click())
        // rotate edit screen and check values
        toggleOrientation()
        onView(withId(R.id.name))
            .check(matches(withText(stringToBeTyped)))
        onView(withId(R.id.auto_complete_text_view))
            .check(matches(withText(exampleGroupName)))
            .perform(androidx.test.espresso.action.ViewActions.pressBack())
        // goto main
        onView(withId(R.id.collapsing_toolbar))
            .perform(androidx.test.espresso.action.ViewActions.pressBack())
        // select example group
        onView(withId(R.id.spinner))
            .perform(click())
        onView(withText(exampleGroupName))
            .perform(click())
        // delete group
        openActionBarOverflowOrOptionsMenu(
            InstrumentationRegistry.getInstrumentation()
                .targetContext
        )
        onView(withText(R.string.delete_group))
            .perform(click())
        onView(withId(R.id.ok))
            .perform(click())
    }

    @Test
    fun testFlowAddGroup_master_detail_editItem_deleteGroup() {
        // wait for db to fill up the recycler
        Thread.sleep(1500)
        // add new group
        val exampleGroupName = "example group"
        openActionBarOverflowOrOptionsMenu(
            InstrumentationRegistry.getInstrumentation()
                .targetContext
        ) // click on "..."
        onView(withText(R.string.new_group))
            .perform(click())
        onView(withId(R.id.group_name))
            .perform(typeText(exampleGroupName), pressImeActionButton())
        // goto detail screen
        // wait for db to fill up the recycler
        Thread.sleep(1500)
        onView(withId(R.id.recycler_view_items))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ItemsAdapter.ItemsViewHolder>(
                    10,
                    click()
                )
            )
        // goto edit screen
        onView(withId(R.id.menu_edit))
            .perform(click())
        // fill the inputs in editscreen
        onView(withId(R.id.name))
            .perform(replaceText(stringToBeTyped))
        var existingGroupName = ""
        onView(withId(R.id.auto_complete_text_view))
            .check { view, _ ->
                existingGroupName = (view as AutoCompleteTextView).text.toString()
            } // save existing group name
            .perform(click())
        onView(withText(exampleGroupName))
            .inRoot(RootMatchers.isPlatformPopup())
            .perform(click())
        // save and exit
        onView(withId(R.id.fab_save))
            .perform(click())
        // check the name in toolbar and proceed to main screen
        onView(withId(R.id.collapsing_toolbar))
            .check { view, _ -> assert((view as CollapsingToolbarLayout).title == stringToBeTyped) }
            .perform(androidx.test.espresso.action.ViewActions.pressBack())
        // select example group
        onView(withId(R.id.spinner))
            .perform(click())
        onView(withText(exampleGroupName))
            .perform(click())
        // find for name in recycler. if scrolling position is not restored, should be error
        onView(withText(stringToBeTyped))
            .check(matches(withText(stringToBeTyped)))
            .perform(click())
        // goto edit screen
        onView(withId(R.id.menu_edit))
            .perform(click())
        // restore default values
        onView(withId(R.id.name))
            .perform(clearText())
        onView(withId(R.id.auto_complete_text_view))
            .perform(click())
        onView(withText(existingGroupName))
            .inRoot(RootMatchers.isPlatformPopup())
            .perform(click())
        // save item name and go back to main screen
        onView(withId(R.id.fab_save))
            .perform(click())
        onView(withId(R.id.collapsing_toolbar))
            .perform(androidx.test.espresso.action.ViewActions.pressBack())
        // check that selected group is example group
        onView(withText(exampleGroupName))
            .check(matches(withText(exampleGroupName)))
        // delete group
        openActionBarOverflowOrOptionsMenu(
            InstrumentationRegistry.getInstrumentation()
                .targetContext
        )
        onView(withText(R.string.delete_group))
            .perform(click())
        onView(withId(R.id.ok))
            .perform(click())
        // check that selected group is default
        onView(withText(R.string.all_items))
            .check(matches(withText(R.string.all_items)))
    }

    @Test
    fun sortingMode() {
        Thread.sleep(1500) // wait for db to fill up the recycler
        onView(withId(R.id.menu_sort))
            .perform(click())
        onView(withText(R.string.by_date_desc))
            .perform(click())
        onView(withId(R.id.recycler_view_items))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ItemsAdapter.ItemsViewHolder>(
                    10,
                    click()
                )
            )
        var itemName = ""
        onView(withId(R.id.collapsing_toolbar))
            .check { view, _ -> itemName = (view as CollapsingToolbarLayout).title.toString() }
            .perform(androidx.test.espresso.action.ViewActions.pressBack())
        onView(withId(R.id.recycler_view_items))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ItemsAdapter.ItemsViewHolder>(
                    10,
                    click()
                )
            )
        onView(withId(R.id.collapsing_toolbar))
            .check { view, _ ->
                assert((view as CollapsingToolbarLayout).title.toString() == itemName)
            }
    }

    @Test
    fun searchViewWithRotation() {
        // wait for db to fill up the recycler
        Thread.sleep(1500)
        // goto detail for change the name of the item
        onView(withId(R.id.recycler_view_items))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ItemsAdapter.ItemsViewHolder>(
                    10,
                    click()
                )
            )
        // goto edit screen
        onView(withId(R.id.menu_edit))
            .perform(click())
        // fill the inputs in editscreen
        onView(withId(R.id.name))
            .perform(replaceText(stringToBeTyped))
        // save and exit
        onView(withId(R.id.fab_save))
            .perform(click())
        // goto main
        onView(withId(R.id.collapsing_toolbar))
            .perform(androidx.test.espresso.action.ViewActions.pressBack())
        // open search view
        Thread.sleep(1500)
        onView(withId(R.id.menu_search))
            .perform(click())
        // type query
        onView(isAssignableFrom(AutoCompleteTextView::class.java))
            .perform(typeText(stringToBeTyped))
        // check query after rotation
        toggleOrientation()
        onView(isAssignableFrom(AutoCompleteTextView::class.java))
            .check(matches(withText(stringToBeTyped)))
        // goto detail
        Thread.sleep(1500)
        onView(withId(R.id.recycler_view_items))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ItemsAdapter.ItemsViewHolder>(
                    0,
                    click()
                )
            )
        onView(withId(R.id.collapsing_toolbar))
            .check { view, _ ->
                assert((view as CollapsingToolbarLayout).title.toString() == stringToBeTyped)
            }
        // goto edit screen
        onView(withId(R.id.menu_edit))
            .perform(click())
        // restore default name
        onView(withId(R.id.name))
            .perform(clearText())
        // save and exit
        onView(withId(R.id.fab_save))
            .perform(click())
        onView(withId(R.id.collapsing_toolbar))
            .perform(androidx.test.espresso.action.ViewActions.pressBack())
        // check that query is active after backstack
        onView(isAssignableFrom(AutoCompleteTextView::class.java))
            .check(matches(withText(stringToBeTyped)))
    }

    @Test
    fun swipeToEditWithRotation() {
        // wait for db to fill up the recycler
        Thread.sleep(1500)
        // goto detail for change the name of the item
        onView(withId(R.id.recycler_view_items))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ItemsAdapter.ItemsViewHolder>(
                    0,
                    swipeRight()
                )
            )
        onView(withId(R.id.name))
            .perform(replaceText(stringToBeTyped))
        onView(withId(R.id.fab_save))
            .perform(click())
        toggleOrientation()
        onView(withId(R.id.recycler_view_items))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ItemsAdapter.ItemsViewHolder>(
                    0,
                    swipeRight()
                )
            )
        onView(withId(R.id.name))
            .check(matches(withText(stringToBeTyped)))
            .perform(clearText())
        onView(withId(R.id.fab_save))
            .perform(click())
    }
}

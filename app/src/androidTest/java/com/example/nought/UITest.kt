package com.example.nought

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class UITest {
    @get:Rule
    val activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Test
    fun noteCreateRenameDelete() {
        // Note list is empty
        onView(withId(R.id.empty_note_list_message)).check(matches(isDisplayed()))
        onView(withId(R.id.note_list)).check(matches(not(isDisplayed())))


        // Dialog for note creation is displayed
        onView(withId(R.id.create_note_fab)).perform(click())
        onView(withText("Set note's title:")).check(matches(isDisplayed()))


        // Note list is not empty anymore
        val title = "My note"
        onView(withId(R.id.note_title_edit_field)).perform(typeText(title), closeSoftKeyboard())
        onView(withText("Submit")).perform(click())
        onView(isRoot()).perform(pressBack())
        onView(withId(R.id.empty_note_list_message)).check(matches(not(isDisplayed())))
        onView(withId(R.id.note_list)).check(matches(isDisplayed()))


        // Note title can be changed
        val newTitlePiece = " 2.0"
        onView(withText(title)).perform(longClick())
        onView(withText("Rename")).perform(click())
        onView(withId(R.id.note_title_edit_field)).perform(typeText(newTitlePiece), closeSoftKeyboard())
        onView(withText("Submit")).perform(click())
        onView(withText(title + newTitlePiece)).check(matches(isDisplayed()))


        // Note list is empty again
        onView(withText(title + newTitlePiece)).perform(click())
        onView(isRoot()).perform(pressBack())
        onView(withText(title + newTitlePiece)).perform(longClick())
        onView(withText("Delete")).perform(click())
        onView(withText("Yes")).perform(click())
        onView(withId(R.id.empty_note_list_message)).check(matches(isDisplayed()))
        onView(withId(R.id.note_list)).check(matches(not(isDisplayed())))
    }
}

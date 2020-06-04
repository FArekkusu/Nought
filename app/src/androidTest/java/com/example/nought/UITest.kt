package com.example.nought

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.hamcrest.CoreMatchers.containsString
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


        // Note title is displayed in the entries list
        val title = "My note"
        onView(withId(R.id.note_title_edit_field)).perform(typeText(title), closeSoftKeyboard())
        onView(withText("Submit")).perform(click())
        onView(withId(R.id.entries_list_note_title)).check(matches(withText(containsString(title))))

        // Note list is not empty anymore
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


        // Updated note title is displayed in the entries list
        onView(withText(title + newTitlePiece)).perform(click())
        onView(withId(R.id.entries_list_note_title)).check(matches(withText(containsString(title + newTitlePiece))))

        // Note list is empty again
        onView(isRoot()).perform(pressBack())
        onView(withText(title + newTitlePiece)).perform(longClick())
        onView(withText("Delete")).perform(click())
        onView(withText("Yes")).perform(click())
        onView(withId(R.id.empty_note_list_message)).check(matches(isDisplayed()))
        onView(withId(R.id.note_list)).check(matches(not(isDisplayed())))
    }

    @Test
    fun entryCreateEditDelete() {
        // Create note
        onView(withId(R.id.create_note_fab)).perform(click())
        val noteTitle = "My note"
        onView(withId(R.id.note_title_edit_field)).perform(typeText(noteTitle), closeSoftKeyboard())
        onView(withText("Submit")).perform(click())

        // Entry list is empty
        onView(withId(R.id.empty_entry_list_message)).check(matches(isDisplayed()))
        onView(withId(R.id.entry_list)).check(matches(not(isDisplayed())))


        // Go to textual entry creator
        onView(withId(R.id.go_to_adder_button)).perform(click())
        onView(withId(R.id.entry_text)).check(matches(isDisplayed()))


        // Entry list is not empty anymore
        val entryText = "My entry"
        onView(withId(R.id.entry_text)).perform(typeText(entryText))
        onView(withId(R.id.submit_button)).perform(click())
        onView(withId(R.id.empty_entry_list_message)).check(matches(not(isDisplayed())))
        onView(withId(R.id.entry_list)).check(matches(isDisplayed()))


        // Entry content can be changed
        val newTextPiece = " 2.0"
        onView(withText(entryText)).perform(longClick())
        onView(withText("Edit")).perform(click())
        onView(withId(R.id.entry_text)).perform(typeText(newTextPiece))
        onView(withId(R.id.submit_button)).perform(click())
        onView(withText(entryText + newTextPiece)).check(matches(isDisplayed()))


        // Entry list is empty again
        onView(withText(entryText + newTextPiece)).perform(longClick())
        onView(withText("Delete")).perform(click())
        onView(withText("Yes")).perform(click())
        onView(withId(R.id.empty_entry_list_message)).check(matches(isDisplayed()))
        onView(withId(R.id.entry_list)).check(matches(not(isDisplayed())))


        // Go back and delete note
        onView(isRoot()).perform(pressBack())
        onView(withId(R.id.note_list)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, longClick()))
        onView(withText("Delete")).perform(click())
        onView(withText("Yes")).perform(click())
        onView(withId(R.id.empty_note_list_message)).check(matches(isDisplayed()))
        onView(withId(R.id.note_list)).check(matches(not(isDisplayed())))
    }
}

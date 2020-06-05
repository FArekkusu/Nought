package com.example.nought

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.util.Checks
import androidx.test.rule.ActivityTestRule
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
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


        // Title changing can be cancelled
        val newTitlePiece = " 2.0"
        onView(withText(title)).perform(longClick())
        onView(withText("Rename")).perform(click())
        onView(withId(R.id.note_title_edit_field)).perform(typeText(newTitlePiece), closeSoftKeyboard())
        onView(withText("Cancel")).perform(click())
        onView(withText(title)).check(matches(isDisplayed()))
        onView(withId(R.id.note_list)).check(matches(not(hasDescendant(withText(title + newTitlePiece)))))


        // Note title can be changed
        onView(withText(title)).perform(longClick())
        onView(withText("Rename")).perform(click())
        onView(withId(R.id.note_title_edit_field)).perform(typeText(newTitlePiece), closeSoftKeyboard())
        onView(withText("Submit")).perform(click())
        onView(withText(title + newTitlePiece)).check(matches(isDisplayed()))


        // Updated note title is displayed in the entries list
        onView(withText(title + newTitlePiece)).perform(click())
        onView(withId(R.id.entries_list_note_title)).check(matches(withText(containsString(title + newTitlePiece))))


        // Note deletion can be cancelled
        onView(isRoot()).perform(pressBack())
        onView(withText(title + newTitlePiece)).perform(longClick())
        onView(withText("Delete")).perform(click())
        onView(withText("No")).perform(click())
        onView(withId(R.id.empty_note_list_message)).check(matches(not(isDisplayed())))
        onView(withId(R.id.note_list)).check(matches(isDisplayed()))


        // Note list is empty again
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

    @Test
    fun entryMarkUnmark() {
        // Create note
        onView(withId(R.id.create_note_fab)).perform(click())
        val noteTitle = "My note"
        onView(withId(R.id.note_title_edit_field)).perform(typeText(noteTitle), closeSoftKeyboard())
        onView(withText("Submit")).perform(click())


        // Check 0 marked, 0 total
        onView(isRoot()).perform(pressBack())
        onView(withId(R.id.note_list)).check(matches(hasDescendant(withText(containsString("0/0 entries marked as completed")))))
        onView(withId(R.id.note_list)).check(matches(hasDescendant(withBackgroundColor(R.color.white))))


        // Add first entry
        onView(withText(noteTitle)).perform(click())
        onView(withId(R.id.go_to_adder_button)).perform(click())
        val firstEntryText = "Entry 1"
        onView(withId(R.id.entry_text)).perform(typeText(firstEntryText))
        onView(withId(R.id.submit_button)).perform(click())
        onView(withText(firstEntryText)).check(matches(hasTextColor(R.color.black)))


        // Check 0 marked, 1 total
        onView(isRoot()).perform(pressBack())
        onView(withId(R.id.note_list)).check(matches(hasDescendant(withText(containsString("0/1 entries marked as completed")))))
        onView(withId(R.id.note_list)).check(matches(hasDescendant(withBackgroundColor(R.color.white))))


        // Add second entry
        onView(withId(R.id.note_list)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        onView(withId(R.id.go_to_adder_button)).perform(click())
        val secondEntryText = "Entry 2"
        onView(withId(R.id.entry_text)).perform(typeText(secondEntryText))
        onView(withId(R.id.submit_button)).perform(click())
        onView(withText(secondEntryText)).check(matches(hasTextColor(R.color.black)))


        // Check 0 marked, 2 total
        onView(isRoot()).perform(pressBack())
        onView(withId(R.id.note_list)).check(matches(hasDescendant(withText(containsString("0/2 entries marked as completed")))))
        onView(withId(R.id.note_list)).check(matches(hasDescendant(withBackgroundColor(R.color.white))))


        // Mark first entry
        onView(withId(R.id.note_list)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        onView(withText(firstEntryText)).perform(swipeRight())
        onView(withText(firstEntryText)).check(matches(hasTextColor(R.color.gray)))


        // Check 1 marked, 2 total
        onView(isRoot()).perform(pressBack())
        onView(withId(R.id.note_list)).check(matches(hasDescendant(withText(containsString("1/2 entry marked as completed")))))
        onView(withId(R.id.note_list)).check(matches(hasDescendant(withBackgroundColor(R.color.white))))


        // Mark second entry
        onView(withId(R.id.note_list)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        onView(withText(secondEntryText)).perform(swipeRight())
        onView(withText(secondEntryText)).check(matches(hasTextColor(R.color.gray)))


        // Check 2 marked, 2 total
        onView(isRoot()).perform(pressBack())
        onView(withId(R.id.note_list)).check(matches(hasDescendant(withText(containsString("2/2 entries marked as completed")))))
        onView(withId(R.id.note_list)).check(matches(hasDescendant(withBackgroundColor(R.color.green))))


        // Delete note
        onView(withId(R.id.note_list)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, longClick()))
        onView(withText("Delete")).perform(click())
        onView(withText("Yes")).perform(click())
        onView(withId(R.id.empty_note_list_message)).check(matches(isDisplayed()))
        onView(withId(R.id.note_list)).check(matches(not(isDisplayed())))
    }

    private fun withBackgroundColor(colorRes: Int): Matcher<View> {
        Checks.checkNotNull(colorRes)
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description?) {
                description?.appendText("background color: ${colorRes}")
            }

            override fun matchesSafely(item: View?): Boolean {
                val context = item?.context
                val expectedColor = if (Build.VERSION.SDK_INT <= 22) context?.resources?.getColor(colorRes) else context?.getColor(colorRes)
                return (item?.background as ColorDrawable).color == expectedColor
            }
        }
    }
}

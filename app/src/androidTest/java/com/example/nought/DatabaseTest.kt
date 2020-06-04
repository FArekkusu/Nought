package com.example.nought

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.nought.database.Entry
import com.example.nought.database.EntryDatabase
import com.example.nought.database.EntryDatabaseDao
import com.example.nought.database.Note
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var dao: EntryDatabaseDao
    private lateinit var db: EntryDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EntryDatabase::class.java).build()
        dao = db.entryDatabaseDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun noteLifecycle() {
        // No notes exist
        var allNotes = dao.getAllNotesSync()
        assertEquals(0, allNotes.size)


        // Only 1 note exists
        val title = "My note"
        dao.insert(Note(title = title))
        allNotes = dao.getAllNotesSync()
        assertEquals(1, allNotes.size)


        // Correct title and totals
        val note = allNotes[0].note
        assertEquals(title, note.title)
        assertEquals(0, allNotes[0].markedEntries)
        assertEquals(0, allNotes[0].totalEntries)


        // Note count didn't change
        val newTitle = "My note 2.0"
        dao.updateTitle(note.id, newTitle)
        allNotes = dao.getAllNotesSync()
        assertEquals(1, allNotes.size)
        assertEquals(newTitle, allNotes[0].note.title)
        assertEquals(0, allNotes[0].markedEntries)
        assertEquals(0, allNotes[0].totalEntries)


        // Note was deleted
        dao.deleteNote(note.id)
        allNotes = dao.getAllNotesSync()
        assertEquals(0, allNotes.size)
    }

    @Test
    @Throws(IOException::class)
    fun entryLifecycle() {
        // No entries exist yet
        dao.insert(Note(title = ""))
        val note = dao.getAllNotesSync()[0].note
        var allEntries = dao.getAllEntriesSync(note.id)
        assertEquals(0, allEntries.size)


        // 2 entries were added
        val emptyString = ""
        dao.insert(Entry(content = emptyString, noteId = note.id, markable = true, marked = false))
        dao.insert(Entry(content = emptyString, noteId = note.id, markable = false, marked = false))
        allEntries = dao.getAllEntriesSync(note.id)
        assertEquals(2, allEntries.size)


        // Second entry was removed, first remains
        dao.deleteEntry(allEntries[1].id)
        val remainingEntries = dao.getAllEntriesSync(note.id)
        assertEquals(1, remainingEntries.size)
        assertEquals(allEntries[0], remainingEntries[0])


        // Fetching entry by id works correctly
        var entry = dao.getEntrySync(allEntries[0].id)
        assertEquals(entry, allEntries[0])


        // Entry was marked
        dao.mark(entry.id)
        assertEquals(true, dao.getEntrySync(entry.id).marked)


        // Content was updated, and entry was unmarked
        val newText = "Not empty string"
        dao.updateText(entry.id, newText)
        entry = dao.getEntrySync(entry.id)
        assertEquals(newText, entry.content)
        assertEquals(false, entry.marked)


        // Repeated marking switches between marked and unmarked state
        dao.mark(entry.id)
        assertEquals(true, dao.getEntrySync(entry.id).marked)
        dao.mark(entry.id)
        assertEquals(false, dao.getEntrySync(entry.id).marked)


        // Deleting a note deletes its entries too
        dao.deleteNote(note.id)
        assertEquals(0, dao.getAllNotesSync().size)
        assertEquals(0, dao.getAllEntriesForAllNotesSync().size)
    }

    @Test
    @Throws(IOException::class)
    fun notesAndEntriesLifecycle() {
        // No notes exist
        var allNotes = dao.getAllNotesSync()
        assertEquals(0, allNotes.size)


        // Correct initial totals
        dao.insert(Note(title = ""))
        dao.insert(Note(title = ""))
        allNotes = dao.getAllNotesSync()
        assertEquals(2, allNotes.size)
        assertEquals(0, allNotes[0].markedEntries)
        assertEquals(0, allNotes[0].totalEntries)
        assertEquals(0, allNotes[1].markedEntries)
        assertEquals(0, allNotes[1].totalEntries)


        // Insert entries for note #1
        val emptyString = ""
        dao.insert(Entry(content = emptyString, noteId = allNotes[0].note.id, markable = true, marked = true))

        // Insert entries for note #2
        dao.insert(Entry(content = emptyString, noteId = allNotes[1].note.id, markable = false, marked = false))
        dao.insert(Entry(content = emptyString, noteId = allNotes[1].note.id, markable = true, marked = false))
        dao.insert(Entry(content = emptyString, noteId = allNotes[1].note.id, markable = true, marked = true))


        // Note #1 has 1 entry
        var allEntries = dao.getAllEntriesSync(allNotes[0].note.id)
        assertEquals(1, allEntries.size)


        // Note #2 still has 3 entries
        allEntries = dao.getAllEntriesSync(allNotes[1].note.id)
        assertEquals(3, allEntries.size)


        // Both notes exist, and their marked/total counts are up to date
        allNotes = dao.getAllNotesSync()
        assertEquals(2, allNotes.size)
        assertEquals(1, allNotes[0].markedEntries)
        assertEquals(1, allNotes[0].totalEntries)
        assertEquals(1, allNotes[1].markedEntries)
        assertEquals(2, allNotes[1].totalEntries)


        // Entry count didn't change, only second entry of second note has updated text
        val newText = "I was empty before"
        dao.updateText(allEntries[1].id, newText)

        allEntries = dao.getAllEntriesSync(allNotes[0].note.id)
        assertEquals(1, allEntries.size)
        assertEquals(emptyString, allEntries[0].content)

        allEntries = dao.getAllEntriesSync(allNotes[1].note.id)
        assertEquals(3, allEntries.size)
        assertEquals(emptyString, allEntries[0].content)
        assertEquals(newText, allEntries[1].content)
        assertEquals(emptyString, allEntries[2].content)


        // Second note's marked count is up to date, first note's marked count didn't change
        dao.mark(allEntries[1].id)
        allNotes = dao.getAllNotesSync()
        assertEquals(2, allNotes.size)
        assertEquals(1, allNotes[0].markedEntries)
        assertEquals(1, allNotes[0].totalEntries)
        assertEquals(2, allNotes[1].markedEntries)
        assertEquals(2, allNotes[1].totalEntries)


        // All entries of note #2 unmarked, note #1 is untouched
        dao.unmarkAll(allNotes[1].note.id)
        allNotes = dao.getAllNotesSync()
        assertEquals(2, allNotes.size)
        assertEquals(1, allNotes[0].markedEntries)
        assertEquals(1, allNotes[0].totalEntries)
        assertEquals(0, allNotes[1].markedEntries)
        assertEquals(2, allNotes[1].totalEntries)


        // No notes exist anymore
        dao.deleteNote(allNotes[0].note.id)
        dao.deleteNote(allNotes[1].note.id)
        assertEquals(0, dao.getAllNotesSync().size)


        // No entries exist anymore
        allEntries = dao.getAllEntriesForAllNotesSync()
        assertEquals(0, allEntries.size)
    }
}

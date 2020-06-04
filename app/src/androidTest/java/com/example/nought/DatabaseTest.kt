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
        var note = allNotes[0].note
        assertEquals(title, note.title)
        assertEquals(0, allNotes[0].markedEntries)
        assertEquals(0, allNotes[0].totalEntries)


        // Note count didn't change
        val newTitle = "My note 2.0"
        dao.updateTitle(note.id, newTitle)
        allNotes = dao.getAllNotesSync()
        assertEquals(1, allNotes.size)
        assertEquals(0, allNotes[0].markedEntries)
        assertEquals(0, allNotes[0].totalEntries)


        // Note title was updated
        note = allNotes[0].note
        assertEquals(newTitle, note.title)


        // Note was deleted
        dao.deleteNote(note.id)
        allNotes = dao.getAllNotesSync()
        assertEquals(0, allNotes.size)
    }

    @Test
    @Throws(IOException::class)
    fun entriesLifecycle() {
        // No notes exist
        var allNotes = dao.getAllNotesSync()
        assertEquals(0, allNotes.size)


        // Correct initial totals
        dao.insert(Note(title = "Note 1"))
        dao.insert(Note(title = "Note 2"))
        allNotes = dao.getAllNotesSync()
        assertEquals(2, allNotes.size)
        assertEquals(0, allNotes[0].markedEntries)
        assertEquals(0, allNotes[0].totalEntries)
        assertEquals(0, allNotes[1].markedEntries)
        assertEquals(0, allNotes[1].totalEntries)


        // Insert entries for note #1
        dao.insert(Entry(content = "", noteId = allNotes[0].note.id, markable = true, marked = false))
        dao.insert(Entry(content = "", noteId = allNotes[0].note.id, markable = true, marked = true))

        // Insert entries for note #2
        dao.insert(Entry(content = "", noteId = allNotes[1].note.id, markable = false, marked = false))
        dao.insert(Entry(content = "", noteId = allNotes[1].note.id, markable = true, marked = false))
        dao.insert(Entry(content = "", noteId = allNotes[1].note.id, markable = true, marked = true))


        // Note #1 has 2 entries
        var allEntries = dao.getAllEntriesSync(allNotes[0].note.id)
        assertEquals(2, allEntries.size)


        // second entry was deleted from note #1, and the first one remains
        dao.deleteEntry(allEntries[1].id)
        val remainingEntries = dao.getAllEntriesSync(allNotes[0].note.id)
        assertEquals(1, remainingEntries.size)
        assertEquals(allEntries[0].id, remainingEntries[0].id)


        // Both notes exist, and their marked/total counts are up to date
        allNotes = dao.getAllNotesSync()
        assertEquals(2, allNotes.size)
        assertEquals(0, allNotes[0].markedEntries)
        assertEquals(1, allNotes[0].totalEntries)
        assertEquals(1, allNotes[1].markedEntries)
        assertEquals(2, allNotes[1].totalEntries)


        // Note #2 still has 3 entries
        allEntries = dao.getAllEntriesSync(allNotes[1].note.id)
        assertEquals(3, allEntries.size)


        // Entry count didn't change, second entry has updated text
        var newText = "I was empty before"
        dao.updateText(allEntries[1].id, newText)
        allEntries = dao.getAllEntriesSync(allNotes[1].note.id)
        assertEquals(3, allEntries.size)
        assertEquals(newText, allEntries[1].content)


        // Second note's marked count is up to date, first note's marked count didn't change
        dao.mark(allEntries[1].id)
        allNotes = dao.getAllNotesSync()
        assertEquals(2, allNotes.size)
        assertEquals(0, allNotes[0].markedEntries)
        assertEquals(1, allNotes[0].totalEntries)
        assertEquals(2, allNotes[1].markedEntries)
        assertEquals(2, allNotes[1].totalEntries)


        // Updating marked entry unmarked it
        newText = "I was not empty just now"
        dao.updateText(allEntries[1].id, newText)
        assertEquals(false, dao.getAllEntriesSync(allNotes[1].note.id)[1].marked)


        // Marking an entry twice marks and unmarks it
        dao.mark(allEntries[1].id)
        assertEquals(true, dao.getAllEntriesSync(allNotes[1].note.id)[1].marked)
        dao.mark(allEntries[1].id)
        assertEquals(false, dao.getAllEntriesSync(allNotes[1].note.id)[1].marked)


        // Entry is fetched correctly by id
        val entry22 = dao.getEntrySync(allEntries[1].id)
        assertEquals(entry22.id, allEntries[1].id)
        assertEquals(entry22.content, newText)


        // All entries of note #2 unmarked, note #1 is untouched
        dao.unmarkAll(allNotes[1].note.id)
        allNotes = dao.getAllNotesSync()
        assertEquals(2, allNotes.size)
        assertEquals(0, allNotes[0].markedEntries)
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

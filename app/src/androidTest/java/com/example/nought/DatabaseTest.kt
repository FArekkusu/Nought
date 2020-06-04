package com.example.nought

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
    fun createNote() {
        dao.insert(Note(title = "My note"))
        assertEquals("My note", dao.getNoteSync(1).title)
    }
}

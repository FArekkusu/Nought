package com.example.nought.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EntryDatabaseDao {
    @Insert
    fun insert(entry: Entry)

    @Query("UPDATE entries SET content = :content, marked = 0 WHERE id = :id")
    fun updateText(id: Long, content: String)

    @Query("UPDATE notes SET title = :title WHERE id = :id")
    fun updateTitle(id: Long, title: String)

    @Query("DELETE FROM entries WHERE id = :id")
    fun deleteEntry(id: Long)

    @Query("DELETE FROM notes WHERE id = :id")
    fun deleteNote(id: Long)

    @Query("UPDATE entries SET marked = NOT marked WHERE id = :id")
    fun mark(id: Long)

    @Query("UPDATE entries SET marked = 0 WHERE marked AND note_id = :id")
    fun unmarkAll(id: Long)

    @Query("SELECT * FROM entries WHERE id = :id")
    fun getEntry(id: Long): LiveData<Entry>

    @Query("SELECT * FROM entries WHERE id = :id")
    fun getEntrySync(id: Long): Entry

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNote(id: Long): LiveData<Note>

    @Query("SELECT * FROM entries WHERE note_id = :noteId ORDER BY id")
    fun getAllEntries(noteId: Long): LiveData<List<Entry>>

    @Query("SELECT * FROM entries WHERE note_id = :noteId ORDER BY id")
    fun getAllEntriesSync(noteId: Long): List<Entry>

    @Insert
    fun insert(note: Note): Long

    @Query("""
        SELECT
          n.*,
          SUM(CASE WHEN e.marked THEN 1 ELSE 0 END) AS marked_entries,
          SUM(CASE WHEN e.markable THEN 1 ELSE 0 END) AS total_entries
        FROM
          notes AS n
          LEFT JOIN entries AS e ON n.id = e.note_id
        GROUP BY
          n.id, n.title
    """)
    fun getAllNotes(): LiveData<List<NoteWithTotals>>
}

data class NoteWithTotals(
    @Embedded
    val note: Note,

    @ColumnInfo(name = "marked_entries")
    val markedEntries: Int,

    @ColumnInfo(name = "total_entries")
    val totalEntries: Int
)
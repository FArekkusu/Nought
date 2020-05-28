package com.example.nought.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "entries",
    foreignKeys = arrayOf(ForeignKey(
        entity = Note::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("note_id"),
        onDelete = ForeignKey.CASCADE
    ))
)
data class Entry(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "note_id")
    var noteId: Long = 0L,

    @ColumnInfo(name = "content")
    var content: String = "",

    @ColumnInfo(name = "markable")
    var markable: Boolean = true,

    @ColumnInfo(name = "marked")
    var marked: Boolean = false
)
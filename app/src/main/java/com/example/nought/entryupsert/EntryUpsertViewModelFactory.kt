package com.example.nought.entryupsert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.nought.database.EntryDatabaseDao

class EntryUpsertViewModelFactory(
    private val noteId: Long,
    private val entryId: Long,
    private val dataSource: EntryDatabaseDao
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EntryUpsertViewModel::class.java)) {
            return EntryUpsertViewModel(noteId, entryId, dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
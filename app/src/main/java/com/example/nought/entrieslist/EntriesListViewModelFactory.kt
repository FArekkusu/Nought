package com.example.nought.entrieslist

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.nought.database.EntryDatabaseDao

class EntriesListViewModelFactory(
    private val noteId: Long,
    private val dataSource: EntryDatabaseDao,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EntriesListViewModel::class.java)) {
            return EntriesListViewModel(noteId, dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
package com.example.nought.entrieslist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.nought.database.Entry
import com.example.nought.database.EntryDatabaseDao
import com.example.nought.database.Note
import kotlinx.coroutines.*

class EntriesListViewModel(
    val noteId: Long,
    val database: EntryDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val entries = database.getAllEntries(noteId)

    private val note = MediatorLiveData<Note>()

    fun getNote() = note

    private val _navigateToEntryUpsert = MutableLiveData<Long>()

    val navigateToEntryUpsert: LiveData<Long>
        get() = _navigateToEntryUpsert

    init {
        note.addSource(database.getNote(noteId), note::setValue)
    }

    fun onEnter(id: Long) {
        uiScope.launch {
            _navigateToEntryUpsert.value = id
        }
    }

    fun doneNavigating() {
        _navigateToEntryUpsert.value = null
    }

    fun onUpsertImage(id: Long, imageBase64: String) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                if (id == 0L)
                    database.insert(Entry(content = imageBase64, noteId = noteId, markable = false))
                else
                    database.updateText(id, imageBase64)
            }
        }
    }

    fun onMark(id: Long) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database.mark(id)
            }
        }
    }

    fun onUnmarkAll() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database.unmarkAll(noteId)
            }
        }
    }

    fun onDelete(id: Long) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                 database.deleteEntry(id)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
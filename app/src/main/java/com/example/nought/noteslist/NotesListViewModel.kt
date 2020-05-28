package com.example.nought.noteslist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.nought.database.EntryDatabaseDao
import com.example.nought.database.Note
import kotlinx.coroutines.*

class NotesListViewModel(
    val database: EntryDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val notes = database.getAllNotes()

    private val _navigateToEntriesList = MutableLiveData<Long>()

    val navigateToEntriesList: LiveData<Long>
        get() = _navigateToEntriesList

    fun onEnter(id: Long) {
        uiScope.launch {
            _navigateToEntriesList.value = id
        }
    }

    fun doneNavigating() {
        _navigateToEntriesList.value = null
    }

    fun onCreate(id: Long, title: String) {
        var navigateTo: Long? = null
        if (title.isNotEmpty())
            uiScope.launch {
                withContext(Dispatchers.IO) {
                    if (id == 0L)
                        navigateTo = database.insert(Note(title = title))
                    else
                        database.updateTitle(id, title)
                }

                _navigateToEntriesList.value = navigateTo
            }
    }

    fun onDelete(id: Long) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database.deleteNote(id)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
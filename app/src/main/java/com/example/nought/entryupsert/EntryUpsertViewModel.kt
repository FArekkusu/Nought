package com.example.nought.entryupsert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nought.database.Entry
import com.example.nought.database.EntryDatabaseDao
import kotlinx.coroutines.*

class EntryUpsertViewModel(
    val noteId: Long,
    val entryId: Long,
    val database: EntryDatabaseDao) : ViewModel() {

    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val entry = MediatorLiveData<Entry>()

    fun getEntry() = entry

    init {
        entry.addSource(database.getEntry(entryId), entry::setValue)
    }

    private val _navigateToEntriesList = MutableLiveData<Long>()

    val navigateToEntriesList: LiveData<Long>
        get() = _navigateToEntriesList

    fun doneNavigating() {
        _navigateToEntriesList.value = null
    }

    fun onReturn() {
        _navigateToEntriesList.value = entryId
    }

    fun onUpsert(content: String) {
        uiScope.launch {
            if (content.isNotEmpty())
                withContext(Dispatchers.IO) {
                    if (entryId == 0L)
                        database.insert(Entry(content = content, noteId = noteId))
                    else if (database.getEntrySync(entryId).content != content)
                        database.updateText(entryId, content)
                }

            _navigateToEntriesList.value = noteId
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
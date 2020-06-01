package com.example.nought.noteslist

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nought.R
import com.example.nought.database.EntryDatabase
import com.example.nought.databinding.FragmentNotesListBinding

class NotesListFragment : Fragment() {
    lateinit var binding: FragmentNotesListBinding
    lateinit var notesListViewModel: NotesListViewModel
    lateinit var adapter: NoteAdapter
    var renamedNoteId: Long = 0L
    var renamedNoteTitle: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_notes_list, container, false)

        val application = requireNotNull(this.activity).application

        val dataSource = EntryDatabase.getInstance(application).entryDatabaseDao

        val viewModelFactory = NotesListViewModelFactory(dataSource, application)

        notesListViewModel =
            ViewModelProviders.of(
                this, viewModelFactory).get(NotesListViewModel::class.java)

        binding.notesListViewModel = notesListViewModel

        binding.setLifecycleOwner(this)

        adapter = NoteAdapter(NoteListener {
            notesListViewModel.onEnter(it)
        })
        binding.noteList.adapter = adapter

        val manager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.noteList.layoutManager = manager

        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        binding.noteList.addItemDecoration(itemDecoration)

        registerForContextMenu(binding.noteList)

        notesListViewModel.notes.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.emptyNoteListMessage.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                binding.noteList.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
                adapter.submitNoteList(it)
            }
        })

        notesListViewModel.navigateToEntriesList.observe(viewLifecycleOwner, Observer {
            it?.let {
                this.findNavController().navigate(
                    NotesListFragmentDirections.actionNotesListFragmentToEntriesListFragment(it))
                notesListViewModel.doneNavigating()
            }
        })

        binding.createNoteFab.setOnClickListener {
            val editText = EditText(requireContext())
            editText.setText(renamedNoteTitle)
            renamedNoteTitle = ""
            AlertDialog.Builder(requireContext())
                .setTitle("Set note's title:")
                .setView(editText)
                .setPositiveButton("Submit") {_, _ ->
                    notesListViewModel.onCreate(renamedNoteId, editText.text.toString())
                    renamedNoteId = 0L
                }
                .setNegativeButton("Cancel") {_, _ ->
                }
                .show()
        }

        return binding.root
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val position = adapter.position
        val itemId = item.itemId
        val noteId = adapter.getId(position)
        val noteTitle = adapter.getTitle(position)
        if (itemId == 1) {
            renamedNoteId = noteId
            renamedNoteTitle = noteTitle
            binding.createNoteFab.callOnClick()
        } else
            AlertDialog.Builder(requireContext())
                .setMessage("Confirm deletion?")
                .setPositiveButton("Yes") { _, _ -> notesListViewModel.onDelete(noteId) }
                .setNegativeButton("No") { _, _ -> }
                .create()
                .show()
        return super.onContextItemSelected(item)
    }
}
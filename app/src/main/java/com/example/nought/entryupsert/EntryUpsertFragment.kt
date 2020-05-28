package com.example.nought.entryupsert

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.nought.R
import com.example.nought.database.EntryDatabase
import com.example.nought.databinding.FragmentEntryUpsertBinding

class EntryUpsertFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val binding: FragmentEntryUpsertBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_entry_upsert, container, false)

        val application = requireNotNull(this.activity).application

        val arguments = EntryUpsertFragmentArgs.fromBundle(requireArguments())

        val dataSource = EntryDatabase.getInstance(application).entryDatabaseDao

        val viewModelFactory = EntryUpsertViewModelFactory(arguments.noteId, arguments.entryId, dataSource)

        val entryUpsertViewModel =
            ViewModelProviders.of(
                this, viewModelFactory).get(EntryUpsertViewModel::class.java)

        binding.entryUpsertViewModel = entryUpsertViewModel

        binding.setLifecycleOwner(this)

        entryUpsertViewModel.navigateToEntriesList.observe(viewLifecycleOwner, Observer {
            it?.let {
                this.findNavController().navigate(
                    EntryUpsertFragmentDirections.actionEntryUpsertFragmentToEntriesListFragment(arguments.noteId))
                entryUpsertViewModel.doneNavigating()
            }
        })

        binding.cancelButton.setOnClickListener {
            val activity = requireActivity()
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            entryUpsertViewModel.onReturn()
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }

        binding.submitButton.setOnClickListener {
            val activity = requireActivity()
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            entryUpsertViewModel.onUpsert(binding.entryText.text.toString())
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }

        return binding.root
    }
}
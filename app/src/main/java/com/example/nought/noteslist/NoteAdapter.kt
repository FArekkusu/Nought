package com.example.nought.noteslist

import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nought.database.Note
import com.example.nought.database.NoteWithTotals
import com.example.nought.databinding.NoteBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteAdapter(val clickListener: NoteListener) : ListAdapter<DataItem,
        RecyclerView.ViewHolder>(NoteDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    var position: Int = -1

    fun submitNoteList(list: List<NoteWithTotals>?) {
        adapterScope.launch {
            val items = when (list) {
                null -> emptyList()
                else -> list.map { DataItem.NoteItem(it) }
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    fun getId(position: Int): Long {
        return getItem(position).id
    }

    fun getTitle(position: Int): String {
        return getItem(position).title
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val noteItem = getItem(position) as DataItem.NoteItem
                val noteWithTotals = noteItem.noteWithTotals
                holder.bind(noteWithTotals.note, noteWithTotals.markedEntries, noteWithTotals.totalEntries)
                holder.itemView.setOnClickListener {
                    clickListener.onClick(noteWithTotals.note)
                }
                holder.itemView.setOnLongClickListener {
                    this.position = holder.position
                    return@setOnLongClickListener false
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: NoteBinding)
        : RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {

        fun bind(item: Note, marked: Int, total: Int) {
            binding.note = item
            val word = if (marked == 1) "entry" else "entries"
            binding.markedVsCompleted = "${marked}/${total} ${word} marked as completed"
            binding.allMarked = total > 0 && marked == total
            binding.executePendingBindings()
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu!!.add(Menu.NONE, 1, 1, "Rename")
            menu.add(Menu.NONE, 2, 2, "Delete")
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = NoteBinding.inflate(layoutInflater, parent, false)

                val viewHolder = ViewHolder(binding)
                parent.setOnCreateContextMenuListener(viewHolder)

                return viewHolder
            }
        }
    }
}

class NoteDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}

class NoteListener(val clickListener: (Long) -> Unit) {
    fun onClick(note: Note) = clickListener(note.id)
}

sealed class DataItem {
    data class NoteItem(val noteWithTotals: NoteWithTotals): DataItem() {
        override val id = noteWithTotals.note.id
        override val title = noteWithTotals.note.title
    }

    abstract val id: Long
    abstract val title: String
}
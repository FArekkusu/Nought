package com.example.nought.entrieslist

import android.graphics.BitmapFactory
import android.graphics.Paint
import android.util.Base64
import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nought.database.Entry
import com.example.nought.databinding.ImageEntryBinding
import com.example.nought.databinding.TextEntryBinding
import kotlinx.android.synthetic.main.text_entry.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EntryAdapter : ListAdapter<DataItem,
        RecyclerView.ViewHolder>(EntryDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    var position: Int = -1

    fun submitEntryList(list: List<Entry>?) {
        adapterScope.launch {
            val items = when (list) {
                null -> emptyList()
                else -> list.map { if (it.markable) DataItem.TextEntryItem(it) else DataItem.ImageEntryItem(it) }
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    fun getId(position: Int): Long {
        return getItem(position).id
    }

    fun getMarkable(position: Int): Boolean {
        return getItem(position).markable
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TextViewHolder -> {
                val textEntryItem = getItem(position) as DataItem.TextEntryItem
                holder.bind(textEntryItem.entry)
                holder.itemView.setOnLongClickListener {
                    this.position = holder.position
                    return@setOnLongClickListener false
                }
                holder.itemView.content.apply {
                    if (textEntryItem.entry.marked)
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    else
                        paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }
            is ImageViewHolder -> {
                val imageEntryItem = getItem(position) as DataItem.ImageEntryItem
                holder.bind(imageEntryItem.entry)
                holder.itemView.setOnLongClickListener {
                    this.position = holder.position
                    return@setOnLongClickListener false
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> TextViewHolder.from(parent)
            1 -> ImageViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).markable) {
            true -> 0
            else -> 1
        }
    }

    class ImageViewHolder private constructor(val binding: ImageEntryBinding)
        : RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {

        fun bind(item: Entry) {
            val byteArray = Base64.decode(item.content, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            binding.imageContent.setImageBitmap(bitmap)
            binding.executePendingBindings()
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu!!.add(Menu.NONE, 1, 1, "Edit")
            menu.add(Menu.NONE, 2, 2, "Delete")
        }

        companion object {
            fun from(parent: ViewGroup): ImageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ImageEntryBinding.inflate(layoutInflater, parent, false)

                val viewHolder = ImageViewHolder(binding)
                parent.setOnCreateContextMenuListener(viewHolder)

                return viewHolder
            }
        }
    }

    class TextViewHolder private constructor(val binding: TextEntryBinding)
        : RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {

        fun bind(item: Entry) {
            binding.entry = item
            binding.executePendingBindings()
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu!!.add(Menu.NONE, 1, 1, "Edit")
            menu.add(Menu.NONE, 2, 2, "Delete")
        }

        companion object {
            fun from(parent: ViewGroup): TextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TextEntryBinding.inflate(layoutInflater, parent, false)

                val viewHolder = TextViewHolder(binding)
                parent.setOnCreateContextMenuListener(viewHolder)

                return viewHolder
            }
        }
    }
}

class EntryDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}

sealed class DataItem {
    data class TextEntryItem(val entry: Entry): DataItem() {
        override val id = entry.id
        override val markable = true
    }

    data class ImageEntryItem(val entry: Entry): DataItem() {
        override val id = entry.id
        override val markable = false
    }

    abstract val id: Long
    abstract val markable: Boolean
}
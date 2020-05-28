package com.example.nought.entrieslist

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nought.R
import com.example.nought.database.EntryDatabase
import com.example.nought.databinding.FragmentEntriesListBinding
import java.io.ByteArrayOutputStream
import kotlin.math.min
import kotlin.math.round

class EntriesListFragment : Fragment() {
    lateinit var binding: FragmentEntriesListBinding
    lateinit var entriesListViewModel: EntriesListViewModel
    lateinit var adapter: EntryAdapter
    var replacedImageEntryId: Long = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_entries_list, container, false)

        val application = requireNotNull(this.activity).application

        val arguments = EntriesListFragmentArgs.fromBundle(requireArguments())

        val dataSource = EntryDatabase.getInstance(application).entryDatabaseDao

        val viewModelFactory = EntriesListViewModelFactory(arguments.noteId, dataSource, application)

        entriesListViewModel =
            ViewModelProviders.of(
                this, viewModelFactory).get(EntriesListViewModel::class.java)

        binding.entriesListViewModel = entriesListViewModel

        binding.setLifecycleOwner(this)

        adapter = EntryAdapter()
        binding.entryList.adapter = adapter

        val touchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (viewHolder is EntryAdapter.ImageViewHolder)
                    return
                val position = viewHolder.adapterPosition
                val id = adapter.getId(position)
                entriesListViewModel.onMark(id)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val newDX = if (viewHolder is EntryAdapter.TextViewHolder) dX / 10 else 0f
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    newDX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }

        val touchHelper = ItemTouchHelper(touchCallback)
        touchHelper.attachToRecyclerView(binding.entryList)

        val manager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.entryList.layoutManager = manager

        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        binding.entryList.addItemDecoration(itemDecoration)

        registerForContextMenu(binding.entryList)

        binding.entryList.itemAnimator!!.changeDuration = 0

        entriesListViewModel.entries.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.emptyEntryListMessage.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                binding.entryList.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
                binding.unmarkAllButton.visibility = if (it.any { e -> e.marked }) View.VISIBLE else View.GONE
                adapter.submitEntryList(it)
            }
        })

        entriesListViewModel.navigateToEntryUpsert.observe(viewLifecycleOwner, Observer {
            it?.let {
                this.findNavController().navigate(
                    EntriesListFragmentDirections.actionEntriesListFragmentToEntryUpsertFragment(arguments.noteId, it))
                entriesListViewModel.doneNavigating()
            }
        })

        binding.addImageButton.setOnClickListener {
            val intent = Intent(if (Build.VERSION.SDK_INT > 18) Intent.ACTION_OPEN_DOCUMENT else Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        binding.unmarkAllButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage("Unmark all entries?")
                .setPositiveButton("Yes") { _, _ -> entriesListViewModel.onUnmarkAll() }
                .setNegativeButton("No") { _, _ -> }
                .create()
                .show()
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data == null) return

            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true

            var imageStream = requireContext().contentResolver.openInputStream(data.data!!)
            BitmapFactory.decodeStream(imageStream, null, options)
            imageStream!!.close()

            options.inSampleSize = calculateInSampleSize(options)
            options.inJustDecodeBounds = false

            imageStream = requireContext().contentResolver.openInputStream(data.data!!)

            val ei: ExifInterface = run {
                if (Build.VERSION.SDK_INT > 23)
                    ExifInterface(requireContext().contentResolver.openInputStream(data.data!!)!!)
                else
                    ExifInterface(data.data!!.path!!)
            }

            val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val image = BitmapFactory.decodeStream(imageStream, null, options)
            val outputStream = ByteArrayOutputStream()
            rotateIfRequired(image!!, orientation).compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

            val encodedString = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
            entriesListViewModel.onUpsertImage(replacedImageEntryId, encodedString)
            replacedImageEntryId = 0L
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options): Int {
        val optionsWidth = options.outWidth
        val optionsHeight = options.outHeight
        var inSampleSize = 1

        if (optionsWidth > MAX_ALOWED_WIDTH || optionsHeight > MAX_ALLOWED_HEIGHT) {
            val widthRatio = round(optionsWidth.toDouble() / MAX_ALOWED_WIDTH)
            val heightRatio = round(optionsHeight.toDouble() / MAX_ALLOWED_HEIGHT)
            inSampleSize = min(widthRatio, heightRatio).toInt()

            val totalPixels = optionsWidth * optionsHeight
            val totalPixelsCap = MAX_ALOWED_WIDTH * MAX_ALLOWED_HEIGHT * 2
            while (totalPixels / (inSampleSize * inSampleSize) > totalPixelsCap)
                inSampleSize++
        }

        return inSampleSize
    }

    private fun rotateIfRequired(image: Bitmap, orientation: Int): Bitmap {
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(image, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(image, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(image, 270)
            else -> image
        }
    }

    private fun rotateImage(image: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotated = Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
        image.recycle()
        return rotated
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val position = adapter.position
        val itemId = item.itemId
        val entryId = adapter.getId(position)
        val entryMarkable = adapter.getMarkable(position)
        if (itemId == 1) {
            if (entryMarkable)
                entriesListViewModel.onEnter(entryId)
            else {
                replacedImageEntryId = entryId
                binding.addImageButton.callOnClick()
            }
        } else
            AlertDialog.Builder(requireContext())
                .setMessage("Confirm deletion?")
                .setPositiveButton("Yes") { _, _ -> entriesListViewModel.onDelete(entryId) }
                .setNegativeButton("No") { _, _ -> }
                .create()
                .show()
        return super.onContextItemSelected(item)
    }

    companion object {
        const val MAX_ALOWED_WIDTH = 1024
        const val MAX_ALLOWED_HEIGHT = 1024
    }
}
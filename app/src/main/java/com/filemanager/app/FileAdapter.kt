package com.filemanager.app

import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.filemanager.app.databinding.ListItemIosBinding
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.ContextCompat

class FileAdapter(
    private val onClick: (FileItem) -> Unit,
    private val onLongClick: (FileItem) -> Unit,
    private val onSelectionChange: (Int) -> Unit
) :
    ListAdapter<FileItem, FileAdapter.FileViewHolder>(FileDiffCallback()) {

    private val selectedItems = SparseBooleanArray()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ListItemIosBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding, onClick, onLongClick, selectedItems, onSelectionChange)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val fileItem = getItem(position)
        holder.bind(fileItem)
        // --- SELECTION LOGIC START ---
        if (fileItem.isSelected) {
            // Selected state
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.selected_item_background
                )
            )
        } else {
            // Default state
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    android.R.color.transparent
                )
            )
        }
        // --- SELECTION LOGIC END ---
    }

    fun toggleSelection(position: Int) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position)
        } else {
            selectedItems.put(position, true)
        }
        notifyItemChanged(position)
        onSelectionChange(selectedItems.size())
    }

    fun getSelectedItems(): List<FileItem> {
        val items = mutableListOf<FileItem>()
        for (i in 0 until selectedItems.size()) {
            if (selectedItems.valueAt(i)) {
                items.add(getItem(selectedItems.keyAt(i)))
            }
        }
        return items
    }

    fun clearSelection() {
        val selection = selectedItems.size()
        selectedItems.clear()
        notifyDataSetChanged()
        onSelectionChange(0)
    }

    class FileViewHolder(
        private val binding: ListItemIosBinding,
        private val onClick: (FileItem) -> Unit,
        private val onLongClick: (FileItem) -> Unit,
        private val selectedItems: SparseBooleanArray,
        private val onSelectionChange: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (selectedItems.size() > 0) {
                        (bindingAdapter as? FileAdapter)?.toggleSelection(position)
                    } else {
                        onClick(getItem(position))
                    }
                }
            }
            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    (bindingAdapter as? FileAdapter)?.toggleSelection(position)
                    onLongClick(getItem(position))
                }
                true
            }
        }

        fun bind(fileItem: FileItem) {
            binding.tvFileNameIos.text = fileItem.name

            val details = StringBuilder()
            if (fileItem.isDirectory) {
                details.append("Folder")
            } else {
                details.append(formatSize(fileItem.file.length()))
            }
            details.append(" • ")
            details.append(formatDate(fileItem.file.lastModified()))
            binding.tvFileDetailsIos.text = details.toString()

            val iconResId = when {
                fileItem.isDirectory -> R.drawable.ic_folder_ios
                fileItem.name.endsWith(".pdf", true) -> R.drawable.ic_pdf_file
                fileItem.name.endsWith(".mp4", true) || fileItem.name.endsWith(".avi", true) -> R.drawable.ic_video_file
                fileItem.name.endsWith(".apk", true) -> R.drawable.ic_apk_file
                else -> R.drawable.ic_file
            }
            binding.ivFileIconIos.setImageResource(iconResId)
            binding.ivArrowIos.visibility = if (fileItem.isDirectory) View.VISIBLE else View.GONE
        }

        private fun getItem(position: Int): FileItem {
            return (bindingAdapter as FileAdapter).getItem(position)
        }

        private fun formatSize(size: Long): String {
            if (size <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
            return String.format("%.2f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
        }

        private fun formatDate(timestamp: Long): String {
            val date = Date(timestamp)
            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return formatter.format(date)
        }
    }

    private class FileDiffCallback : DiffUtil.ItemCallback<FileItem>() {
        override fun areItemsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
            return oldItem.file.absolutePath == newItem.file.absolutePath
        }

        override fun areContentsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
            return oldItem == newItem
        }
    }
}

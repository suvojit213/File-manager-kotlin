package com.filemanager.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

class FileAdapter(
    private val onItemClick: (FileItem) -> Unit,
    private val onItemLongClick: (FileItem) -> Unit,
    private val onSelectionChange: (Int) -> Unit
) : ListAdapter<FileItem, FileAdapter.FileViewHolder>(FileDiffCallback()) {

    private val selectedItems = mutableSetOf<FileItem>()
    private var actionMode: ActionMode? = null

    fun startActionMode(view: View, callback: ActionMode.Callback) {
        if (actionMode == null) {
            actionMode = view.startActionMode(callback)
        }
    }

    fun finishActionMode() {
        actionMode?.finish()
        actionMode = null
    }

    fun toggleSelection(fileItem: FileItem) {
        if (selectedItems.contains(fileItem)) {
            selectedItems.remove(fileItem)
        } else {
            selectedItems.add(fileItem)
        }
        onSelectionChange(selectedItems.size)
        notifyItemChanged(currentList.indexOf(fileItem))
    }

    fun clearSelection() {
        selectedItems.clear()
        onSelectionChange(0)
        notifyDataSetChanged()
    }

    fun getSelectedItems(): Set<FileItem> {
        return selectedItems
    }

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileIcon: ImageView = itemView.findViewById(R.id.fileIcon)
        val fileName: TextView = itemView.findViewById(R.id.fileName)
        val fileDetails: TextView = itemView.findViewById(R.id.fileDetails)
        val arrowIcon: ImageView = itemView.findViewById(R.id.arrowIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val fileItem = getItem(position)
        
        holder.fileName.text = fileItem.name
        
        // Set icon and background based on file type
        if (fileItem.isDirectory) {
            holder.fileIcon.setImageResource(R.drawable.ic_folder)
            holder.arrowIcon.visibility = View.VISIBLE
        } else {
            holder.arrowIcon.visibility = View.GONE

            when (fileItem.extension.lowercase()) {
                "jpg", "jpeg", "png", "webp" -> {
                    Glide.with(holder.itemView.context)
                         .load(fileItem.path)
                         .placeholder(R.drawable.ic_file) // Default icon
                         .into(holder.fileIcon)
                }
                "mp4", "mkv", "avi", "mov" -> {
                     Glide.with(holder.itemView.context)
                         .load(fileItem.path)
                         .placeholder(R.drawable.ic_video_file) // Video icon
                         .into(holder.fileIcon)
                }
                "pdf" -> {
                    holder.fileIcon.setImageResource(R.drawable.ic_pdf_file) // PDF icon
                }
                "apk" -> {
                     holder.fileIcon.setImageResource(R.drawable.ic_apk_file) // APK icon
                }
                else -> {
                    holder.fileIcon.setImageResource(R.drawable.ic_file) // Generic file icon
                }
            }
        }
        
        // Set background for selected items
        if (selectedItems.contains(fileItem)) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.item_selected_background))
        } else {
            if (fileItem.isDirectory) {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.item_folder_background))
            } else {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.item_file_background))
            }
        }

        // Format date and details
        val dateFormat = SimpleDateFormat("dd.MM.yyyy, hh:mm a", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(fileItem.lastModified))
        
        val details = if (fileItem.isDirectory) {
            "$formattedDate - ${fileItem.itemCount} ${if (fileItem.itemCount == 1) "item" else "items"}"
        } else {
            "$formattedDate - ${formatFileSize(fileItem.size)}"
        }
        
        holder.fileDetails.text = details
        
        holder.itemView.setOnClickListener {
            if (actionMode != null) {
                toggleSelection(fileItem)
            } else {
                onItemClick(fileItem)
            }
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClick(fileItem)
            true
        }
    }

    private fun formatFileSize(size: Long): String {
        return when {
            size >= 1024 * 1024 * 1024 -> String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0))
            size >= 1024 * 1024 -> String.format("%.1f MB", size / (1024.0 * 1024.0))
            size >= 1024 -> String.format("%.1f kB", size / 1024.0)
            else -> "$size B"
        }
    }
}

class FileDiffCallback : DiffUtil.ItemCallback<FileItem>() {
    override fun areItemsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
        return oldItem.path == newItem.path
    }

    override fun areContentsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
        return oldItem == newItem
    }
}
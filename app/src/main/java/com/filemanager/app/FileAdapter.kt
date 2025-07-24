package com.filemanager.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class FileAdapter(
    private var fileList: List<FileItem>,
    private val onItemClick: (FileItem) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

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
        val fileItem = fileList[position]
        
        holder.fileName.text = fileItem.name
        
        // Set icon based on file type
        if (fileItem.isDirectory) {
            holder.fileIcon.setImageResource(R.drawable.ic_folder)
            holder.arrowIcon.visibility = View.VISIBLE
        } else {
            holder.fileIcon.setImageResource(getFileIcon(fileItem.name))
            holder.arrowIcon.visibility = View.GONE
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
            onItemClick(fileItem)
        }
    }

    override fun getItemCount(): Int = fileList.size

    fun updateFiles(newFileList: List<FileItem>) {
        fileList = newFileList
        notifyDataSetChanged()
    }

    private fun getFileIcon(fileName: String): Int {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg", "png", "gif", "bmp" -> R.drawable.ic_file
            "mp4", "avi", "mkv", "mov" -> R.drawable.ic_file
            "mp3", "wav", "flac", "aac" -> R.drawable.ic_file
            "pdf" -> R.drawable.ic_file
            "txt", "doc", "docx" -> R.drawable.ic_file
            "zip", "rar", "7z" -> R.drawable.ic_file
            "apk" -> R.drawable.ic_file
            else -> R.drawable.ic_file
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


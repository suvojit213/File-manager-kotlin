package com.filemanager.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.filemanager.app.databinding.ListItemIosBinding

class StorageLocationAdapter(private val onClick: (FileItem) -> Unit) :
    ListAdapter<FileItem, StorageLocationAdapter.StorageLocationViewHolder>(FileItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StorageLocationViewHolder {
        val binding = ListItemIosBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StorageLocationViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: StorageLocationViewHolder, position: Int) {
        val fileItem = getItem(position)
        holder.bind(fileItem)
    }

    class StorageLocationViewHolder(private val binding: ListItemIosBinding, private val onClick: (FileItem) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(fileItem: FileItem) {
            binding.tvFileNameIos.text = fileItem.name
            binding.tvFileDetailsIos.text = fileItem.details

            binding.ivFileIconIos.setImageResource(fileItem.icon)
            binding.ivArrowIos.setImageResource(R.drawable.ic_arrow_right_ios)

            itemView.setOnClickListener { onClick(fileItem) }
        }
    }

    private class FileItemDiffCallback : DiffUtil.ItemCallback<FileItem>() {
        override fun areItemsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
            return oldItem.file.absolutePath == newItem.file.absolutePath
        }

        override fun areContentsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
            return oldItem == newItem
        }
    }
}
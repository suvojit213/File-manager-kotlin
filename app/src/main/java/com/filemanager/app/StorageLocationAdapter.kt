package com.filemanager.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.filemanager.app.databinding.ListItemIosBinding

class StorageLocationAdapter(private val onClick: (StorageViewModel.StorageVolume) -> Unit, private val formatSize: (Long) -> String) :
    ListAdapter<StorageViewModel.StorageVolume, StorageLocationAdapter.StorageLocationViewHolder>(StorageVolumeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StorageLocationViewHolder {
        val binding = ListItemIosBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StorageLocationViewHolder(binding, onClick, formatSize)
    }

    override fun onBindViewHolder(holder: StorageLocationViewHolder, position: Int) {
        val storageVolume = getItem(position)
        holder.bind(storageVolume)
    }

    class StorageLocationViewHolder(private val binding: ListItemIosBinding, private val onClick: (StorageViewModel.StorageVolume) -> Unit, private val formatSize: (Long) -> String) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(storageVolume: StorageViewModel.StorageVolume) {
            binding.tvFileNameIos.text = storageVolume.name
            binding.tvFileDetailsIos.text = "${formatSize(storageVolume.freeSpace)} free of ${formatSize(storageVolume.totalSpace)}"

            val iconResId = if (storageVolume.isPrimary) R.drawable.ic_phone_storage_ios else R.drawable.ic_sd_card_ios
            binding.ivFileIconIos.setImageResource(iconResId)
            binding.ivArrowIos.setImageResource(R.drawable.ic_arrow_right_ios)

            itemView.setOnClickListener { onClick(storageVolume) }
        }
    }

    private class StorageVolumeDiffCallback : DiffUtil.ItemCallback<StorageViewModel.StorageVolume>() {
        override fun areItemsTheSame(oldItem: StorageViewModel.StorageVolume, newItem: StorageViewModel.StorageVolume): Boolean {
            return oldItem.path == newItem.path
        }

        override fun areContentsTheSame(oldItem: StorageViewModel.StorageVolume, newItem: StorageViewModel.StorageVolume): Boolean {
            return oldItem == newItem
        }
    }
}